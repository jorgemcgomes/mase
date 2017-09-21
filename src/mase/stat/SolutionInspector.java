/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import mase.controllers.AgentController;
import mase.controllers.HeterogeneousGroupController;
import mase.neat.NEATAgentController;
import mase.neat.NEATSerializer;

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
                
                if(readSolution.getController() instanceof HeterogeneousGroupController) {
                    Set<String> contrStr = new HashSet<>();
                    HeterogeneousGroupController hgc = (HeterogeneousGroupController) readSolution.getController();
                    AgentController[] allControllers = hgc.getAllControllers();
                    for(AgentController ac : allControllers) {
                        contrStr.add(NEATSerializer.serializeToString(((NEATAgentController) ac).getNetwork()));
                    }
                    System.out.println("Different controllers: " + contrStr.size());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
