/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that implements a kalman
 * filter
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

public class KalmanFilter {
 private float q;
 private float r;
 private float x;
 private float p;
 private float k;

 /**
  * @brief Class constructor
  * @param sensor_noise The measurement noise covariance
  * @param estimated_error The estimation error covariance
  * @param process_noise The process noise covariance
  */
 public KalmanFilter(float sensor_noise, float estimated_error, float process_noise) {
  this.q = process_noise;
  this.r = sensor_noise;
  this.p = estimated_error;
  this.x = 0;
 }

 /**
  * @brief Kalman gain calculation
  */
 private void calcKalmanGain() {
  this.k = this.p / (this.p + this.r);
 }

 /**
  * @brief Covariance Extrapolation calculation
  */
 private void calcPredictorCovarianceEq() {
  this.p = this.p + this.q;
 }

 /**
  * @brief Covariance update calculation
  */
 private void calcCorrectorEq() {
  this.p = (1 - this.k)* this.p;
 }

 /**
  * @brief Calculate State update equation
  */
 public void calcStateUpdate(float counts) {
  this.x = this.x + this.k * (counts - this.x);
 }

 /**
  * @brief Remove noise from counts with the use of an Kalman filter
  * @param counts Current value in counts
  * @return The corrected estimation
  */
 public float updateEstimation(float counts) {
  calcPredictorCovarianceEq();
  calcKalmanGain();
  calcStateUpdate(counts);
  calcCorrectorEq();
  return this.x;
 }
}