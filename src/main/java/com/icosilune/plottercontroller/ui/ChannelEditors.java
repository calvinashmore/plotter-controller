/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.ui;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.icosilune.plottercontroller.data.DataChannel;
import com.icosilune.plottercontroller.data.Extents;
import com.icosilune.plottercontroller.data.Plot;
import com.icosilune.plottercontroller.data.PlotDataIterator;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author ashmore
 */
public class ChannelEditors extends JPanel {

  private final Map<DataChannel, ChannelEditor> editors;

  public ChannelEditors() {

    setLayout(new GridBagLayout());
    //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    
    ImmutableMap.Builder<DataChannel, ChannelEditor> editors = ImmutableMap.builder();
    for (DataChannel dataChannel : DataChannel.values()) {
      ChannelEditor editor = new ChannelEditor(dataChannel, constraints);
      editors.put(dataChannel, editor);
      constraints.gridy++;
      constraints.gridx = 0;
    }
    this.editors = editors.build();
  }

  public Extents getExtents() {
    return new Extents(Maps.transformValues(editors, ChannelEditor::getRange));
  }
  
  void updatePlot(Plot plot) {
    editors.values().forEach(editor -> editor.updateRange(plot.getExtents().get(editor.channel)));
  }

  void updateProgress(PlotDataIterator.PlotProgress progress) {
    editors.values().forEach(editor -> editor.update(progress.getPoint().get(editor.channel)));
  }

  class ChannelEditor {
    private final DataChannel channel;
    private final JFormattedTextField min;
    private final JFormattedTextField max;
    private final JFormattedTextField current;

    ChannelEditor(DataChannel channel, GridBagConstraints constraints) {
      this.channel = channel;
      add(new JLabel(channel.getNiceName()), constraints);
      constraints.gridx++;
      add(min = new JFormattedTextField(NumberFormat.getNumberInstance()), constraints);
      constraints.gridx++;
      add(max = new JFormattedTextField(NumberFormat.getNumberInstance()), constraints);
      constraints.gridx++;
      add(current = new JFormattedTextField(NumberFormat.getNumberInstance()), constraints);
      constraints.gridx++;
      add(new JButton(new SetAction()), constraints);
      
      min.setPreferredSize(new Dimension(80,24));
      max.setPreferredSize(new Dimension(80,24));
      current.setPreferredSize(new Dimension(80,24));
    }

    public void updateRange(Range<Double> extents) {
      this.min.setValue(extents.lowerEndpoint());
      this.max.setValue(extents.upperEndpoint());
      this.current.setText("");
    }

    public void update(double current) {
      this.current.setValue(current);
    }

    public Range<Double> getRange() {
      return Range.closed(Double.valueOf(min.getText()), Double.valueOf(max.getText()));
    }

    private class SetAction extends AbstractAction {

      public SetAction() {
        super("set");
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        
      }
    }
  }
}
