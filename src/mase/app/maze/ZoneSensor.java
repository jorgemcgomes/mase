/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import java.awt.Color;
import mase.mason.world.AbstractSensor;
import mase.mason.world.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class ZoneSensor extends AbstractSensor {

    private static final long serialVersionUID = 1L;

    public ZoneSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    @Override
    public int valueCount() {
        return 1;
    }

    @Override
    public double[] readValues() {
        MazeTask mt = (MazeTask) state;
        double speedLimit = mt.speedingMonitor.getZoneSpeedLimit();
        if(speedLimit < mt.par.linearSpeed) {
            this.paint = Color.RED;
        } else {
            this.paint = new Color(0,0,0,0);
        }
        return new double[]{speedLimit};
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        MazeTask mt = (MazeTask) state;
        return new double[]{(Math.max(0, Math.min(mt.par.linearSpeed, vals[0])) / mt.par.linearSpeed) * 2 - 1};
    }
}
