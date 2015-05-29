#!/bin/bash

./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/EC/pred/fit_p2v10/config.params -f ~/exps/EC/pred/fit_p2v10 -f ~/exps/EC/pred/nsga_p2v10
./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/EC/pred/fit_p2v13/config.params -f ~/exps/EC/pred/fit_p2v13 -f ~/exps/EC/pred/nsga_p2v13
./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/EC/pred/fit_p5v4/config.params -f ~/exps/EC/pred/fit_p5v4 -f ~/exps/EC/pred/nsga_p5v4 
./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/EC/pred/fit_p5v13/config.params -f ~/exps/EC/pred/fit_p5v13 -f ~/exps/EC/pred/nsga_p5v13
./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/EC/pred/fit_p7v4/config.params -f ~/exps/EC/pred/fit_p7v4
./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/EC/pred/fit_p7v10/config.params -f ~/exps/EC/pred/fit_p7v10

