#!/usr/bin/Rscript

source("runsource.r")

#runCommandLine("-out pred5h_nov50 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=5 -p jobs=10 -p generations=500 -p eval.post.1.ns-blend=0.5 -p problem.escape-distance=10 -p problem.n-predators=5")

#runCommandLine("-out pred3h_fit -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=500")



runCommandLine("-out pred5m_nov50 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p jobs=10 -p generations=500 -p pop.subpops=5 -p problem.n-predators=5 -p eval.post.1.ns-blend=0.5 -p problem.escape-distance=7")
runCommandLine("-out pred5m_fit -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/coevolution.params -p pop.subpops=5 -p jobs=10 -p generations=500 -p problem.n-predators=5 -p problem.escape-distance=7")

#runCommandLine("-out pred3m_nov50 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=500 -p eval.post.1.ns-blend=0.5 -p problem.escape-distance=7")
#runCommandLine("-out pred3m_fit -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=500 -p problem.escape-distance=7")

#runCommandLine("-out pred5h_nov50 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=5 -p jobs=10 -p generations=250 -p eval.post.1.ns-blend=0.5 -p problem.escape-distance=10 -p problem.n-predators=5")