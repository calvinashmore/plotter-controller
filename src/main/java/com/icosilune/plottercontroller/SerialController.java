/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;

/**
 *
 * @author ashmore
 */
class SerialController {
  
  // Another condition to keep track of: what if the serial port is closed?
  // e.g., becomes unplugged
  private final SerialPort serialPort;
  private final ExecutorService executor =
      Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
          .setDaemon(false)
          .setNameFormat("controller-%d")
          .build());
  // consider making a constant or something else
  private final int itemsToWrite = 5;
  private final Iterator<String> items;
  // The total number of lines successfully handled by the SerialPort
  private int currentProgress = 0;
  // The total number of lines written to the SerialPort
  private int currentWritten = 0;
  private boolean paused = false;

  SerialController(SerialPort serialPort, Iterator<String> items) {
    this.serialPort = serialPort;
    this.items = items;
  }

  public Future<?> start() {
    try {
      serialPort.addEventListener((SerialPortEvent event) -> {
        if (event.isRXCHAR() && event.getEventValue() > 0) {
          try {
            String data = serialPort.readString(event.getEventValue()).trim();
            if (data.isEmpty()) {
              return;
            }
            System.out.println("Serial read: " + data);
            int parsedData = Integer.valueOf(data);
            // ??
            currentProgress = parsedData;
          } catch (SerialPortException ex) {
            ex.printStackTrace();
          }
        }
      });
    } catch (SerialPortException ex) {
      throw new RuntimeException(ex);
      // Can't attach listener
    }
    return executor.submit(this::run);
  }

  public void shutdown() {
    // shuts down and interrrupts the task
    executor.shutdownNow();
  }

  private void run() {
    try {
      while (true) {
        Thread.sleep(100);
        if (!paused && currentWritten - currentProgress < itemsToWrite) {
          try {
            if (!items.hasNext()) {
              return;
            }
            serialPort.writeString(items.next());
            currentWritten++;
          } catch (SerialPortException ex) {
            // now what?
            System.out.println("Got a serial exception. Going to pause");
            ex.printStackTrace();
            paused = true;
          }
        }
      }
    } catch (InterruptedException ex) {
      // just exit normally
    }
  }
  
}
