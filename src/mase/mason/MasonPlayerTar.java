/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import mase.stat.PersistentSolution;
import mase.stat.Reevaluate;
import mase.stat.SolutionPersistence;
import sim.display.Controller;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class MasonPlayerTar {
       
    public static void main(String[] args) throws Exception {
        File tar = Reevaluate.findControllerFile(args);
        if(tar == null) {
            System.exit(1);
        }
        long startSeed = 0;
        MasonSimulationProblem sim = (MasonSimulationProblem) Reevaluate.createSimulator(args, tar.getParentFile());
        final MasonSimState state = sim.getSimState(null, startSeed);
        
        final List<PersistentSolution> sols = SolutionPersistence.readSolutionsFromTar(tar);
        System.out.println("Solutions found in tar: " + sols.size());
        
        
        SolutionsList frame = new SolutionsList();
        
        final JTable table = frame.table;
        final JButton launch = frame.launchButton;
        final JTextArea text = frame.text;
        final JLabel status = frame.status;
        
        // Fill table
        DefaultTableModel mod = (DefaultTableModel) table.getModel();
        for(PersistentSolution s : sols) {
            Object ud = s.getUserData();
            String udStr = ud == null ? "NA" : (ud.getClass().isArray() ? Arrays.toString((int[]) ud) : ud.toString()); 
            mod.addRow(new Object[]{s.getGeneration(), s.getSubpop(), s.getIndex(), s.getFitness(), udStr});
        }
        
        // Update text pane on row selection
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int r = table.getSelectedRow();
                if(r != -1) {
                    r = table.convertRowIndexToModel(r);
                    PersistentSolution sol = sols.get(r);
                    text.setText(sol.toString());
                } else {
                    text.setText("");
                }
            }
            
        });
        
        // Change controller on launch button
        launch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int r = table.getSelectedRow();
                if(r != -1) {
                    r = table.convertRowIndexToModel(r);
                    PersistentSolution sol = sols.get(r);
                    state.setGroupController(sol.getController());
                    status.setText(sol.getGeneration() + " / " + sol.getSubpop() + " / " + sol.getIndex());
                }
            }
        });
        
        GUIState gui = sim.getSimStateUI(state);
        Controller contr = gui.createController();   
        contr.registerFrame(frame);
    }

    
}
