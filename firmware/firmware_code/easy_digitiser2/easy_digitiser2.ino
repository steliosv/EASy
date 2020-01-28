/**
   @mainpage E.A.SY. firmware
   @version v20
   @author Stelios Voutsinas
   CreateDate 2020

*/

/** @file easyv20.ino
    @brief E.A.SY. ads1256 Unit.

    This contains the library for the ADS1256

    @author Stelios Voutsinas (stevo)
    @bug No known bugs.
*/

/** @defgroup   EASY ads1256 firmware
  @{
*/
#include "ADS1256.h"
#include "dcrem.h"
#include "RS-FEC.h"
#include <stdio.h>
#include <stdlib.h>
#include <driverlib/timer_a.h>
#include <driverlib/interrupt.h>
#define NVIC_ISER0                                         (HWREG32(0xE000E100)) /* Irq 0 to 31 Set Enable Register */

unsigned long count  = 0;
unsigned long prevTime = 0;
unsigned long nowSample = 0;
unsigned long prevSample = 0;
unsigned long timelapse = 0;
const int LEDR = RED_LED;
const int LEDB = BLUE_LED;
const int LEDG = GREEN_LED;
const int SCLK_PIN = 7; //sck
const int CS_PIN = 18; //Chip Select active low
const int MISO_PIN = 14; //MISO
const int MOSI_PIN = 15; //MOSI
const int DRDY_PIN = 13; //DATA READY
const int SYNC_PIN = 12; //syncronisation power down active low
const int RESET_PIN = 11; //RESET PIN active low
const int BUTTON1 = PUSH1;
const int BUTTON2 = PUSH2;
const int SYSOCAL_PIN = 25; //pin that performs System Offset Calibration
const int SYSGCAL_PIN = 26; //pin that performs System Gain Calibration
const int WDT_PIN = 40;//pin that "pets" the watchdog timer
const int OV_PIN = 39;//pin that informs for Overvoltage failure
const int UV_PIN = 38;//pin that informs for Undervoltage failure
const int RS_PIN = A15;//pin that enables reed solomon encoding
int WDTEnable = true; //Enables WDT
int WDTState = 0;
int SettleState = 0;
int RSState = true;

volatile int confStat = false;
volatile int sdcsStat = false;
volatile int sysoCalStat = false;
volatile int sysgCalStat = false;
volatile int state = false;
volatile int a = 0;
unsigned char OFC0data;
unsigned char OFC1data;
unsigned char OFC2data;
unsigned char FSC0data;
unsigned char FSC1data;
unsigned char FSC2data;
unsigned long sampleTime = 0;
const int msglen = 80;
const uint8_t ECC_LENGTH = 10;  //Max message lenght, and "guardian bytes", Max corrected bytes ECC_LENGTH/2
char message_frame[msglen]; //max chars: 80
char encoded_frame[msglen + ECC_LENGTH]; //max chars: 90
RS::ReedSolomon<msglen, ECC_LENGTH> rs;

float fclk_in_MHZ;
int filtinit = 0;
int cntSample = 0;
long DataPacket[9] = {
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
};

Digitizer ads1256;
DC accew, accns, accz;
DC geoew, geons, geoz;


/** @brief Monitor execution time
    @param[in] now current timestamp
    @return The elapsed time
*/
unsigned long timeStamp(unsigned long now) {
  timelapse = now - prevTime;
  prevTime = now;
  Serial.print("Period: ");
  Serial.print(timelapse / 1000);
  Serial.print("ms ");
  Serial.print(1000.0 / (timelapse / 1000));
  Serial.println("SPS");
  return timelapse;
}

/** @brief Configure a timer
    @param[in] periodInUS timer period expressed in microseconds
*/
void setTimer(unsigned periodInUS)
{
  // Configuration word
  // Bits 15-10: Unused
  // Bits 9-8: Clock source select: set to SMCLK (12MHz)
  // Bits 7-6: Input divider: set to 4
  // Bits 5-4: Mode control: Count up to TACCRO and reset
  // Bit 3: Unused
  // Bits 2: TACLR : set to initially clear timer system
  // Bit 1: Enable interrupts from TA0
  // Bit 0: Interrupt (pending) flag : set to zero (initially)
  TA3CTL = 0b0000001010010110;
  TA3CCR0 = periodInUS * 3; // Set TACCR0 = Period (3MHz clock)
  TA3CCTL0 = BIT4; // Enable interrupts when TAR = TACCR0
  // The following places the address of our interrupt service routine in the RAM based interrupt vector table
  // The vector number is 14 + 16  = 30 which is represented by the symbol INT_TA3_0
  Interrupt_registerInterrupt(INT_TA3_0, timerISR);
  // according to the datasheet Table 6-12 timer A3 is on ISR 14
  NVIC_ISER0 = (1 << 14); // enable this interrupt in the NVIC
}

/** @brief Timer ISR
*/
void timerISR(void)
{
  TA3CTL &= ~1;         // Acknowledge the interrupt
  TA3CCTL0 &= ~1;       // Acknowledge the interrupt
  NVIC->ICPR[0] = (1 << 14); // clear interrupt pending flag in NVIC
  state = true;
}

/** @brief discarts data until the filter settles down
*/
void discartData()
{
  ads1256.setChannel(0 , COM);
  ads1256.getChannel(); //ch1 - GND
  ads1256.setChannel(3 , COM);
  ads1256.getChannel(); //ch0 - GND
  ads1256.setChannel(2 , COM);
  ads1256.getChannel(); //ch3 - GND
  ads1256.setChannel(4 , COM);
  ads1256.getChannel(); //ch2 - GND
  ads1256.setChannel(5 , COM);
  ads1256.getChannel(); //ch4 - GND
  ads1256.setChannel(6 , COM);
  ads1256.getChannel(); //ch5 - GND
  ads1256.setChannel(7 , COM);
  ads1256.getChannel(); //ch6 - GND
  ads1256.setChannel(1 , COM);
  ads1256.getChannel(); //ch7 - GND
}

/** @brief Cycle and fetch data from all channels
    @details Due to a setting on the ADS1256-EVM board Chan 0&1 and Chan 2&3 are crossovered keep in mind that you have to re invert them in order
    to display data properly (e.g. chan 0, chan 1, chan 2, chan 3 etc...)
*/
void efficientInputCycling()
{
  ads1256.setChannel(0 , COM);
  DataPacket[0] = ads1256.getChannel(); //ch1 - GND
  ads1256.setChannel(3 , COM);
  DataPacket[1] = ads1256.getChannel(); //ch0 - GND
  ads1256.setChannel(2 , COM);
  DataPacket[2] = ads1256.getChannel(); //ch3 - GND
  ads1256.setChannel(4 , COM);
  DataPacket[3] = ads1256.getChannel(); //ch2 - GND
  ads1256.setChannel(5 , COM);
  DataPacket[4] = ads1256.getChannel(); //ch4 - GND
  ads1256.setChannel(7 , COM);
  DataPacket[5] = ads1256.getChannel(); //ch5 - GND
  // deprecated just put a zero to the DataPacket[6]
  //ads1256.setChannel(7 , COM);
  //DataPacket[6] =  ads1256.getChannel(); //ch6 - GND
  DataPacket[6] = 0; //ch6 - GND
  ads1256.setChannel(1 , COM);
  DataPacket[7] = ads1256.getChannel(); //ch7 - GND
  //clear dc constant
  DataPacket[0] = accew.autoRemoveDCConst(DataPacket[0]);
  DataPacket[1] = accns.autoRemoveDCConst(DataPacket[1]);
  DataPacket[2] = accz.autoRemoveDCConst(DataPacket[2]);
  DataPacket[3] = geoew.autoRemoveDCConst(DataPacket[3]);
  DataPacket[4] = geons.autoRemoveDCConst(DataPacket[4]);
  DataPacket[5] = geoz.autoRemoveDCConst(DataPacket[5]);
  //unfortunately MSP432 does not support CHANGE constant on attach interrupt
  if (WDTEnable) {
    if (digitalRead(OV_PIN) && !digitalRead(UV_PIN)) {
      DataPacket[8] = 2;
    }
    else if (!digitalRead(OV_PIN) && digitalRead(UV_PIN)) {
      DataPacket[8] = 1;
    }
    else {
      DataPacket[8] = 0;
    }
  }
  else {
    DataPacket[8] = -1;
  }
  if (RSState) {
    memset(message_frame, 0, sizeof(message_frame)); // Clear the array
    sprintf(message_frame, "%ld %ld %ld %ld %ld %ld %ld %ld %ld", DataPacket[0], DataPacket[1], DataPacket[2], DataPacket[3], DataPacket[4], DataPacket[5], DataPacket[6], DataPacket[7], DataPacket[8]);
    //sprintf(message_frame, "%ld %ld %ld %ld %ld %ld %ld %ld %ld", j,j,j,j,j,j,j,j,j);
    rs.Encode(message_frame, encoded_frame);
    for (uint i = 0; i < sizeof(encoded_frame); i++) {
      Serial.print(encoded_frame[i]);
    }
    Serial.println();
  }
  else {
    for (uint i = 0; i < 9; i++) {
      Serial.print(DataPacket[i]);
      Serial.print(" ");
    }
    Serial.println();
  }
}

/** @brief Initialisation of the main features of the program
*/
void setup()
{
  fclk_in_MHZ = 7.68;
  digitalWrite(LEDR, HIGH);
  Serial.begin(115200);
  Serial.flush();
  delay(5000); // wait 5 secs
  digitalWrite(LEDR, LOW);
  digitalWrite(LEDG, HIGH);
  pinMode(BUTTON1, INPUT_PULLUP);
  pinMode(BUTTON2, INPUT_PULLUP);
  pinMode(SYSOCAL_PIN, INPUT);
  pinMode(SYSGCAL_PIN, INPUT);
  pinMode(RS_PIN, INPUT);
  pinMode(OV_PIN, INPUT_PULLUP);
  pinMode(UV_PIN, INPUT_PULLUP);
  pinMode(WDT_PIN, OUTPUT);
  if (digitalRead(RS_PIN)) {
    RSState = true;
  } else {
    RSState = false;
  }
  attachInterrupt(BUTTON1, conf, FALLING); //Interrupt setup for Falling DRDY detection
  attachInterrupt(BUTTON2, sdcs, FALLING); //Interrupt setup for Falling DRDY detection
  attachInterrupt(SYSOCAL_PIN, sysoCal, RISING); //Interrupt setup for system calibration detection
  attachInterrupt(SYSGCAL_PIN, sysgCal, RISING); //Interrupt setup for system gain detection
  delay(1000);               // wait for a second
  digitalWrite(LEDG, LOW);
  digitalWrite(LEDB, HIGH);
  ads1256.begin(DRDY_PIN, CS_PIN, RESET_PIN, SYNC_PIN);
  //GPIO pins set as outputs to decrease power dissipation
  ads1256.setDeviceReady(ADS1256_DRATE_1000SPS, ADS1256_GAIN_1, GPIO_IIIO); //after assigning a PGA value and Data rate, Calibration is needed
  ads1256.setChannel(1 , COM);
  delay(1000);               // wait for a second
  digitalWrite(LEDB, LOW);
  for (filtinit = 0;  filtinit < accew.getTSettle(SPS100); filtinit++) {
    //dont transmit serial data unless the filter has settled down
    digitalWrite(LEDG, (SettleState) ? HIGH : LOW);
    SettleState = !SettleState;
    discartData();
  }
  digitalWrite(LEDG, LOW);
  setTimer(SPS100);
}

/** @brief Run continuously from top to bottom until the program is stopped
*/
void loop()
{
  if (state) {
    state = false;
    efficientInputCycling();
  }

  if (sdcsStat) {
    Serial.println("Entering SDCS mode");
    ads1256.setSDCSMode(2);
    SPI.transfer(SYNC);
    delayMicroseconds(t11); //t11 delay (4*tCLKIN) after WREG command
    SPI.transfer(WAKEUP);
    efficientInputCycling();
    sdcsStat = false;
  }

  if (confStat) {
    Serial.println("ADS1256 registers values:");
    ads1256.getConf();
    confStat = false;
  }
  if (sysoCalStat) {
    Serial.println("Apply zero input on the inputs");
    delay(5001);
    ads1256.cmd(SYSOCAL); // perform System Offset Calibration
    SPI.transfer(SYNC);
    delayMicroseconds(t11); //t11 delay (4*tCLKIN) after WREG command
    SPI.transfer(WAKEUP);
    delay(35);
    Serial.println("done");
    OFC0data = ads1256.getRegValue(OFC0);
    OFC1data = ads1256.getRegValue(OFC1);
    OFC2data = ads1256.getRegValue(OFC2);
    sysoCalStat = false;
  }
  if (sysgCalStat) {
    Serial.println("Apply full-scale input on the inputs");
    delay(5001);
    ads1256.cmd(SYSGCAL); // perform System Offset Calibration
    SPI.transfer(SYNC);
    delayMicroseconds(t11); //t11 delay (4*tCLKIN) after WREG command
    SPI.transfer(WAKEUP);
    delay(35);
    Serial.println("done");
    FSC0data = ads1256.getRegValue(FSC0);
    FSC1data = ads1256.getRegValue(FSC1);
    FSC2data = ads1256.getRegValue(FSC2);
    sysgCalStat = false;
  }
}
/** @brief Resets the WDT (MAX697) every 10sec
*/
void wdtSnooze() {
  digitalWrite(WDT_PIN, (WDTState) ? HIGH : LOW);
  WDTState = !WDTState;
}

/** @brief SDCS ISR
*/
void sdcs() {
  sdcsStat = true;
}

/** @brief Configuration ISR
*/
void conf() {
  confStat = true;
}

/** @brief System Calibration ISR
*/
void sysoCal() {
  sysoCalStat = true;
}

/** @brief System gain calibration ISR
*/
void sysgCal() {
  sysgCalStat = true;
}
/* @} */
