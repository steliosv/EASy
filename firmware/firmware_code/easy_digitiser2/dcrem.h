/** @file ADS1256.h
  @brief E.A.SY. Digitizer.

  This contains the Header file for the dc offset removal class

  @author Stelios Voutsinas (stevo)
  @bug No known bugs.
*/
#ifndef dcrem_h
#define dcrem_h

class DC
{

  public:
    DC(void);
    long autoRemoveDCConst (long curr);
    int getTSettle(int sps);



  private:
    const float ALPHA = 0.9;
    long lastInput  = 0;
    long lastOutput = 0;

};

#endif
