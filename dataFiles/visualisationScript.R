
#Read in the various CSV files

lineCounts <- read.csv("lineCounts.csv")
vocabCounts <- read.csv("vocabCounts.csv")
commentCounts <- read.csv("commentCounts.csv")

#Merge the columns into a single variable using cbind
#(only using the second column in two of the files to
#avoid repeating the file names)

counts <- cbind(lineCounts,vocabCounts[,2],commentCounts[,2])

#Add column names

colnames(counts) <- c("File","Lines", "Vocabulary", "Comments")

#Load up the ggplot2 library

library(ggplot2)

#Scatter plot lines versus comments, colouring
#according to vocabulary size

ggplot(counts,aes(x=Lines,y=Comments)) + 
  geom_point(aes(color=Vocabulary)) 

#Add labels

ggplot(counts,aes(x=Lines,y=Comments)) + 
  geom_point(aes(color=Vocabulary))  + 
  geom_text(aes(label=File))

#Selective labelling

ggplot(counts,aes(x=Lines,y=Comments)) + 
  geom_point(aes(color=Vocabulary))  + 
  geom_text(data=counts[counts$Comments>150,],aes(label=File))

