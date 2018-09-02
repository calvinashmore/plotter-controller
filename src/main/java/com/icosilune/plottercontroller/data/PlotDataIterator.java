/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data;

import com.google.auto.value.AutoValue;
import java.util.Iterator;

/**
 * Writes plot data to serial output.
 * Has mutable state that advances as we move through the points.
 */
public class PlotDataIterator implements Iterator<DataPoint> {

  // Output format is formatted string.
  private final Plot plot;
  
  // May want to have a specific class to represent this???
  private int strokeIndex = 0;
  private int pointIndex = 0; // index of point inside the stroke
  private long dataIndex = 0; // index of all data
  private State currentState = State.PRE_START;
  private Stroke currentStroke;
  private DataPoint currentPoint = DataPoint.ORIGIN;

  // Might be nice to have accessors to power a UI or something.
  
  public enum State {
    PRE_START,
    POSITIONING,
    STROKE,
    DONE,
  }
  
  public PlotDataIterator(Plot plot) {
    this.plot = plot;
  }
  
  @Override
  public boolean hasNext() {
    return currentState != State.DONE;
  }
  
  public State getState() {
    return currentState;
  }
  
  @Override
  public DataPoint next() {
    // how to handle space in between strokes??
    switch(currentState) {
      case PRE_START:
        startPositioning();
        currentPoint = DataPoint.ORIGIN;
        break;
      case POSITIONING:
        // Not quite sure what we want to do here.
        // As written, we'll get the 0 point twice. Maybe that's okay.
        currentPoint = applyTransforms(currentStroke.getPoint(0));
        currentState = State.STROKE;
        break;
      case STROKE:
        currentPoint = applyTransforms(currentStroke.getPoint(pointIndex));
        
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
    return currentPoint;
  }
  
  @AutoValue
  public static abstract class PlotProgress {
    public abstract int getPointIndex();
    public abstract int getTotalPoints();
    public abstract int getStrokeIndex();
    public abstract int getTotalStrokes();
    public abstract DataPoint getPoint();
    public abstract State getState();
    
    public double getStrokeProgress() {
      return getPointIndex() / (double) getTotalPoints();
    }
    
    public double getTotalProgress() {
      return getStrokeIndex() / (double) getTotalStrokes();
    }

    static PlotProgress create(int pointIndex, int totalPoints, int strokeIndex, int totalStrokes, DataPoint point, State state) {
      return new AutoValue_PlotDataIterator_PlotProgress(pointIndex, totalPoints, strokeIndex, totalStrokes, point, state);
    }
  }
  
  public PlotProgress getProgress() {
    return PlotProgress.create(
        pointIndex, 
        currentStroke == null ? 1 : currentStroke.getNumberDataPoints(),
        strokeIndex,
        plot.getStrokes().size(),
        currentPoint,
        currentState);
  }

//  public double getStrokeProgress() {
//    return pointIndex / (double) currentStroke.getNumberDataPoints();
//  }
//
//  public double getTotalProgress() {
//    return strokeIndex / (double) plot.getStrokes().size();
//  }

  public long getDataIndex() {
    return dataIndex;
  }
  
  private DataPoint applyTransforms(DataPoint point) {
    return channel -> plot.getChannelTransform(channel).apply(point.get(channel));
  }
  
  private void startPositioning() {
    currentState = State.POSITIONING;
    currentStroke = plot.getStrokes().get(strokeIndex);
    pointIndex = 0;
  }
  
}
