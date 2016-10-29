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
    
    Mockito.verify(serialPort).writeString("hello FFFFFFFFCDCCCC3D\n");
  }
  
  @Test
  public void testReceiveData() throws SerialPortException, UnsupportedEncodingException {

    ListenerObject listenerObj = Mockito.mock(ListenerObject.class);
    FakeListener commandListener = new FakeListener("hello", listenerObj);
    
    controller.connect();
    controller.addListener(commandListener);
    SerialPortEventListener listener = eventListenerCaptor.getValue();

    // https://www.h-schmidt.net/FloatConverter/IEEE754.html
    // 0x3dcccccd in big endian
    byte[] bytes = "hello FFFFFFFFCDCCCC3D0000000000000000\n".getBytes("US-ASCII");
    Mockito.when(serialPort.readBytes()).thenReturn(bytes);

    listener.serialEvent(new SerialPortEvent("fakePortName", SerialPortEvent.RXCHAR, 10));
    
    Mockito.verify(listenerObj).doStuff(-1, 0.1f, 0L);
  }
  
  interface ListenerObject {
    void doStuff(int a, float b, long c);
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
}
