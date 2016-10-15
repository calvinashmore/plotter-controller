/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.io;

/**
 * Perform some kind of action using the plotter.
 * E.g., moving to a specific point
 */
// QUESTIONS: How should this work alongside the plotter itself?
// Should handshaking be a command?
// Longer term question: should we try to make some kind of RPC layer?
public interface PlotterCommand {
  public void execute(SerialController controller);
}
