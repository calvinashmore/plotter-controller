/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.arduino.rpc;

import com.google.common.collect.ImmutableList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ashmore
 */
public abstract class CommandListener {
  private static final Logger LOG = Logger.getLogger( SerialController.class.getName() );
  
  private final String name;
  private final ImmutableList<Type> types;

  public CommandListener(String name, Type... types) {
    this.name = name;
    this.types = ImmutableList.copyOf(types);
  }

  public String getName() {
    return name;
  }

  public ImmutableList<Type> getTypes() {
    return types;
  }
  
  public void execute(String stringArgs) {
    try {
    Object[] args = new Object[types.size()];
    int pos = 0;
    for(int i=0;i<types.size();i++) {
      Type type = types.get(i);
      args[i] = type.decode(stringArgs.substring(pos, pos + type.stringSize()));
      pos += type.stringSize();
    }
    process(args);
    } catch(IndexOutOfBoundsException | IllegalArgumentException ex) {
      LOG.log(Level.WARNING,"Could not parse args \"{0}\" with types {1} in command {2} due to {3}",new Object[]{stringArgs, types,name, ex});
    }
  }

  public abstract void process(Object[] args);
}
