/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.icosilune.plottercontroller.data.DataChannel;
import com.icosilune.plottercontroller.data.Stroke;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author ashmore
 */
public abstract class FunctionalSingleStrokeGenerator extends SingleStrokeGenerator {
  private final int plotPoints;
  private final Map<DataChannel, Function<Double, Double>> functions;

  public FunctionalSingleStrokeGenerator(int plotPoints, Map<DataChannel, Function<Double, Double>> functions) {
    this.plotPoints = plotPoints;
    
    ImmutableMap.Builder<DataChannel, Function<Double, Double>> builder = ImmutableMap.builder();
    
    Preconditions.checkArgument(functions.containsKey(DataChannel.POSITION_X));
    Preconditions.checkArgument(functions.containsKey(DataChannel.POSITION_Y));
    
    builder.put(DataChannel.POSITION_X, functions.get(DataChannel.POSITION_X));
    builder.put(DataChannel.POSITION_Y, functions.get(DataChannel.POSITION_Y));
    builder.put(DataChannel.SPEED, functions.getOrDefault(DataChannel.SPEED, t -> 9.0));
    builder.put(DataChannel.SPEED, functions.getOrDefault(DataChannel.SPEED, t -> 9.0));
    
    this.functions = builder.build();
  }

  @Override
  Stroke generateStroke() {
    Map<DataChannel, double[]> dataBuffers = new LinkedHashMap<>();
    
    
    
    return new Stroke(dataBuffers)
  }
  
  
}
