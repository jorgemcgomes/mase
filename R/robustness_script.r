### CCEA ROBUSTNESS ####################################################

setwd("~/exps/rob/")

# generalisation comparison ccea-best vs single (evolved without noise, post-evaluated with noises)
data <- metaLoadData("*/*/bl", filenames=c("postfitness.stat","enfitness.stat","tvlfitness.stat","tvmfitness.stat","tvhfitness.stat","allfitness.stat"), 
                     filename.ids=c("BL","AN","EN","TVL","TVM","TVH","All"), fun="loadFitness")

ggplot(lastGen(data), aes(x=File,y=BestSoFar,fill=ID2)) + geom_boxplot() + 
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) + geom_jitter(position=position_jitterdodge(jitter.width=.1),colour="grey",size=1) + 
  ggtitle("CCEA vs Single pop.: Generalisation") + xlab("Post-evaluation task setup") + ylab("Highest fitness") +
  scale_fill_discrete(name="Evolutionary setup (no noise)") + theme(legend.position="bottom")
ggsave("box_ccea_single_generalisation.pdf", width=8, height=5)

agg <- summaryBy(BestSoFar ~ ID1 + ID2 + File, lastGen(data), FUN=c(mean,se))
pd <- position_dodge(0)
ggplot(agg, aes(x=File,y=BestSoFar.mean,colour=ID2,group=ID2)) +
  geom_errorbar(aes(ymin=BestSoFar.mean-BestSoFar.se, ymax=BestSoFar.mean+BestSoFar.se), width=.5, position=pd) +
  geom_line(position=pd) + geom_point(position=pd) +
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) +
  ggtitle("CCEA vs Single pop.: Generalisation") + xlab("Post-evaluation task setup") + ylab("Highest fitness") +
  scale_colour_discrete(name="Evolutionary setup (no noise)") + theme(legend.position="bottom")
ggsave("sum_ccea_single_generalisation.pdf", width=8, height=5)


# generalisation with ccea setups (evolved without noise, post-evaluated with team variations noise)
data <- metaLoadData("*/ccea/bl*", filenames=c("postfitness.stat","enfitness.stat","tvlfitness.stat","tvmfitness.stat","tvhfitness.stat","allfitness.stat"), 
                     filename.ids=c("BL","AN","EN","TVL","TVM","TVH","All"), fun="loadFitness")

ggplot(lastGen(data), aes(x=File,y=BestSoFar,fill=ID3)) + geom_boxplot() + 
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) + geom_jitter(position=position_jitterdodge(jitter.width=.1),colour="grey",size=1) + 
  ggtitle("CCEA configurations: Generalisation") + xlab("Post-evaluation task setup") + ylab("Highest fitness") +
  scale_fill_discrete(name="Evolutionary setup (no team variations)") + theme(legend.position="bottom")
ggsave("box_ccea_configs_generalisation.pdf", width=12, height=5)

agg <- summaryBy(BestSoFar ~ ID1 + ID3 + File, lastGen(data), FUN=c(mean,se))
pd <- position_dodge(0)
ggplot(agg, aes(x=File,y=BestSoFar.mean,colour=ID3,group=ID3)) +
  geom_errorbar(aes(ymin=BestSoFar.mean-BestSoFar.se, ymax=BestSoFar.mean+BestSoFar.se), width=.5, position=pd) +
  geom_line(position=pd) + geom_point(position=pd) +
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) +
  ggtitle("CCEA configurations: Generalisation") + xlab("Post-evaluation task setup") + ylab("Highest fitness") +
  scale_colour_discrete(name="Evolutionary setup (no noise)") + theme(legend.position="bottom")
ggsave("sum_ccea_configs_generalisation.pdf", width=8, height=5)


# ccea setups comparison (fitness through time, no noise in post-evaluation)
agg <- summaryBy(BestSoFar ~ ID1 + ID3 + Generation, subset(data,File=="BL"), FUN=c(mean,se))
ggplot(agg, aes(Generation,BestSoFar.mean,group=ID3)) + geom_line(aes(colour=ID3)) + ylab("Fitness") +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1) +
  theme(legend.position="bottom") + facet_wrap(~ ID1, scales="free_y") + ggtitle("CCEA configurations: post-evaluation with no noise") +
  scale_colour_discrete(name="CCEA configuration")
ggsave("lines_ccea_configs_evolution.pdf", width=8, height=5)

ggplot(lastGen(subset(data,File=="BL")), aes(x=ID3,y=BestSoFar,fill=ID3)) + geom_boxplot() + 
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) + geom_jitter(position=position_jitterdodge(jitter.width=.1),colour="grey",size=1) + 
  ggtitle("CCEA configurations: post-evaluation with no noise") + xlab("CCEA configuration") + ylab("Highest fitness") +
  scale_fill_discrete(name="CCEA configuration") + theme(legend.position="bottom")
ggsave("box_ccea_configs_evolution.pdf", width=8, height=5)


# capability of evolving robust behaviours (evolved with noise, post-evalauted with the same noise)
data <- loadData(c("*/*/bl","*/*/an","*/*/en","*/*/tv","*/*/tvm","*/*/tvh","*/*/all"), filename="postfitness.stat", fun="loadFitness")
ggplot(lastGen(data), aes(x=ID3,y=BestSoFar,fill=ID2)) + geom_boxplot() + 
  facet_wrap(~ ID1, scales="free_y") + geom_jitter(position=position_jitterdodge(jitter.width=.1),colour="grey",size=1) + 
  ylim(0,NA) + xlab("Task setup (evolution and post-evaluation)") + ylab("Highest fitness") + scale_fill_discrete(name="Evolutionary setup") + 
  theme(legend.position="bottom") + ggtitle("CCEA vs Single: Evolution of robust controllers")
ggsave("box_ccea_single_evolution.pdf", width=8, height=5)

agg <- summaryBy(BestSoFar ~ ID1 + ID2 + ID3, lastGen(data), FUN=c(mean,se))
pd <- position_dodge(0)
ggplot(agg, aes(x=ID3,y=BestSoFar.mean,colour=ID2,group=ID2)) +
  geom_errorbar(aes(ymin=BestSoFar.mean-BestSoFar.se, ymax=BestSoFar.mean+BestSoFar.se), width=.5, position=pd) +
  geom_line(position=pd) + geom_point(position=pd) +
  facet_wrap(~ ID1, scales="free_y") + ylim(0,NA) +
  ggtitle("CCEA vs Single: Evolution of robust controllers") + xlab("Task setup (evolution and post-evaluation)") + ylab("Highest fitness") +
  scale_colour_discrete(name="Evolutionary setup") + theme(legend.position="bottom")
ggsave("sum_ccea_single_evolution.pdf", width=8, height=5)


data <- loadData("pred/ccea/bl", filename="behaviours.stat", fun="loadBehaviours", sample=0.1, vars=c("G1","G2","G3",rep(NA,2)), parallel=T)
d <- preSomProcess(data, c("G1","G2","G3"), sampleSize = 25000)
som <- buildSom(d)
plot(som)
d2 <- preSomProcess(data, c("G1","G2","G3"), sampleSize = 25000, cluster=1000)
som2 <- buildSom(d2)
plot(som2)
m2 <- mapBehaviours(som2, data, parallel=T)

