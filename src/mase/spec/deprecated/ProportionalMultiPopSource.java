/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author jorge
 */
public class ProportionalMultiPopSource extends MultiPopulationSource {

    @Override
    public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread) {
        SelectionPoolBuilder spb = (SelectionPoolBuilder) state.exchanger;
        Individual[] pool = spb.getPool(subpopulation);
        Integer[] origins = spb.getIndividualsOrigin(subpopulation);     
        //System.out.println(Arrays.toString(origins));
        
        int index = state.random[thread].nextInt(pool.length);
        int sub = origins[index];
        
        ArrayList<Individual> subPool = new ArrayList<Individual>(pool.length);
        for(int i = 0 ; i < pool.length ; i++) {
            if(sub == origins[i].intValue()) {
                subPool.add(pool[i]);
            }
        }
        
        int best;
        if(subPool.size() == 1) {
            best = index;
        } else {
            Individual[] subPoolArray = new Individual[subPool.size()];
            subPool.toArray(subPoolArray);
            best = tournament(subPoolArray, state, thread);
        }
        
        // stat
        if (subpopulation == sub) {
            spb.logPick(subpopulation);
        }

        inds[start] = pool[best];
        //System.out.println(best);
        return 1;
    }
    
    
    
}
