#!/bin/bash

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/ecal2/down_tog/nsmix/config.params -f ~/exps/ecal2/down_tog/nsmix -f ~/exps/ecal2/down_tog/moea -f ~/exps/ecal2/down_tog/staged

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/ecal2/down_sep/nsmix/config.params -f ~/exps/ecal2/down_sep/nsmix -f ~/exps/ecal2/down_sep/moea -f ~/exps/ecal2/down_sep/staged

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/ecal2/stable_sep/nsmix/config.params -f ~/exps/ecal2/stable_sep/nsmix -f ~/exps/ecal2/stable_sep/moea -f ~/exps/ecal2/stable_sep/staged

