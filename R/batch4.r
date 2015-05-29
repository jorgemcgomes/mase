#!/usr/bin/Rscript

mazes <- c("hard","star","subset","zigzag","open","multi")
crossover <- c(0.4, 0.8)
for (c in crossover) {
    for (m in mazes) {		
        command <- paste0("./voyager.sh -out /home/jorge/exps/maze3/ea/crossover/",m,"/ga_fit_c",c,"/ -p jobs=30 -p parent.0=build/classes/mase/app/maze/maze.params -p parent.1=build/classes/mase/gax2.params -p neural.structure=elman -p neural.hidden=10 -p vector.species.genome-size=232 -p generations=750 -p pop.default-subpop.size=200 -p problem.maze=",m,".svg -p problem.eval.0=mase.app.maze.MazeFitness -p vector.xover.likelihood=",c)
		print(command)
		system(command)
    }
}

