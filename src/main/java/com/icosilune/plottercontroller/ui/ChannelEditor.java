/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.ui;

import com.google.common.collect.Range;
import com.icosilune.plottercontroller.data.DataChannel;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author ashmore
 */
public class ChannelEditor extends JPanel {
  
//  private final DataChannel channel;
  private final JTextField min;
  private final JTextField max;
  private final JTextField current;

  public ChannelEditor(DataChannel channel) {
//    this.channel = channel;

// Will need to make a grid view out of this
    add(new JLabel(channel.getNiceName()));
    add(min = new JFormattedTextField( NumberFormat.getNumberInstance()));
    add(max = new JFormattedTextField( NumberFormat.getNumberInstance()));
    add(current = new JFormattedTextField( NumberFormat.getNumberInstance()));
    add(new JButton(new SetAction()));
  }
  
  public void updateRange(double min, double max) {
    this.min.setText(Double.toString(min));
    this.max.setText(Double.toString(max));
  }
  
  public void update(double current) {
    this.current.setText(Double.toString(current));
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
