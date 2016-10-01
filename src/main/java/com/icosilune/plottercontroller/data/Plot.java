/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents all the curves / strokes representing a single plotter run.
 */
public class Plot {
  private final ImmutableList<Stroke> strokes;
  private Map<DataChannel, ChannelTransform> channelTransforms;

  public Plot(Iterable<Stroke> strokes) {
    this.strokes = ImmutableList.copyOf(strokes);
    channelTransforms = ImmutableMap.of();
  }
  
  public ChannelTransform getChannelTransform(DataChannel channel) {
    return channelTransforms.getOrDefault(channel, ChannelTransform.identity());
  }
  
  public void setTransforms(Map<DataChannel, ChannelTransform> transforms) {
    this.channelTransforms = transforms;
  }

  public ImmutableList<Stroke> getStrokes() {
    return strokes;
  }
  
  public Extents getExtents() {
    return Extents.union(getStrokes().stream().map(Stroke::getExtents).collect(Collectors.toList()));
  }
}
