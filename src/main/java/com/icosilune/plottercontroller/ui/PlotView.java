/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.ui;

import com.google.common.collect.Range;
import com.icosilune.plottercontroller.data.DataChannel;
import com.icosilune.plottercontroller.data.DataPoint;
import com.icosilune.plottercontroller.data.Plot;
import com.icosilune.plottercontroller.data.Stroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JPanel;

/**
 *
 * @author ashmore
 */
public class PlotView extends JPanel {
  private Plot plot;
  private DataPoint currentProgress;

  public PlotView() {
    setPreferredSize(new Dimension(500, 500));
  }

  public void setPlot(Plot plot) {
    this.plot = plot;
    repaint();
  }

  public void setCurrentProgress(DataPoint currentProgress) {
    this.currentProgress = currentProgress;
    repaint();
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());
    
    // change color based on stroke index
    g.setColor(Color.BLACK);
    if (plot != null) {
      
      Range<Double> xExtents = plot.getExtents(DataChannel.POSITION_X);
      Range<Double> yExtents = plot.getExtents(DataChannel.POSITION_Y);
      
      plot.getStrokes().forEach(s -> paintStroke(s,g2, xExtents, yExtents));
      
      if (currentProgress != null) {
        Point ip = getPosition(currentProgress, xExtents, yExtents);
        g2.setColor(Color.RED);
        g2.drawOval(ip.x-5, ip.y-5, 10, 10);
      }
    }
  }
  
  private Point getPosition(DataPoint p, Range<Double> xExtents, Range<Double> yExtents) {
    double x = p.get(DataChannel.POSITION_X);
    double y = p.get(DataChannel.POSITION_Y);
    int ix = (int) (getWidth() * (x - xExtents.lowerEndpoint())/(xExtents.upperEndpoint() - xExtents.lowerEndpoint()));
    int iy = (int) (getHeight()* (y - yExtents.lowerEndpoint())/(yExtents.upperEndpoint() - yExtents.lowerEndpoint()));
    return new Point(ix, iy);
  }
  
  private void paintStroke(Stroke s, Graphics2D g, Range<Double> xExtents, Range<Double> yExtents) {
    
    int lastIx = 0;
    int lastIy = 0;
    
    for(int i = 0; i<s.getNumberDataPoints();i++) {
      DataPoint p = s.getPoint(i);
      Point ip = getPosition(p, xExtents, yExtents);
      
      int ix = ip.x;
      int iy = ip.y;
      
      if(i>0) {
        g.drawLine(ix, iy, lastIx, lastIy);
      }
      
      lastIx = ix;
      lastIy = iy;
    }
  }
}
