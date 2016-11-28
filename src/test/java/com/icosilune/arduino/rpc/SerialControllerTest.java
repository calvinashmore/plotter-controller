/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.arduino.rpc;

import com.google.common.truth.Truth;
import java.io.UnsupportedEncodingException;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author ashmore
 */
@RunWith(MockitoJUnitRunner.class)
public class SerialControllerTest {
  
  @Mock private SerialPort serialPort;
  @Mock private SerialController.SerialPortFactory serialPortFactory;
  @Captor private ArgumentCaptor<SerialPortEventListener> eventListenerCaptor;
  
  private SerialController controller;
  
  @Before
  public void setup() throws SerialPortException {
    Mockito.doReturn(serialPort).when(serialPortFactory).create();
    controller = new SerialController(serialPortFactory);
    
    Mockito.when(serialPort.openPort()).thenReturn(true);
    Mockito.when(serialPort.setParams(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
    Mockito.doNothing().when(serialPort).addEventListener(eventListenerCaptor.capture());
    Mockito.when(serialPort.isOpened()).thenReturn(true);
  }
  
  @Test
  public void testSendData() throws SerialPortException {
    
    controller.connect();
    controller.sendCommand("hello", -1, 0.1f);
    
    Mockito.verify(serialPort).writeString("hello FFFFFFFF3DCCCCCD\n");
  }
  
  @Test
  public void testReceiveData() throws SerialPortException, UnsupportedEncodingException {
    abstract class ListenerObject {
      abstract void doStuff(int a, float b, long c);
    }
    class FakeListener extends CommandListener {
      private final ListenerObject obj;
      FakeListener(String name, ListenerObject obj) {
        super(name, Type.INT, Type.FLOAT, Type.LONG);
        this.obj = obj;
      }

      @Override
      public void process(Object[] args) {
        Truth.assertThat(args.length).isEqualTo(3);
        int a = (int) args[0];
        float b = (float) args[1];
        long c = (long) args[2];
        obj.doStuff(a, b, c);
      }
    }
    
    ListenerObject listenerObj = Mockito.mock(ListenerObject.class);
    FakeListener commandListener = new FakeListener("hello", listenerObj);
    
    controller.connect();
    controller.addListener(commandListener);
    SerialPortEventListener listener = eventListenerCaptor.getValue();

    // https://www.h-schmidt.net/FloatConverter/IEEE754.html
    byte[] bytes = "hello FFFFFFFF3DCCCCCD0000000000000000\n".getBytes("US-ASCII");
    Mockito.when(serialPort.readBytes()).thenReturn(bytes);

    listener.serialEvent(new SerialPortEvent("fakePortName", SerialPortEvent.RXCHAR, 10));
    
    Mockito.verify(listenerObj).doStuff(-1, 0.1f, 0L);
  }
  
  @Test
  public void testReceiveData_partial() throws SerialPortException, UnsupportedEncodingException {
    abstract class ListenerObject {
      abstract void doStuff(int a);
    }
    class FakeListener extends CommandListener {
      private final ListenerObject obj;
      FakeListener(String name, ListenerObject obj) {
        super(name, Type.INT);
        this.obj = obj;
      }

      @Override
      public void process(Object[] args) {
        Truth.assertThat(args.length).isEqualTo(1);
        obj.doStuff((int) args[0]);
      }
    }
    
    ListenerObject listenerObj = Mockito.mock(ListenerObject.class);
    FakeListener commandListener = new FakeListener("hello", listenerObj);
    
    controller.connect();
    controller.addListener(commandListener);
    SerialPortEventListener listener = eventListenerCaptor.getValue();

    byte[] bytes = "he".getBytes("US-ASCII");
    Mockito.when(serialPort.readBytes()).thenReturn(bytes);
    listener.serialEvent(new SerialPortEvent("fakePortName", SerialPortEvent.RXCHAR, 10));
    Mockito.verifyZeroInteractions(listenerObj);
    
    bytes = "llo FFFFFFFF\nhello ".getBytes("US-ASCII");
    Mockito.when(serialPort.readBytes()).thenReturn(bytes);
    listener.serialEvent(new SerialPortEvent("fakePortName", SerialPortEvent.RXCHAR, 10));
    Mockito.verify(listenerObj).doStuff(-1);
    
    bytes = "000000FF ".getBytes("US-ASCII");
    Mockito.when(serialPort.readBytes()).thenReturn(bytes);
    listener.serialEvent(new SerialPortEvent("fakePortName", SerialPortEvent.RXCHAR, 10));
    Mockito.verifyNoMoreInteractions(listenerObj);
    
    bytes = "\n    ".getBytes("US-ASCII");
    Mockito.when(serialPort.readBytes()).thenReturn(bytes);
    listener.serialEvent(new SerialPortEvent("fakePortName", SerialPortEvent.RXCHAR, 10));
    Mockito.verify(listenerObj).doStuff(255);
  }
}
