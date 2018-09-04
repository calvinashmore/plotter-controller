/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data.generator;

import com.google.common.collect.ImmutableMap;
import com.icosilune.plottercontroller.data.DataChannel;

/**
 *
 * @author ashmo
 */
public class CircleGenerator extends FunctionalSingleStrokeGenerator {
    
    private static double RADIUS = 200;

    public CircleGenerator() {
        super(1000, ImmutableMap.of(
                DataChannel.POSITION_X, t -> RADIUS * Math.cos(t*2*Math.PI),
                DataChannel.POSITION_Y, t -> RADIUS * Math.sin(t*2*Math.PI)
        ));
    }
}
