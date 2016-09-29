/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.ui;

import com.google.common.collect.ImmutableList;
import com.icosilune.plottercontroller.data.DataChannel;
import com.icosilune.plottercontroller.data.Plot;
import com.icosilune.plottercontroller.data.PlotDataIterator;
import com.icosilune.plottercontroller.data.PlotReader;
import com.icosilune.plottercontroller.io.PlotWriter;
import com.icosilune.plottercontroller.io.SerialController;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author ashmore
 */
public class MainPanel extends JPanel {
  
  private final PlotView plotView;
  private final SerialController serialController;
  
  private final AbstractAction loadPlot = new LoadPlotAction();
  private final AbstractAction connect = new ConnectAction();
  private final AbstractAction disconnect = new DisconnectAction();
  private final AbstractAction start = new StartAction();
  private final AbstractAction stop = new StopAction();
  private final AbstractAction pause = new PauseAction();
  private final AbstractAction unpause = new UnpauseAction();

  private Plot currentPlot = null;
  private PlotWriter plotWriter = null;

  public MainPanel() {
    setLayout(new BorderLayout());
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(new JButton(loadPlot));
    buttonPanel.add(new JButton(connect));
    buttonPanel.add(new JButton(disconnect));
    buttonPanel.add(new JButton(start));
    buttonPanel.add(new JButton(stop));
    buttonPanel.add(new JButton(pause));
    buttonPanel.add(new JButton(unpause));
    add(buttonPanel,BorderLayout.NORTH);
    
    plotView = new PlotView();
    add(plotView, BorderLayout.CENTER);
    
    this.serialController = new SerialController();
    configureButtons();
  }
  
  private void configureButtons() {
    loadPlot.setEnabled(plotWriter == null);
    
    // Ideally would have way to toggle these, but it's possible that the serial can get
    // disconnected in the hardware world
    connect.setEnabled(!serialController.isConnected());
    disconnect.setEnabled(serialController.isConnected());
    
    start.setEnabled(currentPlot != null && plotWriter == null && serialController.isConnected());
    stop.setEnabled(plotWriter != null);
    
    pause.setEnabled(plotWriter != null && !plotWriter.isPaused());
    unpause.setEnabled(plotWriter != null && plotWriter.isPaused());
  }
  
  void setPlot(Plot plot) {
    // visual indication of plot change, changes to other states
    // display plot name??
    this.currentPlot = plot;
    plotView.setPlot(plot);
  }
  
  void updateProgress(double strokeProgress, double totalProgress) {
    
  }
  
  class PauseAction extends AbstractAction {
    public PauseAction() {
      super("Pause");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      plotWriter.pause();
      configureButtons();
    }
  }
  
  class UnpauseAction extends AbstractAction {
    public UnpauseAction() {
      super("Unpause");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      plotWriter.unpause();
      configureButtons();
    }
  }
  
  class StartAction extends AbstractAction {
    public StartAction() {
      super("Start!");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      PlotDataIterator plotData = new PlotDataIterator(currentPlot);
      plotWriter = new PlotWriter(serialController, plotData, MainPanel.this::updateProgress);

      plotWriter.start();
      configureButtons();
    }
  }
  
  class StopAction extends AbstractAction {
    public StopAction() {
      super("Stop");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      plotWriter.stop();
      plotWriter = null;
      configureButtons();
    }
  }
  
  class ConnectAction extends AbstractAction {
    public ConnectAction() {
      super("Connect");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      if(!serialController.isConnected()) {
        if(!serialController.connect()) {
          JOptionPane.showMessageDialog(MainPanel.this, "Could not connect");
        }
      }
      configureButtons();
    }
  }
  
  class DisconnectAction extends AbstractAction {
    public DisconnectAction() {
      super("Disconnect");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      serialController.disconnect();
      configureButtons();
    }
  }

  class LoadPlotAction extends AbstractAction {
    public LoadPlotAction() {
      super("Load Plot");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser chooser = new JFileChooser();
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
          "CSV files", "csv");
      chooser.setFileFilter(filter);
      if (chooser.showOpenDialog(MainPanel.this) == JFileChooser.APPROVE_OPTION) {
        File selectedFile = chooser.getSelectedFile();
        try (FileReader fileReader = new FileReader(selectedFile)) {
          Plot plot
              = new PlotReader(
                  ImmutableList.of(
                      DataChannel.POSITION_X,
                      DataChannel.POSITION_Y,
                      DataChannel.SPEED,
                      DataChannel.PRESSURE_Z,
                      DataChannel.YAW,
                      DataChannel.PITCH))
              .read(fileReader);
          setPlot(plot);
          configureButtons();
        } catch (IOException | PlotReader.ParseException ex) {
          // warning message
          JOptionPane.showMessageDialog(MainPanel.this, "File could not be read!");
          Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }
}
