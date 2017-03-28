/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import java.awt.Color;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.CircularObject;
import mase.mason.world.GeomUtils.Segment;
import net.jafama.FastMath;
import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Ball extends CircularObject implements Steppable {

    private static final long serialVersionUID = 1L;
        
    private EmboddiedAgent kickingAgent;
    private double initialSpeed;
    private double currentDirection;
    private double currentSpeed;

    public Ball(Soccer sim) {
        super(new OvalPortrayal2D(Color.WHITE, sim.par.ballRadius * 2, true), sim, sim.field, sim.par.ballRadius);        
    }

    @Override
    public void step(SimState state) {
        if(currentSpeed == 0) {
            return; // ball is stopped, there's nothing to check
        }

        Double2D displacement = new Double2D(currentSpeed * FastMath.cos(currentDirection), currentSpeed * FastMath.sin(currentDirection));
        Double2D newPos = pos.add(displacement);
        
        // if it hits another player, stop
        Soccer soc = (Soccer) state;
        Bag collisions = field.getNeighborsExactlyWithinDistance(pos, soc.par.agentRadius + soc.par.agentKickDistance);
        for(Object o : collisions) {
            if(o != kickingAgent && o instanceof EvolvedSoccerAgent) {
                //System.out.println("Agent collision");
                currentSpeed = 0;
                return;
            }
        }

        // TODO: harcode best-effort check for computational efficiency
        Pair<Double, Segment> closest = soc.fieldBoundaries.getPolygon().closestSegment(newPos);
        if(closest.getLeft() < soc.par.ballRadius) {
            Segment seg = closest.getRight();
            // WARNING: this assumes the field boundaries were constructed clockwise
            Double2D wallNormal = new Double2D(seg.start.x - seg.end.x, seg.start.y - seg.end.y);
            wallNormal = wallNormal.normalize();
            wallNormal = wallNormal.rotate(Math.PI / 2);
            Double2D incomingVector = new Double2D(FastMath.cos(currentDirection), FastMath.sin(currentDirection));
            Double2D outgoingVector = incomingVector.subtract(wallNormal.multiply(2 * wallNormal.dot(incomingVector)));
            currentDirection = outgoingVector.angle();
            currentSpeed = currentSpeed * soc.par.ballCOR;
            kickingAgent = null;       
        } else {
            // ball is free, move ball!
            setLocation(newPos);
            
            // update ball speed
            if(currentSpeed < soc.par.ballMinSpeed) {
                currentSpeed = 0;
            } else if(currentSpeed > soc.par.ballSlipToRoll * initialSpeed) {
                currentSpeed = Math.max(0, currentSpeed - soc.par.ballSlipDeceleration);
            } else {
                currentSpeed = Math.max(0, currentSpeed - soc.par.ballRollDeceleration);
            }            
        }
    }
    
    public double getCurrentSpeed() {
        return currentSpeed;
    }
    
    
    public void reset() {
        this.currentSpeed = 0;
        this.initialSpeed = 0;
        this.currentDirection = 0;
        this.kickingAgent = null;
    }
    
    public void kick(EmboddiedAgent agent, double direction, double power) {
        initialSpeed = power;
        currentSpeed = power;
        currentDirection = direction;
        kickingAgent = agent;
    }

    // How much distance is travelled + how much time until the ball stops
    public Pair<Double,Integer> maxDistance(Soccer soc, double power) {
        int steps = 0;
        double dist = 0;
        double initSpeed = power;
        double curSpeed = power;
        
        while(curSpeed > soc.par.ballMinSpeed) {
            dist += curSpeed;
            steps++;
            if(curSpeed > soc.par.ballSlipToRoll * initSpeed) { // slip
                curSpeed = Math.max(0, curSpeed - soc.par.ballSlipDeceleration);
            } else { // roll
                curSpeed = Math.max(0, curSpeed - soc.par.ballRollDeceleration);
            }
        }
        
        return Pair.of(dist, steps);
    }
    
    

    /*@Override
    public double[] getStateVariables() {
        return new double[] {getLocation().x, getLocation().y, getSpeed()};
    }*/

    @Override
    public boolean maySetLocation(Object field, Object newObjectLocation) {
        reset();
        return super.maySetLocation(field, newObjectLocation);
    }
}
