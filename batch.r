#!/usr/bin/Rscript

source("runsource.r")

#defaultCall(file="src/mase/app/keepaway/keepaway_homo_nov.params", out=paste0("kw_hom_nov"), params=c(jobs=10))
#defaultCall(file="src/mase/app/keepaway/keepaway_hetero_nov.params", out=paste0("kw_het_nov"), params=c(jobs=10))
#defaultCall(file="src/mase/app/keepaway/keepaway_homo.params", out=paste0("kw_hom_fit"), params=c(jobs=10))#
#defaultCall(file="src/mase/app/keepaway/keepaway_hetero.params", out=paste0("kw_het_fit"), params=c(jobs=10))

#defaultCall(file="src/mase/app/keepaway/keepaway_homo_nov.params", out=paste0("kw_hom_nov_rnd"), params=c(jobs=10, "problem.takers-placement"="random-center", "problem.repetitions"=10))
#defaultCall(file="src/mase/app/keepaway/keepaway_homo.params", out=paste0("kw_hom_fit_rnd"), params=c(jobs=10, "problem.takers-placement"="random-center", "problem.repetitions"=10))

#defaultCall(file="src/mase/app/keepaway/keepaway_homo_nov.params", out=paste0("kw_hom_nov_cro"), params=c(jobs=10, "vector.mutate.source.0"="ec.vector.breed.VectorCrossoverPipeline", "vector.xover.source.0"="ec.select.TournamentSelection", "vector.xover.source.1"="ec.select.TournamentSelection", "vector.species.crossover-type"="one"))
#defaultCall(file="src/mase/app/keepaway/keepaway_homo.params", out=paste0("kw_hom_fit_cro"), params=c(jobs=10, "vector.mutate.source.0"="ec.vector.breed.VectorCrossoverPipeline", "vector.xover.source.0"="ec.select.TournamentSelection", "vector.xover.source.1"="ec.select.TournamentSelection", "vector.species.crossover-type"="one"))

#defaultCall(file="src/mase/app/keepaway/keepaway_homo_nov.params", out=paste0("kw_hom_nov_k7"), params=c(jobs=10, "select.tournament.size"=7))
#defaultCall(file="src/mase/app/keepaway/keepaway_homo.params", out=paste0("kw_hom_fit_k7"), params=c(jobs=10, "select.tournament.size"=7))

#defaultCall(file="src/mase/app/keepaway/keepaway_homo_nov.params", out=paste0("kw_hom_nov_cro_k7"), params=c(jobs=10, "vector.mutate.source.0"="ec.vector.breed.VectorCrossoverPipeline", "select.tournament.size"=7, "vector.xover.source.0"="ec.select.TournamentSelection", "vector.xover.source.1"="ec.select.TournamentSelection", "vector.species.crossover-type"="one"))
#defaultCall(file="src/mase/app/keepaway/keepaway_homo.params", out=paste0("kw_hom_fit_cro_k7"), params=c(jobs=10, "vector.mutate.source.0"="ec.vector.breed.VectorCrossoverPipeline", "select.tournament.size"=7, "vector.xover.source.0"="ec.select.TournamentSelection", "vector.xover.source.1"="ec.select.TournamentSelection", "vector.species.crossover-type"="one"))

defaultCall(file="src/mase/app/keepaway/keepaway_hetero_nov.params", out=paste0("kw_het_nov_k7"), params=c(jobs=10, "select.tournament.size"=7))
defaultCall(file="src/mase/app/keepaway/keepaway_hetero.params", out=paste0("kw_het_fit_k7"), params=c(jobs=10, "select.tournament.size"=7))
