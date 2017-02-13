package mase.app.soccer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.generic.SmartAgentProvider;
import mase.mason.MasonSimState;
import mase.mason.world.SmartAgent;
import mase.mason.world.StaticPolygonObject;
import mase.mason.world.StaticPolygonObject.Segment;
import org.apache.commons.math3.util.FastMath;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Double2D;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jorge
 */
public class Soccer extends MasonSimState implements SmartAgentProvider {

    private static final long serialVersionUID = 1L;

    protected SoccerParams par;
    protected Continuous2D field;
    protected List<SoccerAgent> leftTeam, rightTeam, all;
    protected StaticPolygonObject fieldBoundaries;
    protected Double2D leftGoalCenter, rightGoalCenter;
    protected Color leftTeamColor, rightTeamColor;
    protected Ball ball;
    protected Referee referee;
    protected boolean startFlag = true;

    Soccer(long seed, SoccerParams par, GroupController gc) {
        super(gc, seed);
        this.par = par;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.fieldLength, par.fieldWidth);
        double fl = par.fieldLength, fw = par.fieldWidth, hg = par.goalWidth / 2, d = par.cornerDiag;
        Segment t1 = new Segment(new Double2D(0, fw / 2 - hg), new Double2D(0, d));
        Segment t2 = new Segment(t1.end, new Double2D(d, 0));
        Segment t3 = new Segment(t2.end, new Double2D(fl - d, 0));
        Segment t4 = new Segment(t3.end, new Double2D(fl, d));
        Segment t5 = new Segment(t4.end, new Double2D(fl, fw / 2 - hg));
        Segment t6 = new Segment(new Double2D(fl, fw / 2 + hg), new Double2D(fl, fw - d));
        Segment t7 = new Segment(t6.end, new Double2D(fl - d, fw));
        Segment t8 = new Segment(t7.end, new Double2D(d, fw));
        Segment t9 = new Segment(t8.end, new Double2D(0, fw - d));
        Segment t10 = new Segment(t9.end, new Double2D(0, fw / 2 + hg));

        leftGoalCenter = new Double2D(0, fw / 2);
        rightGoalCenter = new Double2D(fl, fw / 2);

        fieldBoundaries = new StaticPolygonObject(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
        fieldBoundaries.setStroke(new BasicStroke(1));
        fieldBoundaries.paint = Color.WHITE;
        fieldBoundaries.filled = false;
        field.setObjectLocation(fieldBoundaries, new Double2D(0, 0));

        ball = new Ball(this);
        ball.setLabel("");
        ball.setColor(Color.WHITE);
        schedule.scheduleRepeating(ball);

        createAgents();
        resetTeams(startFlag);
        startFlag = !startFlag;

        referee = new Referee();
        schedule.scheduleRepeating(referee);
    }

    public void createAgents() {
        int teamSize = 0;
        for (int t : par.formation) {
            teamSize += t;
        }
        leftTeam = new ArrayList<>();
        if (gc != null) {
            AgentController[] acs = super.gc.getAgentControllers(teamSize);
            for (int i = 0; i < teamSize; i++) {
                EvolvedSoccerAgent a = new EvolvedSoccerAgent(this, acs[i], par.agentMoveSpeed, par.agentKickSpeed);
                a.setLabel("Le" + i);
                leftTeam.add(a);
            }
        } else {
            for (int i = 0; i < teamSize; i++) {
                AIKAgent a = new AIKAgent(this);
                a.setLabel("Lp" + i);
                leftTeam.add(a);
            }
        }

        rightTeam = new ArrayList<>();
        for (int i = 0; i < teamSize; i++) {
            AIKAgent a = new AIKAgent(this);
            a.setLabel("Rp" + i);
            //SoccerAgent a = new SoccerAgent(this, null, 0, 0);
            rightTeam.add(a);
        }
        all = new ArrayList<>();
        all.addAll(leftTeam);
        all.addAll(rightTeam);

        leftTeamColor = Color.BLUE;
        rightTeamColor = Color.RED;

        for (SoccerAgent a : leftTeam) {
            a.setTeamContext(leftTeam, rightTeam, leftGoalCenter, rightGoalCenter, leftTeamColor);
            if (a instanceof EvolvedSoccerAgent) {
                ((EvolvedSoccerAgent) a).setupSensors();
            }
            schedule.scheduleRepeating(a);
        }
        for (SoccerAgent a : rightTeam) {
            a.setTeamContext(rightTeam, leftTeam, rightGoalCenter, leftGoalCenter, rightTeamColor);
            schedule.scheduleRepeating(a);
        }
    }

    public void resetTeams(boolean leftStarting) {
        initialPositions(leftTeam, true, leftStarting);
        initialPositions(rightTeam, false, !leftStarting);
        ball.reset();
        ball.setLocation(new Double2D(par.fieldLength / 2, par.fieldWidth / 2));
    }

    protected void initialPositions(List<SoccerAgent> team, boolean left, boolean starting) {
        if (par.lineFormation) {
            double xSpacing = (field.width / 2) / (par.formation.length + 1);
            double mx = field.width / 2;
            double side = left ? -1 : 1;
            int agentIndex = 0;
            double ori = left ? 0 : Math.PI;
            for (int i = 0; i < par.formation.length; i++) {
                double x = mx + side * xSpacing * (par.formation.length - i);
                double ySpacing = (field.height) / (par.formation[i] + 1);
                for (int j = 0; j < par.formation[i]; j++) {
                    double y = left ? ySpacing + ySpacing * j : field.height - ySpacing - ySpacing * j;
                    Double2D pos;
                    if (i == par.formation.length - 1 && j == par.formation[i] / 2 && starting) { // place advanced in front
                        pos = new Double2D(mx + side * par.agentRadius * 2, field.height / 2);
                    } else {
                        pos = new Double2D(x + (random.nextDouble() * 2 - 1) * par.locationRandom,
                                y + (random.nextDouble() * 2 - 1) * par.locationRandom);
                    }
                    SoccerAgent a = team.get(agentIndex++);
                    a.setLocation(pos);
                    a.setOrientation(ori);
                    if (a.getAgentController() != null) {
                        a.getAgentController().reset();
                    }
                }
            }
        } else {
            if (par.formation.length != 1 || par.formation[0] != team.size()) {
                throw new RuntimeException("Not using line formation. Expecting just one number for formation (same as number of agents)");
            }
            List<Double2D> positions = new ArrayList<>(par.formation[0]);
            double side = left ? -1 : 1;
            double ori = left ? 0 : Math.PI;
            if (starting) { // place one in front
                double angle = ori - random.nextDouble() * Math.PI - Math.PI / 2; 
                double dist = par.agentRadius * 2;
                Double2D offset = new Double2D(dist * FastMath.cos(angle), dist * FastMath.sin(angle));
                Double2D pos = new Double2D(field.width / 2, field.height / 2).add(offset);
                positions.add(pos);
            }
            double middleX = field.width / 2 + side * (field.width / 4);
            while (positions.size() < par.formation[0]) {
                Double2D rand = new Double2D(middleX + (random.nextDouble() * 2 - 1) * par.locationRandom,
                        field.height / 2 + (random.nextDouble() * 2 - 1) * par.locationRandom);
                boolean valid = true;
                for (Double2D other : positions) {
                    if (other.distance(rand) < par.agentRadius * 2) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    positions.add(rand);
                }
            }
            Iterator<SoccerAgent> teamIter = team.iterator();
            for (Double2D p : positions) {
                SoccerAgent a = teamIter.next();
                a.setLocation(p);
                a.setOrientation(ori);
                if (a.getAgentController() != null) {
                    a.getAgentController().reset();
                }
            }
        }
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }

    @Override
    public List<? extends SmartAgent> getSmartAgents() {
        return leftTeam;
    }
}


/*ProgSoccerAgent l1 = new ProgSoccerAgent(this);
        ProgSoccerAgent l2 = new ProgSoccerAgent(this);
        ProgSoccerAgent l3 = new ProgSoccerAgent(this);
        ProgSoccerAgent l4 = new ProgSoccerAgent(this);
        ProgSoccerAgent l5 = new ProgSoccerAgent(this);

        ProgSoccerAgent r1 = new ProgSoccerAgent(this);
        ProgSoccerAgent r2 = new ProgSoccerAgent(this);
        ProgSoccerAgent r3 = new ProgSoccerAgent(this);
        ProgSoccerAgent r4 = new ProgSoccerAgent(this);
        ProgSoccerAgent r5 = new ProgSoccerAgent(this);

        leftTeam = new ArrayList<>();
        leftTeam.add(l1);
        leftTeam.add(l2);
        leftTeam.add(l3);
        leftTeam.add(l4);
        leftTeam.add(l5);
        rightTeam = new ArrayList<>();
        rightTeam.add(r1);
        rightTeam.add(r2);
        rightTeam.add(r3);
        rightTeam.add(r4);
        rightTeam.add(r5);
        all = new ArrayList<>(leftTeam);
        all.addAll(rightTeam);

        l1.setTeamContext(leftTeam, rightTeam, leftGoalCenter, rightGoalCenter, Color.BLUE);
        l1.setLocation(new Double2D(120, 76));
        schedule.scheduleRepeating(l1);
        l2.setTeamContext(leftTeam, rightTeam, leftGoalCenter, rightGoalCenter, Color.BLUE);
        l2.setLocation(new Double2D(90, 36));
        schedule.scheduleRepeating(l2);
        l3.setTeamContext(leftTeam, rightTeam, leftGoalCenter, rightGoalCenter, Color.BLUE);
        l3.setLocation(new Double2D(90, 116));
        schedule.scheduleRepeating(l3);
        l4.setTeamContext(leftTeam, rightTeam, leftGoalCenter, rightGoalCenter, Color.BLUE);
        l4.setLocation(new Double2D(40, 56));
        schedule.scheduleRepeating(l4);
        l5.setTeamContext(leftTeam, rightTeam, leftGoalCenter, rightGoalCenter, Color.BLUE);
        l5.setLocation(new Double2D(40, 96));
        schedule.scheduleRepeating(l5);

        r1.setTeamContext(rightTeam, leftTeam, rightGoalCenter, leftGoalCenter, Color.RED);
        r1.setLocation(new Double2D(184, 36));
        schedule.scheduleRepeating(r1);
        r2.setTeamContext(rightTeam, leftTeam, rightGoalCenter, leftGoalCenter, Color.RED);
        r2.setLocation(new Double2D(184, 76));
        schedule.scheduleRepeating(r2);
        r3.setTeamContext(rightTeam, leftTeam, rightGoalCenter, leftGoalCenter, Color.RED);
        r3.setLocation(new Double2D(184, 116));
        schedule.scheduleRepeating(r3);
        r4.setTeamContext(rightTeam, leftTeam, rightGoalCenter, leftGoalCenter, Color.RED);
        r4.setLocation(new Double2D(234, 56));
        schedule.scheduleRepeating(r4);
        r5.setTeamContext(rightTeam, leftTeam, rightGoalCenter, leftGoalCenter, Color.RED);
        r5.setLocation(new Double2D(234, 96));
        schedule.scheduleRepeating(r5);*/
