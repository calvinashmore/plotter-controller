/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

/**
 *
 * @author ashmore
 */
@FunctionalInterface
public interface DataPoint {
  public static final DataPoint ORIGIN = channel -> 0;
  
  double get(DataChannel channel);
}
