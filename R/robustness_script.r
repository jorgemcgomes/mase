### CCEA ROBUSTNESS ####################################################

setwd("~/exps/rob/")

# generalisation comparison ccea-best vs single (evolved without noise, post-evaluated with noises)
data <- rbind(
  loadData("*/*/bl", ids=list(Eval="BL"), filename="postfitness.stat", fun="loadFitness"),
  loadData("*/*/bl", ids=list(Eval="AN"), filename="anfitness.stat", fun="loadFitness"),
  loadData("*/*/bl", ids=list(Eval="EN"), filename="enfitness.stat", fun="loadFitness"),
  loadData("*/*/bl", ids=list(Eval="TVL"), filename="tvlfitness.stat", fun="loadFitness"),
  loadData("*/*/bl", ids=list(Eval="TVM"), filename="tvmfitness.stat", fun="loadFitness"),
  loadData("*/*/bl", ids=list(Eval="TVH"), filename="tvhfitness.stat", fun="loadFitness"),
  loadData("*/*/bl", ids=list(Eval="All"), filename="allfitness.stat", fun="loadFitness")
)
ggplot(lastGen(data), aes(x=Eval,y=BestSoFar,fill=ID2)) + geom_boxplot() + 
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) +
  ggtitle("CCEA vs Single pop.: Generalisation") + xlab("Post-evaluation task setup") + ylab("Highest fitness") +
  scale_fill_discrete(name="Evolutionary setup (no noise)") + theme(legend.position="bottom")

agg <- summaryBy(BestSoFar ~ ID1 + ID2 + Eval, lastGen(data), FUN=c(mean,se))
pd <- position_dodge(0)
ggplot(agg, aes(x=Eval,y=BestSoFar.mean,colour=ID2,group=ID2)) +
  geom_errorbar(aes(ymin=BestSoFar.mean-BestSoFar.se, ymax=BestSoFar.mean+BestSoFar.se), width=.5, position=pd) +
  geom_line(position=pd) + geom_point(position=pd) +
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) +
  ggtitle("CCEA vs Single pop.: Generalisation") + xlab("Post-evaluation task setup") + ylab("Highest fitness") +
  scale_colour_discrete(name="Evolutionary setup (no noise)") + theme(legend.position="bottom")


# generalisation with ccea setups (evolved without noise, post-evaluated with team variations noise)
data <- rbind(
  loadData("*/ccea/bl*", ids=list(Eval="BL"), filename="postfitness.stat", fun="loadFitness"),
  loadData("*/ccea/bl*", ids=list(Eval="AN"), filename="anfitness.stat", fun="loadFitness"),
  loadData("*/ccea/bl*", ids=list(Eval="EN"), filename="enfitness.stat", fun="loadFitness"),
  loadData("*/ccea/bl*", ids=list(Eval="TVL"), filename="tvlfitness.stat", fun="loadFitness"),
  loadData("*/ccea/bl*", ids=list(Eval="TVM"), filename="tvmfitness.stat", fun="loadFitness"),
  loadData("*/ccea/bl*", ids=list(Eval="TVH"), filename="tvhfitness.stat", fun="loadFitness"),
  loadData("*/ccea/bl*", ids=list(Eval="All"), filename="allfitness.stat", fun="loadFitness")
)
ggplot(lastGen(data), aes(x=Eval,y=BestSoFar,fill=ID3)) + geom_boxplot() + 
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) +
  ggtitle("CCEA configurations: Generalisation") + xlab("Post-evaluation task setup") + ylab("Highest fitness") +
  scale_fill_discrete(name="Evolutionary setup (no team variations)") + theme(legend.position="bottom")

agg <- summaryBy(BestSoFar ~ ID1 + ID3 + Eval, lastGen(data), FUN=c(mean,se))
pd <- position_dodge(0)
ggplot(agg, aes(x=Eval,y=BestSoFar.mean,colour=ID3,group=ID3)) +
  geom_errorbar(aes(ymin=BestSoFar.mean-BestSoFar.se, ymax=BestSoFar.mean+BestSoFar.se), width=.5, position=pd) +
  geom_line(position=pd) + geom_point(position=pd) +
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) +
  ggtitle("CCEA configurations: Generalisation") + xlab("Post-evaluation task setup") + ylab("Highest fitness") +
  scale_colour_discrete(name="Evolutionary setup (no noise)") + theme(legend.position="bottom")


# ccea setups comparison (fitness through time, no noise in post-evaluation)
agg <- summaryBy(BestSoFar ~ ID1 + ID3 + Generation, subset(data,Eval=="BL"), FUN=c(mean,se))
ggplot(agg, aes(Generation,BestSoFar.mean,group=ID3)) + geom_line(aes(colour=ID3)) + ylab("Fitness") +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1) +
  theme(legend.position="bottom") + facet_wrap(~ ID1, scales="free_y") + ggtitle("CCEA configurations: post-evaluation with no noise") +
  scale_colour_discrete(name="CCEA configuration")


# capability of evolving robust behaviours (evolved with noise, post-evalauted with the same noise)
data <- loadData(c("*/*/bl","*/*/an","*/*/en","*/*/tv","*/*/tvm","*/*/tvh","*/*/all"), filename="postfitness.stat", fun="loadFitness")
ggplot(lastGen(data), aes(x=ID3,y=BestSoFar,fill=ID2)) + geom_boxplot() + 
  facet_wrap(~ ID1, scales="free_y") + 
  ylim(0,NA) + xlab("Task setup (evolution and post-evaluation)") + ylab("Highest fitness") + scale_fill_discrete(name="Evolutionary setup") + 
  theme(legend.position="bottom") + ggtitle("CCEA vs Single: Evolution of robust controllers")

agg <- summaryBy(BestSoFar ~ ID1 + ID2 + ID3, lastGen(data), FUN=c(mean,se))
pd <- position_dodge(0)
ggplot(agg, aes(x=ID3,y=BestSoFar.mean,colour=ID2,group=ID2)) +
  geom_errorbar(aes(ymin=BestSoFar.mean-BestSoFar.se, ymax=BestSoFar.mean+BestSoFar.se), width=.5, position=pd) +
  geom_line(position=pd) + geom_point(position=pd) +
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) +
  ggtitle("CCEA vs Single: Evolution of robust controllers") + xlab("Task setup (evolution and post-evaluation)") + ylab("Highest fitness") +
  scale_colour_discrete(name="Evolutionary setup") + theme(legend.position="bottom")

