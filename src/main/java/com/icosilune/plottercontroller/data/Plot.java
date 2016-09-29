/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data;

import com.google.auto.value.AutoValue;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Range;
import com.sun.istack.internal.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
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
  
  @Nullable
  public Range<Double> getExtents(DataChannel channel) {
    return getStrokes()
        .stream()
        .map(s -> s.getExtents(channel))
        .filter(Objects::nonNull)
        .reduce((a,b) -> a.span(b) )
        .orElse(null);
  }
}
