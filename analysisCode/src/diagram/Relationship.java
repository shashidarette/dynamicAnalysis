package diagram;

import java.io.IOException;
import java.util.List;

import tracecsv.DataFileManager;
import tracecsv.LogEntry;
import tracecsv.TraceAnalyzer;

public class Relationship {
	public static void main(String[] args) throws IOException {
		String traceFileFolder = 
				"C:\\Users\\Shashi\\Documents\\MS\\SEM2\\SRE\\Assignment2\\assignment2-shashidarette\\dataFiles\\outputs\\TraceFiles";
		List<String> traceFiles = DataFileManager.identifyDataFiles(traceFileFolder);
		
		TraceAnalyzer analyser = new TraceAnalyzer(traceFiles);
		analyser.analyze();		

		List<LogEntry> topClasses = analyser.getTopmostClasses(25);
	}
}
