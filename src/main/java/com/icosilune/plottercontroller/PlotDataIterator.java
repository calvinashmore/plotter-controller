/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import com.google.common.base.Strings;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

  // Might be nice to have accessors to power a UI or something.
  
  private enum State {
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
  
  @Override
  public DataPoint next() {
    // how to handle space in between strokes??
    DataPoint point;
    switch(currentState) {
      case PRE_START:
        startPositioning();
        point = DataPoint.ORIGIN;
        break;
      case POSITIONING:
        // Not quite sure what we want to do here.
        // As written, we'll get the 0 point twice. Maybe that's okay.
        point = applyTransforms(currentStroke.getPoint(0));
        currentState = State.STROKE;
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
    return point;
  }

  public long getDataIndex() {
    return dataIndex;
  }
  
  private DataPoint applyTransforms(DataPoint point) {
    return channel -> plot.getChannelTransforms().get(channel).apply(point.get(channel));
  }
  
//  public static final int PACKET_SIZE = 64;
//  private static final int VERIFIER = 0xDEADBEEF;
  
  // Formats the data point to a string used for output.
  // We expect the point to already be transformed if it needs to be.
//  private byte[] formatPoint(DataPoint point) {
//    ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.LITTLE_ENDIAN);
//    
//    buffer.putLong(dataIndex);
//    
//    for(DataChannel channel : DataChannel.values()) {
//      buffer.putFloat((float) point.get(channel));
//    }
//    buffer.putInt(VERIFIER);
//
//    return buffer.array();
//  }
  
  private void startPositioning() {
    currentState = State.POSITIONING;
    currentStroke = plot.getStrokes().get(strokeIndex);
    pointIndex = 0;
  }
  
}
