#!/bin/bash

for R in {0..9}
do
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_simplephototaxis_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=5 -p problem.maxObstacles=10 -p problem.minObstacleSize=20 -p problem.maxObstacleSize=30 -force
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_simplephototaxis_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.arenaSize=200 -p problem.minObjectDistance=100 -force
    
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_phototaxis_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_phototaxis_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=10 -p problem.maxObstacles=20 -force
    
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_freeforaging_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=5 -p problem.maxObstacles=10 -p problem.minObstacleSize=15 -p problem.maxObstacleSize=30 -force
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_freeforaging_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=30 -p problem.maxObjects=30 -force
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_freeforaging_sdbc_nsneat_$R"_direct/" -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=5 -p problem.maxObjects=5 -force
    
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_obsforaging_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_obsforaging_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=30 -p problem.maxObjects=30 -force
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_obsforaging_sdbc_nsneat_$R"_direct/" -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=5 -p problem.maxObjects=5 -force
    
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_exploration_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force 
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_exploration_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=10 -p problem.maxObstacles=10 -p problem.minObstacleSize=14 -p problem.maxObstacleSize=14 -force    
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_exploration_sdbc_nsneat_$R"_direct/" -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=2 -p problem.maxObstacles=2 -p problem.minObstacleSize=32 -p problem.maxObstacleSize=32 -force    
        
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_maze_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=10 -p problem.maxObstacles=10 -force 
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_maze_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=40 -p problem.maxObstacles=40 -p problem.maxObstacleSize=10 -force         
    
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_avoidance_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=5 -p problem.maxObjects=5 -force
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_avoidance_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=30 -p problem.maxObjects=30 -force     
    
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_predator_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=2 -p problem.maxObjects=2 -force 
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_predator_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.objectSpeed=0.75 -force 
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_predator_sdbc_nsneat_$R"_direct/" -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force 

    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_dynphototaxis_sdbc_nsneat_$R"_direct/" -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=2 -p problem.maxObjects=2 -force 
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_dynphototaxis_sdbc_nsneat_$R"_direct/" -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.objectSpeed=0.75 -force
    ./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_dynphototaxis_sdbc_nsneat_$R"_direct/" -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force 
done

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_simplephototaxis -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=5 -p problem.maxObstacles=10 -p problem.minObstacleSize=20 -p problem.maxObstacleSize=30 -force
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_simplephototaxis -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.arenaSize=200 -p problem.minObjectDistance=100 -force

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_phototaxis -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_phototaxis -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=10 -p problem.maxObstacles=20 -force

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_freeforaging -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=5 -p problem.maxObstacles=10 -p problem.minObstacleSize=15 -p problem.maxObstacleSize=30 -force
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_freeforaging -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=30 -p problem.maxObjects=30 -force
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_freeforaging -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=5 -p problem.maxObjects=5 -force

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_obsforaging -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_obsforaging -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=30 -p problem.maxObjects=30 -force
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_obsforaging -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=5 -p problem.maxObjects=5 -force

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_exploration -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force 
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_exploration -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=10 -p problem.maxObstacles=10 -p problem.minObstacleSize=14 -p problem.maxObstacleSize=14 -force
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_exploration -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=2 -p problem.maxObstacles=2 -p problem.minObstacleSize=32 -p problem.maxObstacleSize=32 -force

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_maze -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=10 -p problem.maxObstacles=10 -force 
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_maze -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=40 -p problem.maxObstacles=40 -p problem.maxObstacleSize=10 -force 

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_avoidance -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=5 -p problem.maxObjects=5 -force
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_avoidance -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=30 -p problem.maxObjects=30 -force 

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_predator -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=2 -p problem.maxObjects=2 -force 
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_predator -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.objectSpeed=0.75 -force 
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_predator -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force 

./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_dynphototaxis -prefix var1 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObjects=2 -p problem.maxObjects=2 -force 
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_dynphototaxis -prefix var2 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.objectSpeed=0.75 -force   
./run.sh mase.stat.BatchReevaluate -r 100 -recursive -f ~/exps/playground/tasks10/pl_dynphototaxis -prefix var3 -p eval.base.problem=mase.mason.MasonSimulationProblem -p problem.minObstacles=0 -p problem.maxObstacles=0 -force 

