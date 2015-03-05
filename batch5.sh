#!/bin/bash

./run.r mase.mason.MasterReevaluate -file ~/exps/fl4/down/fit/config.params -f ~/exps/fl4/down/fit

./run.r mase.mason.MasterReevaluate -file ~/exps/fl4/down_sep/fit/config.params -f ~/exps/fl4/down_sep/fit

./run.r mase.mason.MasterReevaluate -file ~/exps/fl4/stable_sep/fit/config.params -f ~/exps/fl4/stable_sep/ns -f ~/exps/fl4/stable_sep/inc

