/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import sim.field.grid.IntGrid2D;
import sim.util.Int2D;
import sim.util.IntBag;

/**
 *
 * @author Jorge
 */
public class GoState {

    public static final int BLACK = 0, WHITE = 1, EMPTY = 2;
    protected IntGrid2D grid;
    protected Set<Group>[] groups;
    protected int[] captured;
    protected int passCount;
    

    private GoState() {}

    public GoState(int size) {
        this.grid = new IntGrid2D(size, size, EMPTY);
        this.groups = new Set[]{new HashSet<Group>(), new HashSet<Group>()};
        this.captured = new int[]{0, 0};
        this.passCount = 0;
    }
    
    public void update(GoState state) {
        if(state == this) {
            this.passCount++;
        } else {
            this.passCount = 0;
        }
        this.grid.setTo(state.getGrid());
        this.groups = state.groups;
        this.captured = state.captured;
    }

    @Override
    protected GoState clone() {
        // deep clone
        GoState newState = new GoState();
        newState.grid = new IntGrid2D(grid.getWidth(), grid.getWidth());
        for(int x = 0 ; x < grid.getWidth() ; x++) {
            for(int y = 0 ; y < grid.getWidth() ; y++) {
                newState.grid.set(x, y, grid.get(x, y));
            }
        }
        newState.captured = Arrays.copyOf(captured, 2);
        newState.groups = new Set[2];
        for(int i = 0 ; i < newState.groups.length ; i++) {
            newState.groups[i] = new HashSet<Group>();
            for(Group g : this.groups[i]) {
                newState.groups[i].add(g.clone());
            }
        }
        return newState;
    }

    protected static class Group {

        protected Set<Int2D> stones;
        protected Set<Int2D> liberties;

        Group() {
            this.stones = new HashSet<Int2D>();
            this.liberties = new HashSet<Int2D>();
        }

        @Override
        protected Group clone() {
            Group newGroup = new Group();
            newGroup.stones = new HashSet<Int2D>(stones);
            newGroup.liberties = new HashSet<Int2D>(liberties);
            return newGroup;
        }
    }

    private void updateGroupLiberties(Group group, IntGrid2D grid) {
        group.liberties.clear();
        for (Int2D p : group.stones) {
            for (Int2D n : neighbours(p)) {
                if (grid.get(n.x, n.y) == EMPTY) {
                    group.liberties.add(n);
                }
            }
        }
    }

    /*
     * Returns null if not a valid play
     */
    public GoState nextState(int player, Int2D pos) {
        // position already occupied
        if (grid.get(pos.x, pos.y) != EMPTY) {
            return null;
        }

        // place stone
        GoState newState = this.clone();
        newState.grid.set(pos.x, pos.y, player);

        // update players groups
        // create new group with new stone
        Group newGroup = new Group();
        newGroup.stones.add(pos);
        // check for groups occupying the neighbour intersections
        for (Int2D n : neighbours(pos)) {
            Iterator<Group> iter = newState.groups[player].iterator();
            while (iter.hasNext()) {
                Group next = iter.next();
                // if present, merge them in the new group and remove them
                if (next.stones.contains(n)) {
                    newGroup.stones.addAll(next.stones);
                    iter.remove();
                }
            }
        }
        // Add the new group
        newState.groups[player].add(newGroup);

        // update opponent liberties
        Iterator<Group> iter = newState.groups[(player + 1) % 2].iterator();
        while (iter.hasNext()) {
            Group next = iter.next();
            updateGroupLiberties(next, newState.grid);
            // remove group if no liberties
            if (next.liberties.isEmpty()) {
                iter.remove();
                newState.captured[player] += next.stones.size();
                for (Int2D p : next.stones) {
                    newState.grid.set(p.x, p.y, EMPTY);
                }
            }
        }
        
        /* Check for self-capture
         * A player may not self-capture, that is play a stone into a position where 
         * it would have no liberties or form part of a group which would thereby have 
         * no liberties, unless, as a result, one or more of the stones surrounding it is captured.*/
        // update player liberties
        for(Group g : newState.groups[player]) {
            updateGroupLiberties(g, newState.grid);
            // if there is any player group without liberties, the play is invalid
            if(g.liberties.isEmpty()) {
                return null;
            }
        }

        return newState;
    }

    public GoState[] possibleStates(int player) {
        // include current state -- pass
        LinkedList<GoState> states = new LinkedList<GoState>();
        states.add(this);

        // for each empty intersection, generate next state
        for(int x = 0 ; x < grid.getWidth() ; x++) {
            for(int y = 0 ; y < grid.getWidth() ; y++) {
                if(grid.get(x, y) == EMPTY) {
                    GoState next = nextState(player, new Int2D(x,y));
                    // check for invalid states
                    if(next != null) {
                        states.add(next);
                    }
                }
            }
        }
        
        GoState[] res = new GoState[states.size()];
        states.toArray(res);
        return res;
    }

    // TODO: optimize, initialize table with the neighbours and then use fast lookups
    private Int2D[] neighbours(Int2D pos) {
        IntBag xs = new IntBag(), ys = new IntBag();
        grid.getVonNeumannLocations(pos.x, pos.y, 1, IntGrid2D.BOUNDED, false, xs, ys);
        Int2D[] a = new Int2D[xs.numObjs];
        for (int i = 0; i < a.length; i++) {
            a[i] = new Int2D(xs.objs[i], ys.objs[i]);
        }
        return a;
    }

    public int[] getBoardDescription() {
        return grid.toArray();
    }

    public IntGrid2D getGrid() {
        return grid;
    }

    public int getScore(int player) {
        int ownStones = 0;
        int surroundedEmpty = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getWidth(); y++) {
                if (grid.get(x, y) == player) {
                    ownStones++;
                // empty intersections that are completely surrounded by only stones of that player
                } else if (grid.get(x, y) == EMPTY) {
                    boolean surrounded = true;
                    for (Int2D n : neighbours(new Int2D(x,y))) {
                        if (grid.get(n.x, n.y) != player) {
                            surrounded = false;
                            break;
                        }
                    }
                    if (surrounded) {
                        surroundedEmpty++;
                    }
                }
            }
        }
        int cap = captured[player];
        return ownStones + surroundedEmpty + cap;
        
        // TODO: komi
    }
    
}
