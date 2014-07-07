#!/bin/bash

./run.r mase.mason.MasterReevaluate -file ~/exps/pred/fit_e4_p2/config.params -f ~/exps/pred/fit_e4_p2_r2 -f ~/exps/pred/fit_e4_p2_r5 -f ~/exps/pred/fit_e4_p2_r10 -r 50

./run.r mase.mason.MasterReevaluate -file ~/exps/pred/fit_e7/config.params -f ~/exps/pred/fit_e7_r2 -f ~/exps/pred/fit_e7_r5 -f ~/exps/pred/fit_e7_r10 -r 50

