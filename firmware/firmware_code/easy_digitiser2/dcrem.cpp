/** @file ADS1256.h
  @brief E.A.SY. Digitizer.

  This contains the librery code for the dc offset removal class

  @author Stelios Voutsinas (stevo)
  @bug No known bugs.
*/
/** @defgroup  generic DC removal library
  @{
*/
#include "dcrem.h"
#include <Energia.h>
#include <math.h>



/** @brief constructor
*/
DC::DC(void) {

}

/** @brief Detects and removes the dc offset from the signal. keep in mind that
    \f$ f_{c}=\frac{1-a}{2*pi*\Delta _{\tau }}\f$
    @param[in] curr the current value
    @return the clean AC signal
*/
long DC::autoRemoveDCConst (long curr) {

  long biasedSignal;
  long acSignal;
  biasedSignal = curr;
  acSignal = ALPHA * (biasedSignal + lastOutput - lastInput);
  lastInput  = biasedSignal;
  lastOutput = acSignal;
  return acSignal;
}

/** @brief Computes the neccessary time expressed in samples that the filter needs to settle. Equations used:
    \f$ f_{c}=\frac{1-a}{2*pi*a*\Delta _{\tau }}, \tau _{settle}=-\frac{ln(0.01)}{\zeta *2*pi*f _{s}} \f$
    @param[in] sps the current sps of the digitiser
    @return the samples that need to be omitted in order the filter to settle 
*/
 int DC::getTSettle(int sps) {
  float dt = 1 / (float)sps;
  float fs = (1 - ALPHA) / (2 * PI * ALPHA * dt);
  float ts = 4.60 / (2 * PI * fs * 0.707);
  int samplesRequired =((int)ceil(ts/dt));
  return samplesRequired;
}
/* @} */
