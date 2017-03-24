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
import mase.mason.MasonSimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ForagingTask extends MasonSimState {

    private static final long serialVersionUID = 1L;

    protected final ForagingPar originalPar;
    protected ForagingPar par;
    protected Continuous2D field;
    protected FlyingRobot flyingBot;
    protected LandRobot landBot;
    protected List<Item> items;

    public ForagingTask(long seed, ForagingPar par, GroupController gc) {
        super(gc, seed);
        this.originalPar = par;
    }

    @Override
    public void start() {
        super.start();
        par = originalPar.clone();
        par.flyingVisionAngle += par.flyingVisionAngle * par.sensorOffset * (random.nextDouble() * 2 - 1);
        par.landVisionAngle += par.landVisionAngle * par.sensorOffset * (random.nextDouble() * 2 - 1);
        par.flyingMaxHeight += par.flyingMaxHeight * par.maxHeightOffset * (random.nextDouble() * 2 - 1);
        par.landSensingRange += par.landSensingRange * par.sensorOffset * (random.nextDouble() * 2 - 1);
        par.landLinearSpeed += par.landLinearSpeed * par.actuatorOffset * (random.nextDouble() * 2 - 1);
        par.landTurnSpeed += par.landTurnSpeed * par.actuatorOffset * (random.nextDouble() * 2 - 1);
        par.flyingAngSpeed += par.flyingAngSpeed * par.actuatorOffset * (random.nextDouble() * 2 - 1);
        par.flyingLinearSpeed += par.flyingLinearSpeed * par.actuatorOffset * (random.nextDouble() * 2 - 1);

        this.field = new Continuous2D(100, par.arenaSize.x, par.arenaSize.y);
        AgentController[] acs;
        if (gc instanceof HeterogeneousGroupController) {
            acs = gc.getAgentControllers(2);
        } else if (par.useFlyingRobot) {
            acs = new AgentController[]{null, gc.getAgentControllers(1)[0]};
        } else {
            acs = new AgentController[]{gc.getAgentControllers(1)[0], null};
        }

        landBot = new LandRobot(this, field, acs[0] == null ? null : acs[0].clone());
        flyingBot = new FlyingRobot(this, field, acs[1] == null ? null : acs[1].clone());

        if (par.landPlacement == ForagingPar.FIXED) {
            landBot.setLocation(par.landStartPos);
            landBot.setOrientation(par.landStartOri);
        } else if (par.landPlacement == ForagingPar.SEMI_RANDOM) {
            // place in any corner
            double x = super.random.nextInt(2) * field.width;
            double y = super.random.nextInt(2) * field.height;
            landBot.setLocation(new Double2D(x, y));
            landBot.setOrientation(random.nextDouble() * Math.PI * 2);
        } else if (par.landPlacement == ForagingPar.RANDOM) {
            // place anywhere
            Double2D pos = new Double2D(random.nextDouble() * field.width, random.nextDouble() * field.height);
            landBot.setLocation(pos);
            landBot.setOrientation(random.nextDouble() * Math.PI * 2);
        }

        // start in the specified position
        if (par.flyingPlacement == ForagingPar.FIXED) {
            flyingBot.setLocation(par.flyingStartPos);
            flyingBot.setOrientation(par.flyingStartOri);
        } else if (par.flyingPlacement == ForagingPar.SEMI_RANDOM) { // start near the land robot
            Double2D landPos = landBot.getLocation();
            double displacement = par.flyingRadius + par.landRadius * 2;
            double x = landPos.x < field.width / 2 ? landPos.x + displacement : landPos.x - displacement;
            double y = landPos.y < field.height / 2 ? landPos.y + displacement : landPos.y - displacement;
            flyingBot.setLocation(new Double2D(x, y));
            flyingBot.setOrientation(random.nextDouble() * Math.PI * 2);
        } else if (par.flyingPlacement == ForagingPar.RANDOM) { // start anywhere inside the arena
            Double2D pos = null;
            while (pos == null) {
                Double2D candidate;
                if (par.flyingMaxDist <= 0) { // use the entire field
                    candidate = new Double2D(random.nextDouble() * field.width, random.nextDouble() * field.height);
                } else {
                    Double2D l = landBot.getLocation();
                    double xmin = Math.max(0, l.x - par.flyingMaxDist - par.flyingRadius - par.landRadius);
                    double xmax = Math.min(field.width, l.x + par.flyingMaxDist + par.flyingRadius + par.landRadius);
                    double ymin = Math.max(0, l.y - par.flyingMaxDist - par.flyingRadius - par.landRadius);
                    double ymax = Math.min(field.height, l.y + par.flyingMaxDist + par.flyingRadius + par.landRadius);
                    candidate = new Double2D(random.nextDouble() * (xmax - xmin) + xmin, random.nextDouble() * (ymax - ymin) + ymin);
                }

                double dist = landBot.getLocation().distance(candidate) - par.flyingRadius - par.landRadius;
                if (dist > par.landRadius && (par.flyingMaxDist <= 0 || dist <= par.flyingMaxDist)) { // leave at least landRadius distance between them
                    pos = candidate;
                }
            }
            flyingBot.setLocation(pos);
            flyingBot.setOrientation(random.nextDouble() * Math.PI * 2);
        }

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
            Item it = new Item(this, par.itemRadius);
            items.add(it);
            it.setLocation(pos);
        }
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }
}
