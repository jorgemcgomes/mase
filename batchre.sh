#!/bin/bash

./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/fl4/down/fit/config.params -f ~/exps/fl4/down/fit -f ~/exps/fl4/down/ns -f ~/exps/fl4/down/inc -f ~/exps/fl4/down/staged -f ~/exps/fl4/down/halted

./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/fl4/stable_sep/fit/config.params -f ~/exps/fl4/stable_sep/fit -f ~/exps/fl4/stable_sep/ns -f ~/exps/fl4/stable_sep/inc -f ~/exps/fl4/stable_sep/staged -f ~/exps/fl4/stable_sep/halted

./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/fl4/down_sep/fit/config.params -f ~/exps/fl4/down_sep/fit -f ~/exps/fl4/down_sep/ns -f ~/exps/fl4/down_sep/inc -f ~/exps/fl4/down_sep/staged -f ~/exps/fl4/down_sep/halted

./run.r mase.mason.MasterReevaluate -r 50 -file ~/exps/fl4/stable/fit/config.params -f ~/exps/fl4/stable/fit -f ~/exps/fl4/stable/ns -f ~/exps/fl4/stable/inc



