/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.jbot;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import gui.renderer.TwoDRenderer;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.io.FileUtils;
import simulation.Simulator;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class JBotViewer extends javax.swing.JFrame {

    private final TwoDRenderer renderer;
    private Simulator sim;
    private EvaluationFunction eval;
    private final JBotEvolver evo;
    private final PersistentSolution sol;
    private Loop runThread;

    private class Loop extends Thread {

        volatile boolean goOn = true;

        @Override
        public void run() {
            super.run();
            while (goOn) {
                step();
                try {
                    Thread.sleep(intervalSlider.getValue());
                } catch (InterruptedException ex) {
                    Logger.getLogger(JBotViewer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void step() {
        sim.performOneSimulationStep(sim.getTime() + 1);
        timeField.setText(sim.getTime() + "");
        timeBar.setValue((int) (double) sim.getTime());
        fitnessField.setText(eval.getFitness() + "");
        renderer.drawFrame();
        renderer.validate();
        if(sim.simulationFinished() && runThread != null) {
            runThread.goOn = false;
        }
    }

    private void setupSim() {
        sim = JBotSimulator.setupSimulator(evo, (Integer) seedSpinner.getValue(), sol.getController());
        if(seedIncCheck.isSelected()) {
            seedSpinner.setValue(seedSpinner.getNextValue());            
        }
        eval = evo.getEvaluationFunction();
        sim.addCallback(eval);
        renderer.setSimulator(sim);

        timeBar.setMinimum(0);
        timeBar.setMaximum(sim.getEnvironment().getSteps());
        timeBar.setValue(0);
        timeField.setText("0");

        renderer.drawFrame();
        renderer.validate();
    }

    /**
     * Creates new form JBotSimulatorViewer
     */
    public JBotViewer(File jbotConfig, File solution) throws Exception {
        initComponents();
        jbotPath.setText(jbotConfig.getAbsolutePath());
        solutionPath.setText(solution.getAbsolutePath());
        
        sol = SolutionPersistence.readSolution(new FileInputStream(solution));
        System.out.println(sol.toString());
        
        evo = new JBotEvolver(new String[]{jbotConfig.getAbsolutePath()});
        renderer = new TwoDRenderer(new Arguments(""));
        renderer.setSize(renderPanel.getWidth(), renderPanel.getHeight());
        renderPanel.add(renderer);
        this.pack();
        setupSim();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        stepButton = new javax.swing.JButton();
        runButton = new javax.swing.JToggleButton();
        intervalSlider = new javax.swing.JSlider();
        renderPanel = new javax.swing.JPanel();
        restartButton = new javax.swing.JButton();
        timeBar = new javax.swing.JProgressBar();
        timeField = new javax.swing.JTextField();
        seedSpinner = new javax.swing.JSpinner();
        solutionPath = new javax.swing.JLabel();
        jbotPath = new javax.swing.JLabel();
        seedIncCheck = new javax.swing.JCheckBox();
        fitnessField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JBotPlayer");

        mainPanel.setMinimumSize(new java.awt.Dimension(400, 400));
        mainPanel.setPreferredSize(new java.awt.Dimension(700, 796));

        stepButton.setText("Step");
        stepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepButtonActionPerformed(evt);
            }
        });

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        intervalSlider.setMajorTickSpacing(20);
        intervalSlider.setMinorTickSpacing(10);
        intervalSlider.setPaintLabels(true);
        intervalSlider.setPaintTicks(true);
        intervalSlider.setValue(30);

        renderPanel.setPreferredSize(new java.awt.Dimension(700, 700));

        javax.swing.GroupLayout renderPanelLayout = new javax.swing.GroupLayout(renderPanel);
        renderPanel.setLayout(renderPanelLayout);
        renderPanelLayout.setHorizontalGroup(
            renderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 700, Short.MAX_VALUE)
        );
        renderPanelLayout.setVerticalGroup(
            renderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 694, Short.MAX_VALUE)
        );

        restartButton.setText("Restart");
        restartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restartButtonActionPerformed(evt);
            }
        });

        timeField.setEditable(false);
        timeField.setText("0");

        solutionPath.setText("jLabel1");

        jbotPath.setText("jLabel2");

        seedIncCheck.setSelected(true);

        fitnessField.setEditable(false);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(stepButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(restartButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seedSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seedIncCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(intervalSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeField, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(renderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(solutionPath)
                    .addComponent(jbotPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(fitnessField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(seedIncCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(stepButton)
                        .addComponent(runButton)
                        .addComponent(restartButton)
                        .addComponent(seedSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(intervalSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(timeBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(timeField, javax.swing.GroupLayout.Alignment.LEADING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(renderPanel, 694, 694, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(solutionPath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbotPath))
                    .addComponent(fitnessField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 812, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void restartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restartButtonActionPerformed
        if (runThread != null) {
            runThread.goOn = false;
            runThread = null;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(JBotViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
            runButton.setSelected(false);
        }
        setupSim();
    }//GEN-LAST:event_restartButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        if (runButton.isSelected()) {
            if (runThread == null) {
                runThread = new Loop();
                runThread.start();
            }
        } else {
            if (runThread != null) {
                runThread.goOn = false;
                runThread = null;
            }
        }
    }//GEN-LAST:event_runButtonActionPerformed

    private void stepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepButtonActionPerformed
        step();
    }//GEN-LAST:event_stepButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JBotViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JBotViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JBotViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JBotViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        File jbotConfig;
        final File solution = new File(args[0]);
        if(args.length == 2) {
            jbotConfig = new File(args[1]); 
        } else {
            // Search the individual's folder for the jbot config file
            File dir = solution.getParentFile();
            Collection<File> listFiles = FileUtils.listFiles(dir, new String[]{"conf"}, false);
            if(listFiles.size() != 1) {
                System.out.println("Zero or more than one config files found!:\n" + listFiles.toString());
            }
            jbotConfig = listFiles.iterator().next();
        }
        final File jbot = jbotConfig;        
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new JBotViewer(jbot, solution).setVisible(true);
                } catch (Exception ex) {
                    Logger.getLogger(JBotViewer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField fitnessField;
    private javax.swing.JSlider intervalSlider;
    private javax.swing.JLabel jbotPath;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel renderPanel;
    private javax.swing.JButton restartButton;
    private javax.swing.JToggleButton runButton;
    private javax.swing.JCheckBox seedIncCheck;
    private javax.swing.JSpinner seedSpinner;
    private javax.swing.JLabel solutionPath;
    private javax.swing.JButton stepButton;
    private javax.swing.JProgressBar timeBar;
    private javax.swing.JTextField timeField;
    // End of variables declaration//GEN-END:variables
}
