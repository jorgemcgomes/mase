/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.controllers.GroupController;

/**
 *
 * @author jorge
 */
public class DefaultMasonSimulator<T> extends MasonSimulationProblem<MasonSimState<T>> {

    private static final long serialVersionUID = 1L;
    public static final String P_SIMSTATE = "simstate";
    public static final String P_PARAMS = "params";
    private Class<MasonSimState> simClass;
    private T params;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        
        if(state.parameters.exists(base.push(P_PARAMS), defaultBase().push(P_PARAMS))) {
            params = (T) state.parameters.getInstanceForParameter(base.push(P_PARAMS), defaultBase().push(P_PARAMS), Object.class);
            ParamUtils.autoSetParameters(params, state, base, defaultBase(), true);
        }
                
        simClass = state.parameters.getClassForParameter(base.push(P_SIMSTATE), defaultBase().push(P_SIMSTATE), MasonSimState.class);
    }
    
    
    @Override
    protected MasonSimState createSimState(GroupController gc, long seed) {
        try {
            Constructor<MasonSimState> constructor = simClass.getConstructor(GroupController.class, long.class, params.getClass());
            MasonSimState newInstance = constructor.newInstance(gc, seed, params);
            return newInstance;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(DefaultMasonSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new RuntimeException("Unable to create sim state");
    }
    
}
