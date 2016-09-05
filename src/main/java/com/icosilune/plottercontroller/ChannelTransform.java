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
public interface ChannelTransform {
  double apply(double x);
  
  public static ChannelTransform identity() {
    return x -> x;
  }
  
  public static ChannelTransform affine(double offset, double scale) {
    return x -> offset + scale * x;
  }
}
