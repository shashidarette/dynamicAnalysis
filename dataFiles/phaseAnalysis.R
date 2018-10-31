library(ggplot2)
# Provide the timeseries file generted by DynamicCodeAnalyser console application
# TimeSeries files should be selected one by one.
timeseries <- read.csv("C:\\Users\\Shashi\\Documents\\MS\\SEM2\\SRE\\Assignment2\\assignment2-shashidarette\\analysisCode\\Outputs\\timeseries5.csv", strip.white=TRUE)

#Plot the timeseries
ggplot(timeseries, aes(x=TraceNumber,y=StackDepth)) + 
  geom_point(aes(color=AlwaysUsed)) +
               scale_x_continuous(name="TraceNumber", limits=c(0, nrow(timeseries)+50))

