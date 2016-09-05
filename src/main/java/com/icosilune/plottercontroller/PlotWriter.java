/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import com.google.common.base.Strings;
import java.util.Iterator;

/**
 * Writes plot data to serial output.
 * Has mutable state that advances as we move through the points.
 */
public class PlotWriter implements Iterator<String> {
  
  // 1st index is the data index, remainder are values according to DataChannel
  private static final String FORMAT =
      "%d"+ Strings.repeat(" %f", DataChannel.values().length);

  // Output format is formatted string.
  private final Plot plot;
  
  // May want to have a specific class to represent this???
  private int strokeIndex = 0;
  private int pointIndex = 0; // index of point inside the stroke
  private long dataIndex = 0; // index of all data
  private State currentState = State.PRE_START;
  private Stroke currentStroke;

  // Might be nice to have accessors to power a UI or something.
  
  private enum State {
    PRE_START,
    POSITIONING,
    STROKE,
    DONE,
  }

  public PlotWriter(Plot plot) {
    this.plot = plot;
  }
  
  @Override
  public boolean hasNext() {
    return currentState != State.DONE;
  }
  
  @Override
  public String next() {
    // how to handle space in between strokes??
    DataPoint point;
    switch(currentState) {
      case PRE_START:
        startPositioning();
        point = DataPoint.ORIGIN;
      case POSITIONING:
        // Not quite sure what we want to do here.
        // As written, we'll get the 0 point twice. Maybe that's okay.
        point = applyTransforms(currentStroke.getPoint(0));
        break;
      case STROKE:
        point = applyTransforms(currentStroke.getPoint(pointIndex));
        
        pointIndex++;
        if(pointIndex >= currentStroke.getNumberDataPoints()) {
          strokeIndex++;
          if(strokeIndex >= plot.getStrokes().size()) {
            currentState = State.DONE;
          } else {
            startPositioning();
          }
        }
        break;
      case DONE:
        throw new IllegalStateException("no more");
      default:
        throw new IllegalArgumentException("unrecognized state "+currentState);
    }
    dataIndex++;
    return formatPoint(point);
  }
  
  private DataPoint applyTransforms(DataPoint point) {
    return channel -> plot.getChannelTransforms().get(channel).apply(point.get(channel));
  }
  
  // Formats the data point to a string used for output.
  // We expect the point to already be transformed if it needs to be.
  private String formatPoint(DataPoint point) {
    Object[] values = new Object[1 + DataChannel.values().length];
    values[0] = dataIndex;
    for(DataChannel channel : DataChannel.values()) {
      values[channel.ordinal() + 1] = point.get(channel);
    }

    return String.format(FORMAT, values);
  }
  
  private void startPositioning() {
    currentState = State.POSITIONING;
    currentStroke = plot.getStrokes().get(strokeIndex);
    pointIndex = 0;
//    currentStroke.getPoint(0);
  }
  
}
