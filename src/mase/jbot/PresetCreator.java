/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.jbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;
import mase.neat.NEATAgentController;
import mase.neat.NEATSerializer;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jorge
 */
public class PresetCreator {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("PresetCreator [preset file] [controller file] [name tag]");
            System.exit(1);
        }

        File preset = new File(args[0]);
        File cont = new File(args[1]);
        File tag = new File(args[2]);

        PersistentSolution sol = SolutionPersistence.readSolution(new FileInputStream(cont));
        GroupController contr = sol.getController();
        if (contr instanceof HomogeneousGroupController) {
            AgentController ac = contr.getAgentControllers(1)[0];
            File newFile = new File(preset.getAbsolutePath().replace(".conf", "") + "-" + tag + ".conf");
            FileUtils.copyFile(preset, newFile);
            NEATAgentController nac = (NEATAgentController) ac;
            double[] w = NEATSerializer.serializeToArray(nac.getNetwork());
            fillWeights(newFile, w);
        } else {
            AgentController[] acs = contr.getAgentControllers(0);
            for (int i = 0; i < acs.length; i++) {
                File newFile = new File(preset.getAbsolutePath().replace(".conf", "") + "-" + tag + "-" + i + ".conf");
                FileUtils.copyFile(preset, newFile);
                NEATAgentController nac = (NEATAgentController) acs[i];
                double[] w = NEATSerializer.serializeToArray(nac.getNetwork());
                fillWeights(newFile, w);
            }
        }
    }

    private static void fillWeights(File file, double[] weights) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("weights=(");
        for (int i = 0; i < weights.length - 1; i++) {
            sb.append(weights[i]).append(",");
        }
        sb.append(weights[weights.length - 1]).append(")");

        String descr = "description=(" + file.getName().replace("preset_", "").replace(".conf", "") + ")";

        String content = IOUtils.toString(new FileInputStream(file));
        content = content.replaceAll("weights\\s*=\\s*(.*)", sb.toString());
        content = content.replaceAll("description\\s*=\\s*(.*)", descr);
        System.out.println("Writting: " + file.getAbsolutePath() + " " + weights.length + " weights.");
        IOUtils.write(content, new FileOutputStream(file));
    }

}
