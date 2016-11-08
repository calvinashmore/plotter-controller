/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.io;

import com.icosilune.arduino.rpc.CommandListener;
import com.icosilune.arduino.rpc.SerialController;
import com.icosilune.arduino.rpc.Type;

/**
 *
 * @author ashmore
 */
public class PlotterController {
  private final SerialController serialController;
  
  public interface PlotterListener {
    public void reportProgress(long progress);
  }
  
  public PlotterController(SerialController serialController, PlotterListener listener) {
    this.serialController = serialController;
    
    serialController.addListener(new CommandListener("reportProgress", Type.LONG) {
      @Override
      public void process(Object[] args) {
        listener.reportProgress((long) args[0]);
      }
    });
  }

  /** Enable or disable the plotter */
  public void enable(boolean enabled) {
    serialController.sendCommand("enable", enabled ? 1 : 0);
  }
  
  public void moveXY(float x, float y) {
    serialController.sendCommand("moveXY", x, y);
  }
  
  public void moveAxis(int axis, float value) {
    serialController.sendCommand("moveAxis", axis, value);
  }
}
