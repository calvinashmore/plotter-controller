/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 *
 * @author ashmore
 */
public class PlotWriter implements SerialController.DataListener {
  private static final Logger LOG = Logger.getLogger( SerialController.class.getName() );
  
  private static final int START_BLOCK = 0xDEADBEEF;
  private static final int VERIFIER = 0x777777A7;
  private static final long HANDSHAKE_CONFIRM = 0x7887655653351FF1L;
  private static final long SLEEP_TIME = 10;
  
  private final SerialController serialController;
  private final PlotDataIterator dataIterator;
  private final Executor executor;
  
  long currentProgress = -1;
  boolean isStarted;
  boolean isPaused;
  boolean hasHandshake;

  PlotWriter(SerialController serialController, PlotDataIterator dataIterator, Executor executor) {
    this.serialController = serialController;
    this.dataIterator = dataIterator;
    this.executor = executor;
    
    serialController.setDataListener(this);
  }
  
  public void start() {
    if (isStarted) {
      throw new IllegalStateException("Writer is already started");
    }
    LOG.info("Starting");
    isStarted = true;
    executor.execute(this::run);
  }
  
  private void run() {
    // Request the handshake to start
    handshake();

    try {
      while (dataIterator.hasNext()) {
        DataPoint next = dataIterator.next();
        long dataIndex = dataIterator.getDataIndex();
        if (!isPaused && currentProgress < dataIndex) {
          serialController.writeData(formatPoint(dataIndex, next));
        }

        Thread.sleep(SLEEP_TIME);
      }
    } catch (InterruptedException ex) {
      LOG.warning("Interrupted while streaming data!");
    }
    LOG.info("Finished!!!");
  }

  private byte[] formatPoint(long dataIndex, DataPoint point) {

    int baseSize = 2 * Integer.BYTES + Long.BYTES + DataChannel.values().length * Float.BYTES;
    ByteBuffer byteBuffer = ByteBuffer.allocate(baseSize).order(ByteOrder.LITTLE_ENDIAN);

    byteBuffer.putInt(START_BLOCK);
    byteBuffer.putLong(dataIndex);
    for (DataChannel channel : DataChannel.values()) {
      byteBuffer.putFloat((float) point.get(channel));
    }
    byteBuffer.putInt(VERIFIER);

    return byteBuffer.array();
  }
  
  public void pause() {
    isPaused = true;
  }
  
  public void unpause() {
    isPaused = false;
  }
  
  /**
   * Executes handshake, and blocks until it occurs. This may need to get called more than once if the microcontroller is reset.
   */
  public void handshake() {
    hasHandshake = false;
    LOG.info("Initiating handshake");
    try {
      while (!hasHandshake) {
        ByteBuffer bytes = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        bytes.putLong(HANDSHAKE_CONFIRM);
        serialController.writeData(bytes.array());
        Thread.sleep(SLEEP_TIME);
      }
    } catch (InterruptedException ex) {
      LOG.warning("Interrupted while executing handshake!");
    }
    LOG.info("Handshake completed!");
  }

  @Override
  public void handleData(long data) {
    if(data == HANDSHAKE_CONFIRM) {
      hasHandshake = true;
      return;
    }
    
    if(hasHandshake) {
      currentProgress = data;
    }
  }
}
