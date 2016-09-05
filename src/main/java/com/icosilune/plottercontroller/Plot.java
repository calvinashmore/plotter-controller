/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import com.google.auto.value.AutoValue;
import com.google.common.base.Functions;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents all the curves / strokes representing a single plotter run.
 */
@AutoValue
public abstract class Plot {
  public abstract List<Stroke> getStrokes();
  // Data transformation of each channel
  public abstract Map<DataChannel, ChannelTransform> getChannelTransforms();
  
  public static Plot create(List<Stroke> strokes) {
    return create(strokes, Arrays.stream(DataChannel.values()).collect(Collectors.toMap(
        x -> x,
        (DataChannel x) -> ChannelTransform.identity())));
  }

  public static Plot create(List<Stroke> strokes, Map<DataChannel, ChannelTransform> channelTransforms) {
    return new AutoValue_Plot(strokes, channelTransforms);
  }
}
