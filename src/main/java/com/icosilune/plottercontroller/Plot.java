/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import com.google.auto.value.AutoValue;
import java.util.List;

/**
 * Represents all the curves / strokes representing a single plotter run.
 */
@AutoValue
public abstract class Plot {
  public abstract List<Stroke> getStrokes();
  
  // *** Also need something for transformation of data points:
  // public abstract Map<DataChannel, PointXForm> getPointXForms(); ??????
  
  public static Plot create(List<Stroke> strokes) {
    return new AutoValue_Plot(strokes);
  }
}
