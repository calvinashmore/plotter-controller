/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.io;

import com.icosilune.arduino.rpc.SerialController;
import com.icosilune.plottercontroller.data.DataChannel;
import com.icosilune.plottercontroller.data.DataPoint;
import com.icosilune.plottercontroller.data.Plot;
import com.icosilune.plottercontroller.data.PlotDataIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 *
 * @author ashmore
 */
public class PlotWriter {
  private static final Logger LOG = Logger.getLogger( PlotWriter.class.getName() );
  
  private static final long SLEEP_TIME = 10;
  
  private final SerialController serialController;
  private final PlotDataIterator dataIterator;
  private final ExecutorService executor;
  private final ProgressListener progressListener;
  
  long currentProgress = -1;
  boolean isStarted;
  boolean isPaused;

  public PlotWriter(SerialController serialController, Plot plot, ProgressListener progressListener) {
    this.serialController = serialController;
    this.dataIterator = new PlotDataIterator(plot);
    this.executor = Executors.newSingleThreadExecutor();
    this.progressListener = progressListener;
  }
  
  public void start() {
    if (isStarted) {
      throw new IllegalStateException("Writer is already started");
    }
    LOG.info("Starting");
    isStarted = true;
    
    executor.submit(this::run);
  }
  
  public void stop() {
    if(!isStarted) {
      throw new IllegalStateException("Writer has not been started");
    }
    LOG.info("Stopping...");
    executor.shutdownNow();
  }
  
  private void run() {
    // Request the handshake to start
    progressListener.update(dataIterator.getProgress());

    try {
      while (dataIterator.hasNext()) {
        DataPoint next = dataIterator.next();
        long dataIndex = dataIterator.getDataIndex();
        sendData(dataIndex, next);
        progressListener.update(dataIterator.getProgress());
        while(isPaused || currentProgress < dataIndex) {
          Thread.sleep(SLEEP_TIME);
        }
      }
    } catch (InterruptedException ex) {
      LOG.warning("Interrupted while streaming data!");
    }
    LOG.info("Finished!!!");
    serialController.disconnect();
  }
  
  public void pause() {
    isPaused = true;
  }
  
  public void unpause() {
    isPaused = false;
  }
  
  void sendData(long dataIndex, DataPoint point) {
    serialController.sendCommand(
        "handleData",
        dataIndex,
        (float) point.get(DataChannel.POSITION_X),
        (float) point.get(DataChannel.POSITION_Y),
        (float) point.get(DataChannel.SPEED),
        (float) point.get(DataChannel.PRESSURE_Z),
        (float) point.get(DataChannel.PITCH),
        (float) point.get(DataChannel.YAW));
  }
  
  public void handleProgress(long data) {
    currentProgress = data;
  }

  public boolean isPaused() {
    return isPaused;
  }
  
  @FunctionalInterface
  public interface ProgressListener {
    public void update(PlotDataIterator.PlotProgress progress);
  }
}
