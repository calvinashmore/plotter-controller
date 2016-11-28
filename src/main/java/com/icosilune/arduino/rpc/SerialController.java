/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.arduino.rpc;

import com.google.common.base.Splitter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author ashmore
 */
public class SerialController {
  private static final Logger LOG = Logger.getLogger( SerialController.class.getName() );

  private final SerialPortFactory serialPortFactory;
  private final Map<String, CommandListener> commandListeners = new LinkedHashMap<>();

  // Another condition to keep track of: what if the serial port is closed?
  // e.g., becomes unplugged
  private SerialPort serialPort;

  public SerialController() {
    this(new SerialPortFactory());
  }

  SerialController(SerialPortFactory serialPortFactory) {
    this.serialPortFactory = serialPortFactory;
  }
  
  static class SerialPortFactory {
    public SerialPort create() {
      List<String> portNames = Arrays.asList(SerialPortList.getPortNames());
      if (portNames.isEmpty()) {
        LOG.warning("No serial ports available!");
        return null;
      }
      LOG.log(Level.INFO, "Available serial ports: {0}. Choosing first", portNames);
      String portName = portNames.get(0);
      return new SerialPort(portName);
    }
  }
  
  public void addListener(CommandListener listener) {
    commandListeners.put(listener.getName(), listener);
  }
  
  public void disconnect() {
    try {
      if (isConnected()) {
        LOG.info("Closing serial");
        if(!serialPort.closePort()) {
          LOG.warning("Could not close port");
        } else {
          serialPort = null;
        }
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
      serialPort = serialPortFactory.create();

      if (!serialPort.openPort()) {
        LOG.warning("Could not open port");
        return false;
      }
      if (!serialPort.setParams(9600, 8, 1, 0)) {
        LOG.warning("Could not set params");
        return false;
      }

      serialPort.addEventListener(new SerialPortEventListenerImpl());

      return true;
    } catch (SerialPortException ex) {
      LOG.log(Level.WARNING, "Caught serial exception", ex);
      return false;
    }
  }
  
  public void sendCommand(String commandName, Object... args) {
    StringBuilder sb = new StringBuilder();
    sb.append(commandName).append(" ");
    for(Object arg : args) {
      Type t = Type.forClass(arg.getClass());
      sb.append(t.encode(arg));
    }
    
    LOG.log(Level.INFO, "Sent {0}", sb.toString());
    
    sb.append("\n");
    
    writeData(sb.toString());
  }
  
  private void writeData(String data) {
    if (!isConnected()) {
      throw new IllegalStateException("port not open");
    }
    try {
      serialPort.writeString(data);
    } catch (SerialPortException ex) {
      LOG.log(Level.WARNING, "Caught serial exception", ex);
    }
  }

  private void executeCommand(String command) {
    Iterator<String> split = Splitter.on(" ").omitEmptyStrings().trimResults().split(command).iterator();
    String commandName = split.next();
    String commandArgs = "";
    if(split.hasNext()) {
      commandArgs = split.next();
    }
    
    CommandListener listener = commandListeners.get(commandName);
    if (listener == null) {
      LOG.log(Level.WARNING, "Unknown command {0}", commandName);
      return;
    }
    listener.execute(commandArgs);
  }

  private class SerialPortEventListenerImpl implements SerialPortEventListener {

    private final Charset charset = Charset.forName("US-ASCII");
//    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    String buffer = "";

    @Override
    public void serialEvent(SerialPortEvent event) {
      if (event.isRXCHAR() && event.getEventValue() > 0) {
        try {
          
          buffer += new String(serialPort.readBytes(), charset);
          
          while(buffer.indexOf('\n') > 0) {
            int newline = buffer.indexOf('\n');
            String command = buffer.substring(0, newline);
            executeCommand(command);
            buffer = buffer.substring(newline+1);
          }
          
//          buffer.put(serialPort.readBytes());
          // position += byte length
          // limit += byte length

//          String s = new String(buffer.array(),0,buffer.limit(), charset);
//          if(s.indexOf('\n') > 0) {
//            int newline = s.indexOf('\n');
//            String command = s.substring(0, newline);
//            executeCommand(command);
//            
//            // filled data in buffer: [0, limit]
//            // command: [0, newline]
//            // want to copy [newline, limit] to 0
//            // and set position to limit - newline
//            System.arraycopy(buffer.array(), newline, buffer.array(), 0, buffer.limit() - newline);
//            int limit = buffer.limit();
//            buffer.position(limit - newline);
//            
//          } else {
//            // keep adding to the buffer.
////            int limit = buffer.limit();
////            buffer.position(limit);
//          }

          // ISSUES:
          // 1) reportProgress is not sending its argument
          // 2) we're parsing the serial data badly, and throwing out all but the first thing that's read
          // 3) we're receiving serial data from the previous run. There's no serial clearing.
          
//          String string = new String(buffer.array(), charset);
//          if(string.indexOf('\n') > 0) {
//            String command = string.substring(0, string.indexOf('\n'));
//            executeCommand(command);
//            Arrays.fill(buffer.array(), (byte) 0);
//            buffer.clear();
//            LOG.log(Level.INFO, "Received {0}", command);
//          }

        } catch (SerialPortException ex) {
          LOG.log(Level.WARNING, "Caught serial exception", ex);
        }
      }
    }
  }
}
