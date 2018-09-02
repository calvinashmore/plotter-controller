/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data.generator;

import com.google.common.collect.ImmutableList;
import com.icosilune.plottercontroller.data.Plot;
import com.icosilune.plottercontroller.data.Stroke;

/**
 *
 * @author ashmore
 */
public abstract class SingleStrokeGenerator implements PlotGenerator {

  abstract Stroke generateStroke();
  
  @Override
  public Plot generate() {
    return new Plot(ImmutableList.of(generateStroke()));
  }
  
}
