/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data;

/**
 * Channel of a data point used in a Stroke
 */
public enum DataChannel {
  // Units here are going to be tricky. Standardizing these may be dependent on the way the motors
  // and stuff are configured on the plotter.
  
  // X & Y positions regard 0,0 as the origin of the plot. Negative values should be invalid.
  // Not sure what the x&y limits should be. Might be nice to have this actually measure out mm or
  // something.
  POSITION_X("Position X"),
  POSITION_Y("Position Y"),
  
  // Values should be >= 0. Zero speed might be used to create a spot or pause. But may be unlikely
  SPEED("Speed"),
  
  // Not sure what we want this to be. 0 for fully up. 100? for fully down?
  PRESSURE_Z("Pressure"),
  
  // Angle (degrees). 0 for vertical. Otherwise should be angle in degrees.
  // all the way on pitch will eject the pen.
  PITCH("Pitch"),
  
  // Angle (degrees). Full 360 freedom is possible. Including negative values and wrapping.
  YAW("Yaw"),
  ;
  
  private final String niceName;

  private DataChannel(String niceName) {
    this.niceName = niceName;
  }
  
  public String getNiceName() {
    return niceName;
  }
}
