/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author ashmore
 */
public class Main {
  public static void main(String args[]) throws FileNotFoundException, IOException, SerialPortException {
    // NOTE: We don't want to treat this as a thing that we can just write all at once
    // It needs to be possible to pause and resume mid plot- so a UI may be necessary.
    FileReader fileReader = new FileReader("data.csv");
    Plot plot = new PlotReader(ImmutableList.of(DataChannel.POSITION_X, DataChannel.POSITION_Y))
        .read(fileReader);
    PlotDataIterator plotData = new PlotDataIterator(plot);

    SerialController serialController = new SerialController();
    PlotWriter plotWriter = new PlotWriter(serialController, plotData, 
        Executors.newSingleThreadExecutor());
    
    Preconditions.checkState(serialController.connect(), "Could not connect!");
    
    plotWriter.start();
  }
}
