/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import java.io.FileInputStream;

/**
 *
 * @author jorge
 */
public class SolutionInspector {

    public static void main(String[] args) {
        for (String str : args) {
            System.out.println(str);
            try {
                PersistentSolution readSolution = SolutionPersistence.readSolution(new FileInputStream(str));
                System.out.println(readSolution);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
