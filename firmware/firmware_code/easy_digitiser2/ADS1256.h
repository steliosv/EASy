/** @file ADS1256.h
  @brief E.A.SY. Digitizer.

  This contains the Header file for the ADS1256

  @author Stelios Voutsinas (stevo)
  @bug No known bugs.
*/
#ifndef ADS1256_h
#define ADS1256_h
//#include <SPI.h>
#include <Energia.h>
#include <math.h>



/** @defgroup  Registers  ADS1256 Registers list
  @{
*/
/** @brief Status Register */
#define STATUS 0x00
/** @brief Input Multiplexer Control Register */
#define MUX 0x01
/** @brief A/D Control Register */
#define ADCON 0x02
/** @brief A/D Data Rate Register */
#define DRATE 0x03
/** @brief GPIO Control Register */
#define IO 0x04
/** @brief Offset Calibration Byte 0, least significant byte */
#define OFC0 0x05
/** @brief Offset Calibration Byte 1 */
#define OFC1 0x06
/** @brief Offset Calibration Byte 2, most significant byte */
#define OFC2 0x07
/** @brief Full - scale Calibration Byte 0, least significant byte */
#define FSC0 0x08
/** @brief Full - scale Calibration Byte 1 */
#define FSC1 0x09
/** @brief Full - scale Calibration Byte 2, most significant byte */
#define FSC2 0x0A
/* @} */

/** @defgroup  Commands  ADS1256 Command list
  @{
*/
/** @brief Completes SYNC and Exits Standby Mode */
#define WAKEUP 0x00
/** @brief Reads Data */
#define RDATA  0x01
/** @brief Reads Data Continuously */
#define RDATAC 0x03
/** @brief Stop Reading Data Continuously */
#define SDATAC 0x0f
/** @brief Reads from Registers */
#define RREG 0x10
/** @brief Writes to Register */
#define WREG 0x50
/** @brief Performs a self offset and self gain calibration */
#define SELFCAL 0xF0
/** @brief Performs a self offset calibration */
#define SELFOCAL 0xF1
/** @brief Performs a self gain calibration */
#define SELFGCAL 0xF2
/** @brief Performs a system offset calibration */
#define SYSOCAL 0xF3
/** @brief Performs a system gain calibration */
#define SYSGCAL 0xF4
/** @brief Synchronizes the A/D conversion. */
#define SYNC 0xFC
/** @brief Puts the ADS1256 module into a low-power Standby mode */
#define STANDBY 0xFD
/** @brief Reset ADS1256 module by command */
#define RESET  0xFE
/** @brief NOP is used in SPI.Transfer. Just a Dummy byte */
#define NOP 0x00 //0x00 will make a quieter DIN signal than 0fFF, while trying to read data 
/* @} */

/** @defgroup  MUX  ADS1256 multiplexer codes list
  @{
*/
/** @brief Set Positive Input Channel AIN0*/
#define ADS1256_MUXP_AIN0 0x00
/** @brief Set Positive Input Channel AIN1*/
#define ADS1256_MUXP_AIN1 0x10
/** @brief Set Positive Input Channel AIN2*/
#define ADS1256_MUXP_AIN2 0x20
/** @brief Set Positive Input Channel AIN3*/
#define ADS1256_MUXP_AIN3 0x30
/** @brief Set Positive Input Channel AIN4*/
#define ADS1256_MUXP_AIN4 0x40
/** @brief Set Positive Input Channel AIN5*/
#define ADS1256_MUXP_AIN5 0x50
/** @brief Set Positive Input Channel AIN6*/
#define ADS1256_MUXP_AIN6 0x60
/** @brief Set Positive Input Channel AIN7*/
#define ADS1256_MUXP_AIN7 0x70
/** @brief Set Positive Input Channel AINCOM*/
#define ADS1256_MUXP_AINCOM 0x80
/** @brief Set Negative Input Channel AIN0*/
#define ADS1256_MUXN_AIN0 0x00
/** @brief Set Negative Input Channel AIN1*/
#define ADS1256_MUXN_AIN1 0x01
/** @brief Set Negative Input Channel AIN2*/
#define ADS1256_MUXN_AIN2 0x02
/** @brief Set Negative Input Channel AIN3*/
#define ADS1256_MUXN_AIN3 0x03
/** @brief Set Negative Input Channel AIN4*/
#define ADS1256_MUXN_AIN4 0x04
/** @brief Set Negative Input Channel AIN5*/
#define ADS1256_MUXN_AIN5 0x05
/** @brief Set Negative Input Channel AIN6*/
#define ADS1256_MUXN_AIN6 0x06
/** @brief Set Negative Input Channel AIN7*/
#define ADS1256_MUXN_AIN7 0x07
/** @brief Set Negative Input Channel AINCOM*/
#define ADS1256_MUXN_AINCOM 0x08
/** @brief Not a register value, usefull for setChannel function
  @see void Digitizer::setChannel(unsigned char AIN_P, unsigned char AIN_N)
*/
#define COM  -1

/* @} */

/** @defgroup  Gain  ADS1256 gain codes list
  @{
*/
/** @brief Sets Gain multiplier to x1 */
#define ADS1256_GAIN_1  0x00
/** @brief Sets Gain multiplier to x2 */
#define ADS1256_GAIN_2  0x01
/** @brief Sets Gain multiplier to x4 */
#define ADS1256_GAIN_4  0x02
/** @brief Sets Gain multiplier to x8 */
#define ADS1256_GAIN_8  0x03
/** @brief Sets Gain multiplier to x16 */
#define ADS1256_GAIN_16   0x04
/** @brief Sets Gain multiplier to x32 */
#define ADS1256_GAIN_32   0x05
/** @brief Sets Gain multiplier to x64 */
#define ADS1256_GAIN_64   0x06
/* @} */

/** @defgroup  setSDCSMode  ADS1256 setSDCSMode codes list
  @{
*/
/** @brief Sensor detect current sources is disabled */
#define setSDCSMode_00  0x00
/** @brief Sensor detect current sources is set for 0.5uA */
#define setSDCSMode_01  0x08
/** @brief Sensor detect current sources is set for 2uA */
#define setSDCSMode_10  0x10
/** @brief Sensor detect current sources is set for 10uA */
#define setSDCSMode_11  0x18
/* @} */


/** @defgroup SPS 
  @{
*/
/** @brief 1S period in uS*/
#define SEC 1000000
/** @brief 100 SPS period in uS*/
#define SPS100 10000
/** @brief 50 SPS period in uS*/
#define SPS50 20000
/** @brief 25 SPS period in uS*/
#define SPS25 40000
/* @} */

/** @defgroup  DRATE  ADS1256 DRATE codes list
  @{
*/
/** @brief Data rate is set to 30k SPS */
#define ADS1256_DRATE_30000SPS 0xF0
/** @brief Data rate is set to 15k SPS */
#define ADS1256_DRATE_15000SPS 0xE0
/** @brief Data rate is set to 7.5k SPS */
#define ADS1256_DRATE_7500SPS 0xD0
/** @brief Data rate is set to 3.75k SPS */
#define ADS1256_DRATE_3750SPS 0xC0
/** @brief Data rate is set to 2k SPS */
#define ADS1256_DRATE_2000SPS 0xB0
/** @brief Data rate is set to 1k SPS */
#define ADS1256_DRATE_1000SPS 0xA1
/** @brief Data rate is set to 500 SPS */
#define ADS1256_DRATE_500SPS  0x92
/** @brief Data rate is set to 100 SPS */
#define ADS1256_DRATE_100SPS  0x82
/** @brief Data rate is set to 60 SPS */
#define ADS1256_DRATE_60SPS   0x72
/** @brief Data rate is set to 50 SPS */
#define ADS1256_DRATE_50SPS   0x63
/** @brief Data rate is set to 30 SPS */
#define ADS1256_DRATE_30SPS   0x53
/** @brief Data rate is set to 25 SPS */
#define ADS1256_DRATE_25SPS   0x43
/** @brief Data rate is set to 15 SPS */
#define ADS1256_DRATE_15SPS   0x33
/** @brief Data rate is set to 10 SPS */
#define ADS1256_DRATE_10SPS   0x23
/** @brief Data rate is set to 5 SPS */
#define ADS1256_DRATE_5SPS  0x13
/** @brief Data rate is set to 2.5 SPS */
#define ADS1256_DRATE_2_5SPS  0x03
/* @} */

/** @defgroup  time_delays  ADS1256 time_delayslist
  @{
*/
/** @brief ADS1256 Crystal oscillator *
   @details 7.68MHz (default clock)
*/
#define fclckMHz fclk_in_MHZ
extern float fclk_in_MHZ;

/** @brief ADS1256 clock period
   @details 1/7.68Mhz=0.13us
*/
#define tclkin (1 / fclckMHz)

/** @brief ADS1256 T3 time constant
   @details  1 instruction cycle (~ 20.8ns for MSP432)
*/
#define delayT3  __asm(" nop")

/** @brief ADS1256 T6 time constant
   @details 50*tclkin(0.13us)=6.5us (min time)
*/
#define t6  ((int)ceil(50*tclkin)) //7us delay

/** @brief ADS1256 T10 time constant
   @details 8*tclkin(0.13us)=1.04us (min time)
*/
#define t10  ((int)ceil(8*tclkin))//1us delay

/** @brief ADS1256 T11 time constant for RREG, WREG, RDATA commands
   @details 4*tclkin(0.13us)=0.52us (min time)
*/
#define t11rw  ((int)ceil(4*tclkin))//1us delay

/** @brief ADS1256 T11 time constant for SYNC command
   @details 24*tclkin(0.13us)=3.125us (min time)
*/
#define t11  ((int)ceil(24*tclkin))//4us delay

/** @brief ADS1256 T16 time constant
   @details 4*tclkin(0.13us)=0.520us  (min time)
*/
#define t16  ((int)ceil(4*tclkin)) //1us delay

/** @brief ADS1256 T16b time constant
   @details  1 instruction cycle (~ 20.8ns for MSP432)
*/
#define delayT16b  __asm(" nop")
/* @} */

/** @defgroup  ADS1256 GPIO codes list
  @{
*/
/** @brief P3 O P2 O P1 O P0 O*/
#define GPIO_OOOO  0x00
/** @brief P3 O P2 O P1 O P0 I*/
#define GPIO_OOOI  0x10
/** @brief P3 O P2 O P1 I P0 O*/
#define GPIO_OOIO  0x20
/** @brief P3 O P2 O P1 I P0 I*/
#define GPIO_OOII  0x30
/** @brief P3 O P2 I P1 O P0 O*/
#define GPIO_OIOO  0x40
/** @brief P3 O P2 I P1 O P0 I*/
#define GPIO_OIOI  0x50
/** @brief P3 O P2 I P1 I P0 O*/
#define GPIO_OIIO  0x60
/** @brief P3 O P2 I P1 I P0 I*/
#define GPIO_OIII  0x70
/** @brief P3 I P2 O P1 O P0 O*/
#define GPIO_IOOO  0x80
/** @brief P3 I P2 O P1 O P0 I*/
#define GPIO_IOOI  0x90
/** @brief P3 I P2 O P1 I P0 O*/
#define GPIO_IOIO  0xA0
/** @brief P3 I P2 O P1 I P0 I*/
#define GPIO_IOII  0xB0
/** @brief P3 I P2 I P1 O P0 O*/
#define GPIO_IIOO  0xC0
/** @brief P3 I P2 I P1 O P0 I*/
#define GPIO_IIOI  0xD0
/** @brief P3 I P2 I P1 I P0 O*/
#define GPIO_IIIO  0xE0
/** @brief P3 I P2 I P1 I P0 I*/
#define GPIO_IIII  0xF0
/* @} */

/** @brief ADS1256 interrupt function
*/
void DRDY_Int();


class Digitizer
{

  public:
    Digitizer(void);
    void begin( int DRDYpin, int CSpin, int RESETPIN, int SYNCPIN);
    void setRegValue(unsigned char reg, unsigned char wdata);
    unsigned char getRegValue(unsigned char reg);
    void cmd(unsigned char comm);
    long getChannel();
    void setChannel(unsigned char AIN_P, unsigned char AIN_N);
    void setDeviceReady(unsigned char drate, unsigned char gain, unsigned char gpio);
    void setSDCSMode(int uAsetting);
    void getConf();
    void buffEnable(boolean state);
    unsigned char getFactID(void);
    void pinReset();
    void resetCommand();
    void pinSync();
    void waitDRDYGoLow();
    void setGPIOPins(unsigned char gpiopins);
    void setCal( unsigned char OFC0data, unsigned char OFC1data, unsigned char OFC2data, unsigned char FSC0data, unsigned char FSC1data, unsigned char FSC2data);

  private:
    void enableCS();
    void disableCS();
    void lzByte(int data, int len);
    unsigned char _DRDY;
    unsigned char _CS;
    unsigned char _RESET;
    unsigned char _SYNC;
};

#endif
