/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

/**
 * Channel of a data point used in a Stroke
 */
public enum DataChannel {
  // **** Consider using a raw proto format.
  // Can arduino decipher protos?
  // https://github.com/nanopb/nanopb ???
  // The extra structure offered may not be helpful
  POSITION_X,
  POSITION_Y,
  PRESSURE_Z,
  PITCH,
  YAW,
}
