/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import java.util.LinkedList;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.controllers.HeterogeneousGroupController;
import mase.mason.GUICompatibleSimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ForagingTask extends GUICompatibleSimState {

    protected ForagingPar par;
    protected Continuous2D field;
    protected GroupController gc;
    protected FlyingRobot flyingBot;
    protected LandRobot landBot;
    protected List<Item> items;

    public ForagingTask(long seed, ForagingPar par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(100, par.arenaSize.x, par.arenaSize.y);
        AgentController[] acs;
        if(gc instanceof HeterogeneousGroupController) {
            acs = gc.getAgentControllers(2);
        } else {
            if(par.useFlyingRobot) {
                acs = new AgentController[]{null, gc.getAgentControllers(1)[0]};
            } else {
                acs = new AgentController[]{gc.getAgentControllers(1)[0], null};
            }
        }       

        landBot = new LandRobot(this, field, acs[0]);
        flyingBot = new FlyingRobot(this, field, acs[1]);

        landBot.setLocation(par.landStartPos);
        landBot.setOrientation(par.landStartOri);
        flyingBot.setLocation(par.flyingStartPos);
        flyingBot.setOrientation(par.flyingStartOri);

        schedule.scheduleRepeating(landBot);
        schedule.scheduleRepeating(flyingBot);

        // Place items
        staticPlaceItems();

    }

    protected void staticPlaceItems() {
        this.items = new LinkedList<>();
        for (Double2D p : par.items) {
            Double2D pos = p.add(new Double2D((random.nextDouble() - 0.5) * par.itemPlacementZone,
                    (random.nextDouble() - 0.5) * par.itemPlacementZone));
            Item it = new Item(par.itemRadius, pos);
            items.add(it);
            field.setObjectLocation(it, pos);
        }
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }
}
