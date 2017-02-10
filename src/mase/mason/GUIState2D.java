/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.awt.Color;
import javax.swing.JFrame;
import mase.evaluation.EvaluationFunction;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Inspector;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class GUIState2D extends GUIState {

    protected Display2D display;
    protected JFrame displayFrame;
    protected String title;
    protected int w, h;
    protected Color background;
    protected FieldPortrayal2D portrayal;

    public GUIState2D(MasonSimState state, String title, int w, int h, Color background) {
        super(state);
        this.title = title;
        this.w = w;
        this.h = h;
        this.portrayal = state.createFieldPortrayal();
        this.background = background;
    }

    @Override
    public void start() {
        super.start();
        setupPortrayals();
        super.scheduleAtEnd(new Steppable() {
            @Override
            public void step(SimState state) {
                MasonSimState mss = (MasonSimState) state;
                if(mss.schedule.getTime() != Schedule.AFTER_SIMULATION) { // hack to run post-eval when stop button is pressed
                    mss.kill();
                }
                for(int i = 0 ; i < mss.currentEvals.length ; i++) {
                    EvaluationFunction e = mss.currentEvals[i];
                    System.out.println("Evaluation " + i + " " + e.getClass().getSimpleName() + ":\n" + e.getResult());
                }
            }
        });
    }  

    @Override
    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals() {
        MasonSimState mss = (MasonSimState) state;
        mss.setupPortrayal(portrayal);
        // reschedule the displayer
        display.reset();
        display.setBackdrop(background);
        // redraw the display
        display.repaint();
    }

    @Override
    public Object getSimulationInspectedObject() {
        return state;
    }

    @Override
    public Inspector getInspector() {
        Inspector insp = super.getInspector();
        insp.setVolatile(true);
        return insp;
    }

    @Override
    public void init(Controller c) {
        super.init(c);

        // make the displayer
        display = new Display2D(w, h, this);
        display.setClipping(false);
        displayFrame = display.createFrame();
        displayFrame.setTitle(title);
        c.registerFrame(displayFrame);
        //displayFrame.setSize(w + 150, h + 200);
        displayFrame.setVisible(true);
        display.attach(portrayal, "Simulation");
    }

    @Override
    public void quit() {
        super.quit();
        if (displayFrame != null) {
            displayFrame.dispose();
        }
        displayFrame = null;
        display = null;
    }
    
    
}
