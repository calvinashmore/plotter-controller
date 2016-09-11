/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author ashmore
 */
class SerialController {
  private static final Logger LOG = Logger.getLogger( SerialController.class.getName() );

  // Another condition to keep track of: what if the serial port is closed?
  // e.g., becomes unplugged
  private SerialPort serialPort;
  private DataListener dataListener;
  
  @FunctionalInterface
  public interface DataListener {
    void handleData(long data);
  }
  
  void setDataListener(DataListener dataListener) {
    this.dataListener = dataListener;
  }

  SerialController(){
//    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//      disconnect();
//    }));
  }
  
  public void disconnect() {
    try {
      if (isConnected()) {
        LOG.info("Closing serial");
        serialPort.closePort();
      } else {
        LOG.info("Already disconnected");
      }
    } catch (SerialPortException ex) {
      LOG.log(Level.SEVERE, "Caught serial exception", ex);
    }
  }
  
  public boolean isConnected() {
    return serialPort != null && serialPort.isOpened();
  }
  
  public boolean connect() {
    if (serialPort != null) {
      LOG.warning("Serial is already open.");
      return false;
    }

    try {
      List<String> portNames = Arrays.asList(SerialPortList.getPortNames());
      if (portNames.isEmpty()) {
        LOG.warning("No serial ports available!");
        return false;
      }
      LOG.log(Level.INFO, "Available serial ports: {0}. Choosing first", portNames);
      String portName = portNames.get(0);
      serialPort = new SerialPort(portName);

      if (!serialPort.openPort()) {
        LOG.warning("Could not open port");
        return false;
      }
      if (!serialPort.setParams(9600, 8, 1, 0)) {
        LOG.warning("Could not set params");
        return false;
      }

      serialPort.addEventListener((SerialPortEvent event) -> {
        if (event.isRXCHAR() && event.getEventValue() > 0) {
          try {
            byte[] read = serialPort.readBytes(Long.BYTES);
            long response = 
                ByteBuffer.wrap(read).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().get();
            LOG.log(Level.INFO, "Serial read: {0} ({1})", new Object[] {response, bytesToHex(read)});
            dataListener.handleData(response);
          } catch (SerialPortException ex) {
            LOG.log(Level.WARNING, "Caught serial exception", ex);
          }
        }
      });

      return true;
    } catch (SerialPortException ex) {
      LOG.log(Level.WARNING, "Caught serial exception", ex);
      return false;
    }
  }
  
  public void writeData(byte[] data) {
    if (!isConnected()) {
      throw new IllegalStateException("port not open");
    }
    try {
      LOG.log(Level.INFO, "Writing {0}", bytesToHex(data));
      serialPort.writeBytes(data);
    } catch (SerialPortException ex) {
      LOG.log(Level.WARNING, "Caught serial exception", ex);
    }
  }

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  private static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for ( int j = 0; j < bytes.length; j++ ) {
        int v = bytes[j] & 0xFF;
        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }
}
