/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.simple.SimpleBreeder;
import mase.spec.AbstractHybridExchanger.MetaPopulation;

/**
 *
 * @author jorge
 */
public class SkipNewBreeder extends SimpleBreeder {
    
    private static final long serialVersionUID = 1L;

    @Override
    public boolean shouldBreedSubpop(EvolutionState state, int subpop, int threadnum) {
        AbstractHybridExchanger exc = (AbstractHybridExchanger) state.exchanger;
        if(exc.getMetaPopulations() != null) {
            MetaPopulation mp = exc.getMetaPopulations().get(subpop);
            return mp.age > 0 && super.shouldBreedSubpop(state, subpop, threadnum);
        }
        // can happen in the first generation (this is called by the shouldEvaluate)
        return super.shouldBreedSubpop(state, subpop, threadnum);
    }
}
