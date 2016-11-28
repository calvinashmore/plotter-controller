/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.arduino.rpc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author ashmore
 */
public enum Type {
  INT,
  FLOAT,
  LONG,
  ;
  
  public static Type forClass(Class c) {
    if(c == Integer.class) {
      return INT;
    }
    if(c == Float.class) {
      return FLOAT;
    }
    if(c == Long.class) {
      return LONG;
    }
    throw new IllegalArgumentException("Type for "+c+" is not supported");
  }
  
  public String encode(Object obj) {
    byte[] bytes;
    switch(this) {
      case INT: {
        int value = (int) obj;
        ByteBuffer b = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.BIG_ENDIAN);
        b.putInt(value);
        bytes = b.array();
        break;
      }
      case FLOAT: {
        float value = (float) obj;
        ByteBuffer b = ByteBuffer.allocate(Float.BYTES).order(ByteOrder.BIG_ENDIAN);
        b.putFloat(value);
        bytes = b.array();
        break;
      }
      case LONG: {
        long value = (long) obj;
        ByteBuffer b = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN);
        b.putLong(value);
        bytes = b.array();
        break;
      }
      default:
        throw new IllegalArgumentException("Unrecognized type "+this);
    }
    return bytesToHex(bytes);
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
  
  public Object decode(String s) {
    byte[] b = new byte[s.length() / 2];
    for (int i = 0; i < b.length; i++) {
      int index = i * 2;
      int v = Integer.parseInt(s.substring(index, index + 2), 16);
      b[i] = (byte) v;
    }
    
    ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN);
    
    switch(this) {
      case INT: 
        return bb.getInt();
      case FLOAT:
        return bb.getFloat();
      case LONG:
        return bb.getLong();
      default:
        throw new IllegalArgumentException("Unrecognized type "+this);
    }
  }
  
  public int stringSize() {
    switch(this) {
      case INT: return 8;
      case FLOAT: return 8;
      case LONG: return 16;
      default: throw new IllegalArgumentException("Unrecognized type "+this);
    }
  }
}
