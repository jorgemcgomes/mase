#!/bin/bash

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/jbot/foraging2/fit/config.params -f ~/exps/jbot/foraging2/nsga -f ~/exps/jbot/foraging2/fit

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/jbot/foraging2/hom_fit/config.params -f ~/exps/jbot/foraging2/hom_nsga -f ~/exps/jbot/foraging2/hom_fit

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/jbot/herding_rb/fit/config.params -f ~/exps/jbot/herding_rb/nsga -f ~/exps/jbot/herding_rb/fit

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/jbot/herding_rb/hom_fit/config.params -f ~/exps/jbot/herding_rb/hom_nsga -f ~/exps/jbot/herding_rb/hom_fit

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/jbot/predprey_rb/fit/config.params -f ~/exps/jbot/predprey_rb/nsga -f ~/exps/jbot/predprey_rb/fit

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/jbot/predprey_rb/hom_fit/config.params -f ~/exps/jbot/predprey_rb/hom_nsga -f ~/exps/jbot/predprey_rb/hom_fit


