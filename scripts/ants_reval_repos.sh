#!/bin/bash
for JOB in {0..19}
do
    for REPO in ~/exps/swarm/repfinal/*
    do
        for TASK in agg coverage bordercoverage cluster dispersion flocking phototaxis dynphototaxis
        do
            ./run.sh mase.stat.BatchReevaluate -r 10 -file ~/exps/swarm/tasksfinal/$TASK/config.params -f $REPO/job.$JOB.collection.tar.gz -prefix $TASK
        done
    done
done
