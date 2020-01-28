/** @file ADS1256.cpp
    @brief E.A.SY. Digitizer.

    This contains the library for the ADS1256

    @author Stelios Voutsinas (stevo)
    @bug No known bugs.
*/
/** @defgroup ADS1256_library
  @{
*/
#include "ADS1256.h"
#include <Energia.h>
#include <msp432.h>

#include <SPI.h>
#include <eusci.h>
#include <interrupt.h>
#include <debug.h>

volatile int DRDY_state = HIGH;
/** @brief constructor
*/
Digitizer::Digitizer(void) {

}
/** @brief starts the module
    @param[in] DRDYPIN Pin for Data ready
    @param[in] CSPIN Pin for Chip select
    @param[in] RESETPIN Pin for Reset
    @param[in] SYNCPIN Pin for sync
*/
void Digitizer::begin( int DRDYPIN,  int CSPIN, int RESETPIN, int SYNCPIN)
{
  _DRDY = DRDYPIN;
  _CS = CSPIN;
  _RESET = RESETPIN;
  _SYNC = SYNCPIN;
  pinMode(_SYNC, OUTPUT); //SYNC active low powerdown
  digitalWrite(_SYNC, HIGH); //defalt POWER UP state
  pinMode(_RESET, OUTPUT);
  digitalWrite(_RESET, HIGH);
  pinMode(_DRDY, INPUT);
  pinMode(_CS, OUTPUT);
  attachInterrupt(_DRDY, DRDY_Int, FALLING); //Interrupt setup for DRDY detection
  SPI.setClockDivider(SPI_CLOCK_DIV16);      //MSP432 Max supported SPI clock = 16MHz. ADS1256 Max supported SPI clock <= 1.92MHz. SPI operates@1MHz
  SPI.setBitOrder(MSBFIRST);                 //MSB comes first
  SPI.setDataMode(SPI_MODE1);                //CPOL = 0, CPHA = 1
  SPI.begin(_CS);

}

/** @brief Starts the device
  @param[in] drate Data rate register value
  @param[in] gain Gain value
  @param[in] gpio gpio value
*/
void Digitizer::setDeviceReady(unsigned char drate, unsigned char gain, unsigned char gpio)
{
  resetCommand(); // Reset the digitiser
  cmd(SDATAC); // send out SDATAC command to stop continous reading mode(before issue the command wait for DRDY).
  setRegValue(DRATE, drate); // write data rate register
  byte ADCONreg = getRegValue(ADCON);
  bitClear(ADCONreg, 2);
  bitClear(ADCONreg, 1);
  bitClear(ADCONreg, 0);
  ADCONreg |= gain;
  setRegValue(ADCON, ADCONreg);
  enableCS();
  SPI.transfer(SYNC);
  delayMicroseconds(t11); //t11 delay (24*tCLKIN) after SYNC command
  SPI.transfer(WAKEUP);
  disableCS();
  setGPIOPins(gpio);
  cmd(SELFCAL); // perform Self Offset and Gain Calibration
  delay(5000); // wait 5 secs to settle down
  enableCS();
  SPI.transfer(SYNC);
  delayMicroseconds(t11); //t11 delay (24*tCLKIN) after SYNC command
  SPI.transfer(WAKEUP);
  disableCS();
}

/** @brief Writes a value on a register using 3 byte commands (Register, numOfRegisters-1, Data)
    @param[in] reg Register to write value
    @param[in] wdata The value to be written
*/
void Digitizer::setRegValue(unsigned char reg, unsigned char wdata)
{
  enableCS();
  //noInterrupts();
  SPI.transfer(WREG | reg);
  SPI.transfer(0);
  SPI.transfer(wdata);
  delayMicroseconds(t11rw); //t11 delay (4*tCLKIN) after WREG command
  //interrupts();
  disableCS();
}

/** @brief Reads the value of a register
    @param[in] reg Register to be read
    @return Data read from the register
*/
unsigned char Digitizer::getRegValue(unsigned char reg)
{
  unsigned char readValue;
  enableCS();
  //noInterrupts();
  SPI.transfer(RREG | reg);
  SPI.transfer(NOP);
  delayMicroseconds(t6); //t6 delay (50*tCLKIN)
  readValue = SPI.transfer(NOP);
  delayMicroseconds(t11rw); //t11 delay after RREG command
  //interrupts();
  disableCS();
  return readValue;
}


/** @brief writes to the offset and fullscale calibration registers predefined values
    @param[in] OFC0data Register data to be written
    @param[in] OFC1data Register data to be written
    @param[in] OFC2data Register data to be written
    @param[in] FSC0data Register data to be written
    @param[in] FSC1data Register data to be written
    @param[in] FSC2data Register data to be written
*/
void Digitizer::setCal( unsigned char OFC0data, unsigned char OFC1data, unsigned char OFC2data, unsigned char FSC0data, unsigned char FSC1data, unsigned char FSC2data) {
  setRegValue(OFC0, OFC0data);
  setRegValue(OFC1, OFC1data);
  setRegValue(OFC2, OFC2data);
  setRegValue(FSC0, FSC0data);
  setRegValue(FSC1, FSC1data);
  setRegValue(FSC2, FSC2data);
}

/** @brief Sends a command to the ads1256
    @param comm Command to send
*/
void Digitizer::cmd(unsigned char comm)
{
  enableCS();
  if ((comm == SDATAC) || (comm == RDATAC)) {
    waitDRDYGoLow(); //t11 delay
  }
  SPI.transfer(comm);
  if ((comm == SELFCAL) || (comm == SELFOCAL) || (comm == SELFGCAL) || (comm == SYSOCAL) || (comm == SYSGCAL) || (comm == RESET) || (comm == STANDBY) || (comm == RDATAC) || (comm == SDATAC)) {
    waitDRDYGoLow(); //t11 delay
  }
  if ((comm == RDATA) || (comm == RREG) || (comm == WREG)) {
    delayMicroseconds(t11rw); //t11 delay
  }
  disableCS();
}

/** @brief Sets GPIO pins as inputs or outputs
    @param[in] gpiopins GPIO pins to configure
*/
void Digitizer::setGPIOPins(unsigned char gpiopins) {
  unsigned char IOreg = getRegValue(IO);
  bitClear(IOreg, 7);
  bitClear(IOreg, 6);
  bitClear(IOreg, 5);
  bitClear(IOreg, 4);
  IOreg |= gpiopins;
  setRegValue(IO, IOreg);
}

/** @brief Enables the internal buffer
    @param[in] status Buffer state
*/
void Digitizer::buffEnable(boolean state) {
  unsigned char STATUSreg = getRegValue(STATUS);
  if (state) {
    STATUSreg |= 0x02;
    setRegValue(STATUS, STATUSreg);
  }
  else {
    STATUSreg |= 0x00;
    setRegValue(STATUS, STATUSreg);
  }
}

/** @brief Reads the value from a channel
    @return Data from the selected channel
*/
long Digitizer::getChannel()
{
  unsigned long value;
  enableCS();
  SPI.transfer(RDATA);
  delayMicroseconds(t6); //t6 delay (50*tCLKIN)
  // Combine all 3-bytes to 24-bit data using byte shifting.
  value |= SPI.transfer(NOP);
  value <<= 8;
  value |= SPI.transfer(NOP);
  value <<= 8;
  value |= SPI.transfer(NOP);

  if (value & 0x800000) {
    value |= 0xFF000000;
  }
  disableCS();
  return (long)value;
}

/** @brief Switches between channels
    @details Channel Switching for differential mode. Use -1 to set input channel to AINCOM
             Channel switching for single ended mode. Negative input channel are automatically set to AINCOM
    @param[in] AIN_P Positive analog input
    @param[in] AIN_N Negative analog input
*/
void Digitizer::setChannel(unsigned char AIN_P, unsigned char AIN_N)
{
  unsigned char MUX_CHANNEL;
  unsigned char MUXP;
  unsigned char MUXN;

  switch (AIN_P)
  {
    case 0 :
      MUXP = ADS1256_MUXP_AIN0;
      break;
    case 1 :
      MUXP = ADS1256_MUXP_AIN1;
      break;
    case 2 :
      MUXP = ADS1256_MUXP_AIN2;
      break;
    case 3 :
      MUXP = ADS1256_MUXP_AIN3;
      break;
    case 4 :
      MUXP = ADS1256_MUXP_AIN4;
      break;
    case 5 :
      MUXP = ADS1256_MUXP_AIN5;
      break;
    case 6 :
      MUXP = ADS1256_MUXP_AIN6;
      break;
    case 7 :
      MUXP = ADS1256_MUXP_AIN7;
      break;
    default:
      MUXP = ADS1256_MUXP_AINCOM;
  }

  switch (AIN_N)
  {
    case 0 :
      MUXN = ADS1256_MUXN_AIN0;
      break;
    case 1 :
      MUXN = ADS1256_MUXN_AIN1;
      break;
    case 2 :
      MUXN = ADS1256_MUXN_AIN2;
      break;
    case 3 :
      MUXN = ADS1256_MUXN_AIN3;
      break;
    case 4 :
      MUXN = ADS1256_MUXN_AIN4;
      break;
    case 5 :
      MUXN = ADS1256_MUXN_AIN5;
      break;
    case 6 :
      MUXN = ADS1256_MUXN_AIN6;
      break;
    case 7 :
      MUXN = ADS1256_MUXN_AIN7;
      break;
    default:
      MUXN = ADS1256_MUXN_AINCOM;
  }
  MUX_CHANNEL = MUXP | MUXN;

  waitDRDYGoLow();
  setRegValue(MUX, MUX_CHANNEL); // write mux register
  enableCS();
  SPI.transfer(SYNC);
  delayMicroseconds(t11); //t11 delay (24*tCLKIN) after SYNC command
  SPI.transfer(WAKEUP);
  disableCS();
}

/** @brief Returns ADS1256 Factory ID
*/
unsigned char Digitizer:: getFactID(void)
{
  unsigned char id = getRegValue(STATUS);
  return (id >> 4);
}

/** @brief setSDCSMode procedure
    @param uAsetting SDCS mode setting
*/
void Digitizer::setSDCSMode(int uAsetting) {
  byte ADCONreg = getRegValue(ADCON);
  bitClear(ADCONreg, 4);
  bitClear(ADCONreg, 3);

  switch (uAsetting) {
    case 0:
      ADCONreg |= setSDCSMode_00;
      setRegValue(ADCON, ADCONreg);
      break;
    case 1:
      ADCONreg |= setSDCSMode_01;
      setRegValue(ADCON, ADCONreg);
      break;
    case 2:
      ADCONreg |= setSDCSMode_10;
      setRegValue(ADCON, ADCONreg);
      break;
    case 3:
      ADCONreg |= setSDCSMode_11;
      setRegValue(ADCON, ADCONreg);
      break;
  }
}

/** @brief Reads the values from the four main registers
*/
void Digitizer::getConf() {

  unsigned char STATUSreg;
  unsigned char MUXreg;
  unsigned char ADCONreg;
  unsigned char DRATEreg;
  unsigned char IOreg;
  unsigned char OFC0reg;
  unsigned char OFC1reg;
  unsigned char OFC2reg;
  unsigned char FSC0reg;
  unsigned char FSC1reg;
  unsigned char FSC2reg;


  STATUSreg = getRegValue(STATUS);
  MUXreg = getRegValue(MUX);
  ADCONreg = getRegValue(ADCON);
  DRATEreg = getRegValue(DRATE);
  IOreg = getRegValue(IO);
  OFC0reg = getRegValue(OFC0);
  OFC1reg = getRegValue(OFC1);
  OFC2reg = getRegValue(OFC2);
  FSC0reg = getRegValue(FSC0);
  FSC1reg = getRegValue(FSC1);
  FSC2reg = getRegValue(FSC2);

  Serial.println("STATUSreg");
  lzByte(STATUSreg, 8);
  Serial.println();
  Serial.println("MUXreg \t\t");
  lzByte(MUXreg, 8);
  Serial.println();
  Serial.println("ADCONreg \t");
  lzByte(ADCONreg, 8);
  Serial.println();
  Serial.println("DRATEreg \t");
  lzByte(DRATEreg, 8);
  Serial.println();
  Serial.println("IOreg \t");
  lzByte(IOreg, 8);
  Serial.println();
  Serial.println("OFCregs \t");
  lzByte(OFC2reg, 8);
  Serial.print(" ");
  lzByte(OFC1reg, 8);
  Serial.print(" ");
  lzByte(OFC0reg, 8);
  Serial.println();
  Serial.println("FCSregs \t");
  lzByte(FSC2reg, 8);
  Serial.print(" ");
  lzByte(FSC1reg, 8);
  Serial.print(" ");
  lzByte(FSC0reg, 8);
  Serial.println();
  delay(10000);
}

/** @brief Forces a reset by setting the RESET pin low for a certain time
*/
void Digitizer::pinReset() {
  digitalWrite(RESET, LOW); // reset is performed
  delayMicroseconds(t16); //RESET, SYNC/PDWN, pulse width
  digitalWrite(RESET, HIGH); //enable device
}

/** @brief Forces a reset by issuing the RESET command for a certain time
*/
void Digitizer::resetCommand() {
  cmd(RESET);  //reset all registers except the CLK0 and CLK1 bits in the ADCON register to their default values
}

/** @brief Synchroniszation by pin
*/
void Digitizer::pinSync() {
  digitalWrite(_SYNC, LOW); //sync is performed
  delayMicroseconds(t16); //RESET, SYNC/PDWN, pulse width
  digitalWrite(_SYNC, HIGH); //enable device
}

/** @brief Sets CS low
*/
void Digitizer::enableCS()
{
  digitalWrite(_CS, LOW);
  delayT3; //CS low to first SCLK: setup time
}

/** @brief Sets CS high
*/
void Digitizer::disableCS()
{
  delayMicroseconds(t10); //CS low after final SCLK falling edge
  digitalWrite(_CS, HIGH);
}

/** @brief prints an n bit int with its leading zeros
*/
void Digitizer::lzByte(int data, int len) {
  for (int j = len - 1; j >= 0; j--)
  {
    Serial.print(bitRead(data, j));
  }
}

/** @brief Waits for DRDY pin to go low
*/
void Digitizer::waitDRDYGoLow()
{
  while (DRDY_state) continue;
  //EnergiaV18+
  noInterrupts();
  DRDY_state = HIGH;
  interrupts();
}
/** @brief Interrupt function
*/
void DRDY_Int() {
  DRDY_state = LOW;
}
/* @} */
