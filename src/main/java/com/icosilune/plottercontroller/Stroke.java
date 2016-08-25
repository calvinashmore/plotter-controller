/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import java.util.Map;

/**
 * Represents the data needed to express a stroke on the canvas. Contains XY data + all extra metadata that describes
 * pen pressure, angles, etc.
 */
//@AutoValue
public class Stroke {
  // # of data points that must be streamed to the plotter.
  private final int numberDataPoints;
  private final Map<DataChannel, double[]> dataBuffers;
  
  public Stroke(Map<DataChannel, double[]> dataBuffers) {
    // Note: not doing a defensive copy. That may be a good idea, though
    this.dataBuffers = dataBuffers;
    // Just pull the first one; assume they're valid for now.
    this.numberDataPoints = dataBuffers.values().iterator().next().length;
  }
  
  public int getNumberDataPoints() {
    return numberDataPoints;
  }
  
  public double readData(int point, DataChannel channel) {
    return dataBuffers.get(channel)[point];
  }
  
  // non static
  // is there a better way of doing this????
//  public class DataPoint {
//    private final int index;
//
//    DataPoint(int index) {
//      this.index = index;
//    }
//    
//    public double getValue(DataChannel channel) {
//      return dataBuffers.get(channel)[index];
//    }
//  }
}
