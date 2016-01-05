#!/usr/bin/Rscript

for (k in c(100)) {
	for (g in c(0.05)) {
		for(a in c("novel","random")) {
			for (m in c("hard","star","subset","zigzag","open","multi")) {	
			    	command <- paste0("./dell.sh -out /home/jorge/exps/maze3/growth/",m,"/ns_k",k,"_a",a,"_g",g,"/ -p jobs=30 -p parent.0=build/classes/mase/app/maze/maze.params -p parent.1=build/classes/mase/novelty/novelty.params -p parent.2=build/classes/mase/neat/neat.params -p generations=750 -p pop.default-subpop.size=200 -p problem.maze=",m,".svg -p fitness.novelty-index=1 -p eval.post.0.knn=",k," -p eval.post.0.archive-criteria=",a," -p eval.post.0.archive-growth=",g)
				print(command)
				system(command)
			}
		}
	}
}

