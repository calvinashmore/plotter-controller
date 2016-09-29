/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.icosilune.plottercontroller.ui.MainPanel;
import com.icosilune.plottercontroller.ui.PlotView;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author ashmore
 */
public class Main {
  public static void main(String args[]) throws FileNotFoundException, IOException, SerialPortException {
    JFrame main = new JFrame("omfg");
    main.add(new MainPanel());
    main.pack();
    main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    main.setVisible(true);
  }
}
