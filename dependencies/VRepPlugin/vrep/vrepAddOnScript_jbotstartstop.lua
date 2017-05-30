function dump(o)
   if type(o) == 'table' then
      local s = '{'
      for k,v in pairs(o) do
         if type(k) ~= 'number' then k = '"'..k..'"' end
         s = s .. '['..k..']' .. dump(v) .. ', '
      end
      return s .. '}'
   else
      return tostring(o)
   end
end

-- remote API execMode=...
-- just started: execMode=0 ; running: execMode=1 ; just paused: execMode=2 ; just resumed: execMode=3

-----  I/O OF THIS ADD-ON -----

-- TUBE (float[]) evaluation: the addon reads the evaluation results from this tube, after the simulation has been stopped
-- TUBE (float[]) sensing: the addon reads the sensor values from this tube every control step, if controlType != 0
-- TUBE (float[]) actuation: the addon writes the actuator values to this tube, every control step

-- SIGNAL string (float[]) globalparams: the addon writes the global parameters in this signal, at the begining of each sim
-- The global params (without length), as received from the fromClient signal. By convention, the first in the evaluation max time

-- SIGNAL string (float[]) fromClient: the addon reads the individuals from this signal and then clears it
-- <num_global_params> <values>*num_global_params <num_controllers> [<id> <controller_type> <length> <values>*length]*num_controllers
-- The first global param is used as the max. evaluation time
-- The controller type is the first element of the controller (counts for length)
-- If controller_type == 0, the controller values are used directly as actuator values
-- If controller_type != 0, the [type+controller values] are passed to the plugin libv_repExtPluginJBot to initialize the external java controller (simExtLoadController)
 
-- SIGNAL string (float[]) toClient: the addon writes the results to this signal
-- <num_evaluations> [<id> <length> <values>*length]*num_evaluations

execMode=...

if(execMode == 2 and simGetSimulationState() == sim_simulation_advancing_running) then
  simStopSimulation()
end

-- Add-on initialization
if(execMode==0 or execMode==3) then
  simClearStringSignal('fromClient')
  simClearStringSignal('toClient')
  simClearStringSignal('globalparams')
  
  individuals = {}
  results = {}
  waitForResults = false
end

-- Evaluation has ended because the time ended, signal simulation to stop
if(not waitForResult and simGetSimulationState() == sim_simulation_advancing_running and simGetSimulationTime() > evaluationMaxTime) then
  stopTime = simGetSimulationTime()
  simStopSimulation()
  waitForResults = true
end

-- Simulation has been terminated by other means
if(not waitForResults and (simGetSimulationState() == sim_simulation_advancing_abouttostop or simGetSimulationState() == sim_simulation_advancing_lastbeforestop)) then
  stopTime = simGetSimulationTime()
  waitForResults = true
end
  
-- Simulation has been signaled to stop, wait for the evaluation results  
if(waitForResults) then
  evalRes = simTubeRead(evaluationTube)
  if(evalRes ~= nil) then
    unres = simUnpackFloats(evalRes)
    table.insert(results, #unres)
    for i=1,#unres,1 do
      table.insert(results, unres[i])
    end
    
    waitForResults=false
    simTubeClose(evaluationTube)
    simTubeClose(sensingTube)
    simTubeClose(actuationTube)      
    print('Finished evaluation', stopTime, simGetSystemTimeInMs(cpuStartTime))
  else
    -- do not move forward until we get the results
    print('Waiting for results')
    return
  end  
end


-- No evaluation running and no more individuals to evaluate
if(simGetSimulationState() == sim_simulation_stopped and (currentIndividual == numIndividuals or #individuals == 0)) then
  if(#individuals > 0) then
    print('WARNING! Part of the batch was left.','Num individuals:',numIndividuals,'Floats left:',#individuals)
  end
  
  -- send available results and reset
  if(#results > 0) then
    simSetStringSignal('toClient',simPackFloats(results,0,0))
    print('Sending',#results,'floats to client')
    results = {}
  end

  -- retrieve new individuals
  dat=simGetStringSignal('fromClient')
  if(dat ~= nil) then
    -- read from client
    individuals=simUnpackFloats(dat,0,0,0)
    simClearStringSignal('fromClient')

    print('Received',#individuals,'floats from client')
    --print(dump(individuals))
        
    -- load global parameters
    numParams=table.remove(individuals,1)
    params={}
    for i=1,numParams,1 do
      params[i]=table.remove(individuals,1)
    end
    evaluationMaxTime=params[1] -- trust-based
    simSetStringSignal('globalparams',simPackFloats(params))

    numIndividuals=table.remove(individuals,1)
    print('Going to evaluate',numIndividuals)
    currentIndividual=0
  end
  
  if(numIndividuals == 0 or #individuals == 0) then
    -- still no individuals to evaluate, nothing to do, stop here
    print('Waiting for individuals')
    return
  end
  
  -- init results
  simClearStringSignal('toClient')
  results = {numIndividuals}
end

-- Start new evaluation
if(simGetSimulationState() == sim_simulation_stopped) then
  print('-----------------------------------')
  cpuStartTime=simGetSystemTimeInMs(-1)   
  individualId=table.remove(individuals,1)
  currentIndividual = currentIndividual + 1
  table.insert(results,individualId)

  -- reset the robot to the initial state
  evaluationTube = simTubeOpen(0,'evaluation',1)
  sensingTube = simTubeOpen(0,'sensing',1)
  actuationTube = simTubeOpen(0,'actuation',1)  
  simStartSimulation()

  -- load controller
  contrType = table.remove(individuals,1)
  length = table.remove(individuals,1)
  contrParams = {}
  for i=1,length,1 do
    table.insert(contrParams, table.remove(individuals,1))
  end
  print('Eval time',evaluationMaxTime,'Control params', #contrParams)
  simExtLoadController(0,contrType,contrParams) -- ask plugin to load the controller; 0 is the robot handle (id)

  print('Starting evaluation of',individualId, 'Progress:',currentIndividual, '/', numIndividuals, #individuals)
end


-- Regular time step
if(simGetSimulationState() == sim_simulation_advancing_firstafterstop or simGetSimulationState() == sim_simulation_advancing_running) then
  inp = simTubeRead(sensingTube)
  if(inp ~= nil) then
    inputValues = simUnpackFloats(inp)
    actuatorValues = simExtControlStep(0, inputValues)
  else
    actuatorValues = simExtControlStep(0, {})
  end
  if(actuatorValues ~= nil) then
    simTubeWrite(actuationTube,simPackFloats(actuatorValues))
  end
end
