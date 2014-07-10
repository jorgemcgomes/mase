/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred.deprecated;

import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class EscapeAlgorithmTest {

    public static void main(String[] args) {
        MutableDouble2D escape = new MutableDouble2D(0, 0);
        Double2D[] predLocs = new Double2D[]{new Double2D(402.5,94.5), new Double2D(440,81), new Double2D(413,40)};
        Double2D preyLoc = new Double2D(414,67);

        //int count = 0;
        for (Double2D pred : predLocs) {
                    //escape.addIn(pred.getLocation());
            //count++;
            double dist = pred.distance(preyLoc);
            MutableDouble2D vec = new MutableDouble2D(preyLoc);
            vec.subtractIn(pred); // predator to prey vector
            dist = 1 / dist;
            vec.normalize();
            vec.multiplyIn(dist);
            escape.addIn(vec);
        }
        
        escape.normalize();
        escape.multiplyIn(25);
        escape.addIn(preyLoc);
        System.out.println(escape);
    }

}
