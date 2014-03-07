/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sim.field.grid.IntGrid2D;
import sim.util.Int2D;

/**
 *
 * @author Jorge
 */
public class GoState implements Cloneable {

    public static final int BLACK = 0, WHITE = 1, EMPTY = 2;
    protected int[] grid;
    protected List<Group>[] groups;
    protected int[] captured;
    protected int[] surrounded;
    protected int[] possession;

    protected static Int2D[][] neighbourMap;
    protected static int size;

    private GoState() {
    }

    public GoState(int size) {
        this.grid = new int[size * size];
        Arrays.fill(grid, EMPTY);
        this.groups = new List[]{new ArrayList<Group>(20), new ArrayList<Group>(20)};
        this.captured = new int[]{0, 0};
        this.surrounded = new int[]{0,0};
        this.possession = new int[]{0,0};
        synchronized (this) {
            GoState.size = size;
            if (neighbourMap == null) {
                initNeighbours(size);
            }
        }
    }

    private static void initNeighbours(int size) {
        neighbourMap = new Int2D[size * size][];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                ArrayList<Int2D> ns = new ArrayList<Int2D>(4);
                if (x > 0) {
                    ns.add(new Int2D(x - 1, y));
                }
                if (x < size - 1) {
                    ns.add(new Int2D(x + 1, y));
                }
                if (y > 0) {
                    ns.add(new Int2D(x, y - 1));
                }
                if (y < size - 1) {
                    ns.add(new Int2D(x, y + 1));
                }
                Int2D[] a = new Int2D[ns.size()];
                ns.toArray(a);
                int index = x % size + y * size;
                neighbourMap[index] = a;
            }
        }
    }

    public static int index(Int2D pos) {
        return index(pos.x, pos.y);
    }

    public static int index(int x, int y) {
        return x % size + y * size;
    }
    
    public static Int2D reverseIndex(int index) {
        return new Int2D(index % size, index / size);
    }

    private static Int2D[] neighbours(Int2D pos) {
        return neighbourMap[index(pos)];
    }

    protected static class Group implements Cloneable {

        protected ArrayList<Int2D> stones;
        protected ArrayList<Int2D> liberties;

        Group() {
            this.stones = new ArrayList<Int2D>();
            this.liberties = new ArrayList<Int2D>();
        }

        @Override
        protected Group clone() throws CloneNotSupportedException {
            Group newGroup = (Group) super.clone();
            newGroup.stones = (ArrayList<Int2D>) stones.clone();
            newGroup.liberties = (ArrayList<Int2D>) liberties.clone();
            return newGroup;
        }

        protected boolean contains(Int2D stone) {
            for (Int2D s : stones) {
                if (s.x == stone.x && s.y == stone.y) {
                    return true;
                }
            }
            return false;
        }

        // WARNING: might have repeated liberties
        private void updateGroupLiberties(int[] grid) {
            liberties.clear();
            for (Int2D p : stones) {
                for (Int2D n : neighbours(p)) {
                    if (grid[index(n)] == EMPTY) {
                        liberties.add(n);
                    }
                }
            }
        }
    }

    @Override
    protected GoState clone() throws CloneNotSupportedException {
        // deep clone
        GoState newState = (GoState) super.clone();
        newState.grid = Arrays.copyOf(grid, grid.length);
        newState.captured = Arrays.copyOf(captured, 2);
        newState.groups = new List[2];
        for (int i = 0; i < newState.groups.length; i++) {
            newState.groups[i] = new ArrayList<Group>(groups[i].size());
            for (Group g : this.groups[i]) {
                newState.groups[i].add(g.clone());
            }
        }
        return newState;
    }

    /*
     * Returns null if not a valid play
     */
    public GoState nextState(int player, Int2D pos) {
        // position already occupied
        if (grid[index(pos)] != EMPTY) {
            return null;
        }

        // place stone
        GoState newState = null;
        try {
            newState = this.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(GoState.class.getName()).log(Level.SEVERE, null, ex);
        }
        newState.grid[index(pos)] = player;

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
                if (next.contains(n)) {
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
            next.updateGroupLiberties(newState.grid);
            // remove group if no liberties
            if (next.liberties.isEmpty()) {
                iter.remove();
                newState.captured[player] += next.stones.size();
                for (Int2D p : next.stones) {
                    newState.grid[index(p)] = EMPTY;
                }
            }
        }

        /* Check for self-capture
         * A player may not self-capture, that is play a stone into a position where 
         * it would have no liberties or form part of a group which would thereby have 
         * no liberties, unless, as a result, one or more of the stones surrounding it is captured.*/
        // update player liberties
        for (Group g : newState.groups[player]) {
            g.updateGroupLiberties(newState.grid);
            // if there is any player group without liberties, the play is invalid
            if (g.liberties.isEmpty()) {
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
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (grid[index(x, y)] == EMPTY) {
                    GoState next = nextState(player, new Int2D(x, y));
                    // check for invalid states
                    if (next != null) {
                        states.add(next);
                    }
                }
            }
        }

        GoState[] res = new GoState[states.size()];
        states.toArray(res);
        return res;
    }

    public int[] getBoardDescription() {
        return grid;
    }

    public int getScore(int player) {
        possession[player] = 0;
        surrounded[player] = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (grid[index(x, y)] == player) {
                    possession[player]++;
                    // empty intersections that are completely surrounded by only stones of that player
                } else if (grid[index(x, y)] == EMPTY) {
                    boolean sur = true;
                    for (Int2D n : neighbours(new Int2D(x, y))) {
                        if (grid[index(n)] != player) {
                            sur = false;
                            break;
                        }
                    }
                    if (sur) {
                        surrounded[player]++;
                    }
                }
            }
        }
        return possession[player];
    }

    public IntGrid2D asGrid2D() {
        IntGrid2D g = new IntGrid2D(size, size);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                g.set(x, y, grid[index(x, y)]);
            }
        }
        return g;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof GoState) {
            GoState other = (GoState) obj;
            return Arrays.equals(this.grid, other.grid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Arrays.hashCode(this.grid);
        return hash;
    }
}
