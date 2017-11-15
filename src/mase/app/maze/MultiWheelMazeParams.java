/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

/**
 *
 * @author Jorge
 */
public class MultiWheelMazeParams extends MazeParams {
    
    private static final long serialVersionUID = 1L;
    
    public boolean[] wheels;
    
    public double slipAngleLimit;
    public double friction;
    public double parallelAngle;
    
    public double turnLimit;
    public double turnAcceleration;
    
    public double linearAcceleration;
}
