/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import java.util.Arrays;
import mase.mason.world.CircularObject;
import mase.mason.world.MultilineObject;

/**
 * Same as PlaygroundSDBC, but walls are considered as just another obstacle
 * @author jorge
 */
public class PlaygroundSDBC2 extends PlaygroundSDBC {

    private static final long serialVersionUID = 1L;

    // agent-to-obstacles/wall mean distance; agent-to-closest-obstacle/wall distance; agent-to-objects mean distance; agent-to-closest-object mean distance; agent linear speed; agent turn speed 
    @Override
    protected double[] state(Playground pl) {
        double[] res = new double[6];
        Arrays.fill(res, Double.NaN);

        double md = pl.agent.distanceTo(pl.walls);
        double min = md;
        for (MultilineObject o : pl.obstacles) {
            double d = pl.agent.distanceTo(o);
            md += d;
            min = Math.min(d, min);
        }
        res[0] = md / (pl.obstacles.size() + 1);
        res[1] = min;

        if (!pl.objects.isEmpty()) {
            md = 0;
            min = Double.POSITIVE_INFINITY;
            for (CircularObject o : pl.objects) {
                double d = pl.agent.distanceTo(o);
                md += d;
                min = Math.min(d, min);
            }
            res[2] = md / pl.objects.size();
            res[3] = min;
        }

        res[4] = pl.agent.getSpeed();

        res[5] = pl.agent.getTurningSpeed();

        return res;
    }

}
