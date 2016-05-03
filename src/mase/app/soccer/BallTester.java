/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

/**
 *
 * @author jorge
 */
public class BallTester {

    public static void main(String[] args) {
        sim(1, 0.2, 0.35, 0.03, 0.7);
    }

    private static void sim(double speed, double minSpeed, double slipDecel, double rollDecel, double ballSlipToRoll) {
        double currentSpeed = speed;
        double initialSpeed = speed;
        double pos = 0;
        for (int i = 0; i < 100; i++) {
            pos += currentSpeed;
            if (currentSpeed < minSpeed) {
                currentSpeed = 0;
            } else if (currentSpeed > ballSlipToRoll * initialSpeed) {
                currentSpeed = Math.max(0, currentSpeed - slipDecel);
            } else {
                currentSpeed = Math.max(0, currentSpeed - rollDecel);
            }
            System.out.println(i + "\t" + pos + "\t" + currentSpeed);
        }

    }

}
