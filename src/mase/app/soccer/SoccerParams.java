/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import java.io.Serializable;

/**
 *
 * @author jorge
 */
public class SoccerParams implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // http://robocup.mi.fu-berlin.de/buch/rolling.pdf
    // values at 10 fps
    double ballRadius = 2;
    double ballSlipToRoll = 0.7; // of the initial speed
    double ballSlipDeceleration = 0.35; // 3.5 cm/s^2
    double ballRollDeceleration = 0.03; // 0.3 cm/s^2
    double ballMinSpeed = 0.2; // 2cm/s
    double ballCOR = 0.85; // coefficient of restitution, golf ball
    
    int[] formation = new int[]{3};
    double locationRandom = 10;
    double fieldLength = 274;
    double fieldWidth = 152;
    double cornerDiag = 5;
    double goalWidth = 50;
    double discretization = 25;
    int maxGameDuration = 1000;
    int ballStuckTime = 150;
    double ballStuckMovement = 10;    
    
    // values at 10 fps
    double agentRadius = 4;
    double agentMoveSpeed = 1; // 10cm/s
    double agentKickSpeed = 4; // 40cm/s
    double agentMinKickSpeed = 1; // 10cm/s
    //double agentTurnSpeed = 0.157; // 90deg/s
    double agentKickDistance = 0;
    
    int sensorArcs = 4;
    boolean goalSensors = true;
    boolean locationSensor = false;
    boolean possessionSensor = false;
    
    /*double progClearDistance = 10;
    double progOpenAngle;
    double progOpenDistance;*/
}
