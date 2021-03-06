/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Group;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.Collections;
import org.neat4j.core.AIConfig;
import org.neat4j.neat.core.NEATChromosome;
import org.neat4j.neat.core.NEATConfig;
import org.neat4j.neat.core.NEATGADescriptor;
import org.neat4j.neat.core.NEATGATrainingManager;
import org.neat4j.neat.core.NEATGeneticAlgorithm;
import org.neat4j.neat.core.mutators.NEATMutator;
import org.neat4j.neat.core.pselectors.TournamentSelector;
import org.neat4j.neat.core.xover.NEATCrossover;

/**
 *
 * @author jorge
 */
public class NEATSubpop extends Subpopulation {

    public static final String P_NEAT_BASE = "neat";

    public static final String[] NEAT_PARAMETERS = {"PROBABILITY.MUTATION",
        "PROBABILITY.CROSSOVER", "PROBABILITY.ADDLINK", "PROBABILITY.ADDNODE",
        "PROBABILITY.MUTATEBIAS", "PROBABILITY.TOGGLELINK", "PROBABILITY.WEIGHT.REPLACED",
        "EXCESS.COEFFICIENT", "DISJOINT.COEFFICIENT", "WEIGHT.COEFFICIENT", "COMPATABILITY.THRESHOLD",
        "COMPATABILITY.CHANGE", "SPECIE.COUNT", "SURVIVAL.THRESHOLD", "SPECIE.AGE.THRESHOLD",
        "SPECIE.YOUTH.THRESHOLD", "SPECIE.OLD.PENALTY", "SPECIE.YOUTH.BOOST", "SPECIE.FITNESS.MAX",
        "OPERATOR.XOVER", "OPERATOR.FUNCTION", "OPERATOR.PSELECTOR", "OPERATOR.MUTATOR",
        "MAX.PERTURB", "MAX.BIAS.PERTURB", "FEATURE.SELECTION", "RECURRENCY.ALLOWED",
        "INPUT.NODES", "OUTPUT.NODES", "ELE.EVENTS", "ELE.SURVIVAL.COUNT", "ELE.EVENT.TIME",
        "KEEP.BEST.EVER", "EXTRA.FEATURE.COUNT", "NATURAL.ORDER.STRATEGY", "TERMINATION.VALUE", 
        "COPY.BEST", "PROBABILITY.FEATURE.REPLACED", "FEATURE.PERTURB"};
    private static final long serialVersionUID = 1L;
    private NEATGeneticAlgorithm neat;

    // parameters
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        // load neat parameters
        Parameter df = defaultBase();
        AIConfig config = new NEATConfig();
        for (String key : NEAT_PARAMETERS) {
            if (state.parameters.exists(base.push(key), df.push(key))) {
                String val = state.parameters.getString(base.push(key), df.push(key));
                config.updateConfig(key, val);
            } else {
                state.output.warning("Parameter not found.", base.push(key), df.push(key));
            }
        }
        config.updateConfig("POP.SIZE", Integer.toString(individuals.length));

        NEATGATrainingManager gam = new NEATGATrainingManager();

        NEATInitializer init = (NEATInitializer) state.initializer;
        if (init.innovDB == null) {
            neat = new NEATGeneticAlgorithm((NEATGADescriptor) gam.createDescriptor(config), state.random[0].nextLong());
        } else {
            neat = new NEATGeneticAlgorithm((NEATGADescriptor) gam.createDescriptor(config), init.innovDB);
        }

        neat.pluginFitnessFunction(new PreEvaluatedFitnessFunction(Collections.EMPTY_MAP));
        neat.pluginCrossOver(new NEATCrossover(state.random[0].nextLong()));
        neat.pluginMutator(new NEATMutator(state.random[0].nextLong()));
        neat.pluginParentSelector(new TournamentSelector(state.random[0].nextLong()));
    }
    

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_NEAT_BASE);
    }

    public NEATGeneticAlgorithm getNEAT() {
        return neat;
    }

    @Override
    public void populate(EvolutionState state, int thread) {
        neat.innovationDatabase().reset();
        neat.createPopulation();
        for (int j = 0; j < individuals.length; j++) {
            individuals[j] = species.newIndividual(state, 0);
            ((NEATIndividual) individuals[j]).setChromosome((NEATChromosome) neat.population().genoTypes()[j]);
        }
    }

    @Override
    public Group emptyClone() {
        try {
            NEATSubpop p = (NEATSubpop) super.clone();
            p.species = species;  // don't throw it away...maybe this is a bad idea...
            p.individuals = new Individual[individuals.length];  // empty
            p.neat = neat;
            return p;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

}
