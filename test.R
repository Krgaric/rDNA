library("rDNA")
library("rJava")
library("ggraph")
library("igraph")
library("ggrepel")

# test
dna_init("dna-2.0-beta25.jar")
conn <- dna_connection("Congress 2011-2017.dna")
ircExclude = c("intercoder reliability check", "intercoder reliability test")
nw <- dna_network(conn,
                  networkType = "onemode",
                  qualifier = "agreement",
                  qualifierAggregation = "subtract",
                  normalization = "average",
                  timewindow = "events",
                  windowsize = 80,
                  decayExponential = TRUE,
                  decayParameter = 0,
                  duplicates = "document",
                  excludeSections = ircExclude)
dna_plotNetwork(nw$networks[[30]])

mc <- dna_multiclust(connection = conn,
                     k = 0,
                     k.max = 4,
                     timewindow = "events",
                     windowsize = 80,
                     decayExponential = TRUE,
                     decayParameter = 1.0,
                     duplicates = "document",
                     excludeSections = ircExclude)

dna_plotModularity(mc, anomalize = FALSE)
