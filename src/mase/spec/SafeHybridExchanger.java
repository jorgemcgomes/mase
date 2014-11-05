/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

/**
 *
 * @author jorge
 */
public class SafeHybridExchanger extends BasicHybridExchanger {

    public static final String P_FOREIGN_MODE = "foreign-mode";
    public static final String P_DIFFERENCE_MODE = "difference-mode";
    PickMode foreignMode;
    DifferenceMode differenceMode;
    Set<MetaPopulation> newChilds;

    public enum DifferenceMode {

        mean, max, utest
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.foreignMode = PickMode.valueOf(state.parameters.getString(base.push(P_FOREIGN_MODE), null));
        this.differenceMode = DifferenceMode.valueOf(state.parameters.getString(base.push(P_DIFFERENCE_MODE), null));
        newChilds = new HashSet<MetaPopulation>();
    }

    @Override
    protected void splitProcess(EvolutionState state) {
        // Check on current ongoing splits
        checkSplits(state);
        super.splitProcess(state);
    }

    protected void checkSplits(EvolutionState state) {
        Iterator<MetaPopulation> iter = newChilds.iterator();
        while (iter.hasNext()) {
            MetaPopulation child = iter.next();
            if (child.age >= stabilityTime) {
                MetaPopulation parent = child.foreigns.get(0).origin;
                double diff = fitnessDifference(parent, child, state);
                if (diff <= mergeThreshold) { // The split will be reverted
                    state.output.message("*************************** SPLIT REVERTED: " + child + " and " + parent + " ***************************");
                    remerges++;
                    parent.agents.addAll(child.agents);
                    metaPops.remove(child);
                } else { // The split holds    
                    child.foreigns.clear();
                }
                iter.remove();
            }
        }
    }

    @Override
    protected MetaPopulation fork(MetaPopulation parent, EvolutionState state) {
        MetaPopulation child = super.fork(parent, state);
        parent.foreigns.clear();
        child.foreigns.clear();
        child.foreigns.add(new Foreign(parent));
        newChilds.add(child);
        return child;
    }

    @Override
    protected void mergeProcess(EvolutionState state) {
        super.mergeProcess(state);

        // add foreign individuals to the populations outside the stability threshold
        for (MetaPopulation mp : metaPops) {
            if (mp.age >= stabilityTime) {
                HashSet<MetaPopulation> current = new HashSet<MetaPopulation>();
                for (MetaPopulation mpf : metaPops) {
                    if (mp != mpf && mpf.age >= stabilityTime) {
                        current.add(mpf);
                    }
                }
                Iterator<Foreign> iter = mp.foreigns.iterator();
                while(iter.hasNext()) {
                    Foreign next = iter.next();
                    if(current.contains(next.origin)) {
                        current.remove(next.origin);
                    } else {
                        iter.remove();
                    }
                }
                for(MetaPopulation mpf : current) {
                    mp.foreigns.add(new Foreign(mpf));
                }
            }
        }
    }

    @Override
    protected double[][] computeMetapopDistances(EvolutionState state) {
        double[][] distances = new double[metaPops.size()][metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp1 = metaPops.get(i);
            for (int j = i + 1; j < metaPops.size(); j++) {
                MetaPopulation mp2 = metaPops.get(j);
                if (mp1.age >= stabilityTime && mp2.age >= stabilityTime) {
                    double diff1 = fitnessDifference(mp1, mp2, state);
                    double diff2 = fitnessDifference(mp2, mp1, state);
                    //distances[i][j] = distances[j][i] = (diff1 + diff2) / 2;
                    distances[i][j] = distances[j][i] = Math.min(diff1, diff2);
                } else {
                    distances[i][j] = distances[j][i] = Double.POSITIVE_INFINITY;
                }
            }
            distances[i][i] = 0;
        }

        return distances;
    }

    /*
     Fitness difference of other to reference
     */
    private double fitnessDifference(MetaPopulation reference, MetaPopulation other, EvolutionState state) {
        // search foreign of reference in other
        Foreign f = null;
        for (Foreign fOther : other.foreigns) {
            if (fOther.origin == reference) {
                f = fOther;
                break;
            }
        }
        if (f == null || f.inds == null) {
            return Double.POSITIVE_INFINITY;
        }

        // pick individuals from the reference population, using the same method used to pick the foreign individuals
        Individual[] refInds = pickIndividuals(reference.inds, f.inds.length, foreignMode, state);

        if (differenceMode == DifferenceMode.mean) {
            double refFit = 0;
            for (Individual i : refInds) {
                refFit += i.fitness.fitness();
            }
            double foreignFit = 0;
            for (Individual i : f.inds) {
                foreignFit += i.fitness.fitness();
            }
            return refFit == 0 ? Double.POSITIVE_INFINITY : Math.abs(refFit - foreignFit) / Math.abs(refFit);
        } else if (differenceMode == DifferenceMode.max) {
            double refFit = Double.NEGATIVE_INFINITY;
            for (Individual i : refInds) {
                refFit = Math.max(refFit, i.fitness.fitness());
            }
            double foreignFit = Double.NEGATIVE_INFINITY;
            for (Individual i : f.inds) {
                foreignFit = Math.max(foreignFit, i.fitness.fitness());
            }
            return refFit == 0 ? Double.POSITIVE_INFINITY : Math.abs(refFit - foreignFit) / Math.abs(refFit);
        } else if (differenceMode == DifferenceMode.utest) {
            double[] refFits = new double[refInds.length];
            double[] forFits = new double[f.inds.length];
            for (int i = 0; i < refFits.length; i++) {
                refFits[i] = refInds[i].fitness.fitness();
            }
            for (int i = 0; i < forFits.length; i++) {
                forFits[i] = f.inds[i].fitness.fitness();
            }
            MannWhitneyUTest test = new MannWhitneyUTest();
            return test.mannWhitneyU(refFits, forFits) / (refFits.length * forFits.length);
        } else {
            return Double.NaN;
        }
    }

    @Override
    protected void importForeignPreBreed(EvolutionState state) {
        int foreignPool = (int) Math.round(elitePortion * popSize);
        for (MetaPopulation mp : metaPops) {
            for (Foreign f : mp.foreigns) {
                Individual[] foreignInds = pickIndividuals(f.origin.inds, foreignPool, foreignMode, state);
                f.inds = new Individual[foreignInds.length];
                for (int i = 0; i < foreignInds.length; i++) {
                    f.inds[i] = (Individual) foreignInds[i].clone();
                }
            }
        }
    }
}
