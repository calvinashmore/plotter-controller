/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.ui;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.icosilune.plottercontroller.data.DataChannel;
import com.icosilune.plottercontroller.data.Extents;
import com.icosilune.plottercontroller.data.PlotDataIterator;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author ashmore
 */
public class ChannelEditors extends JPanel {
  
  private final Map<DataChannel,ChannelEditor> editors;
  
  public ChannelEditors() {
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    ImmutableMap.Builder<DataChannel, ChannelEditor> editors = ImmutableMap.builder();
    for(DataChannel dataChannel : DataChannel.values()) {
      ChannelEditor editor = new ChannelEditor(dataChannel);
      add(editor);
      editors.put(dataChannel, editor);
    }
    this.editors = editors.build();
  }

  public Extents getExtents() {
    return new Extents(Maps.transformValues(editors, ChannelEditor::getRange));
  }

  void updateProgress(PlotDataIterator.PlotProgress progress) {
    
  }
}
