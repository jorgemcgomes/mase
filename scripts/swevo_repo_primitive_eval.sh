#!/bin/bash
for JOB in {0..29}
do
    for REPO in ~/exps/playground2/rep/*
    do
        for TASK in avoidance tracking exploration foraging obsforaging maze phototaxis obsphototaxis prey
        do
            ./run.sh mase.stat.BatchReevaluate -r 10 -file ~/exps/playground2/tasks/direct_$TASK/config.params -f $REPO/job.$JOB.finalarchive.tar.gz -prefix $TASK
        done
    done
done
