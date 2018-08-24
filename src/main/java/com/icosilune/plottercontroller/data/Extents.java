/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author ashmore
 */
public class Extents {
  private final ImmutableMap<DataChannel, Range<Double>> values;

  public Extents(Map<DataChannel, Range<Double>> values) {
    this.values = ImmutableMap.copyOf(values);
  }

  //@Nullable
  public Range<Double> get(DataChannel channel) {
    return values.get(channel);
  }
  
  public static Extents union(Iterable<Extents> extents) {
    Map<DataChannel, Range<Double>> values = new HashMap<>();
    for(Extents extent: extents) {
      for(Map.Entry<DataChannel, Range<Double>> entry : extent.values.entrySet()) {
        if(!values.containsKey(entry.getKey())) {
          values.put(entry.getKey(), entry.getValue());
        } else {
          Range<Double> existingRange = values.get(entry.getKey());
          Range<Double> newRange = entry.getValue();
          
          values.put(entry.getKey(), Range.encloseAll(
              ImmutableList.of(
                  existingRange.lowerEndpoint(),
                  existingRange.upperEndpoint(),
                  newRange.lowerEndpoint(),
                  newRange.upperEndpoint())));
        }
      }
    }
    return new Extents(values);
  }
}
