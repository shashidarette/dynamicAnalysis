package tracecsv;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.CSVReader;

/**
 * Core class responsible for analyzing the traces generated using the aspect.
 * Generate various outputs required as part of the course work
 * @author Shashidar Ette - se146
 *
 */
public class TraceAnalyzer {
	// List of trace files to be analyzed passed by external client
	private List<String> traceFiles;
	
	// Class map list - name and count pair - retrieved from the trace logs 
	private List<Map<String, AtomicInteger>> classMapList = new ArrayList<Map<String, AtomicInteger>>();
	
	// Method map list  - name and count pair - retrieved from the trace logs
	private List<Map<String, AtomicInteger>> methodMapList = new ArrayList<Map<String, AtomicInteger>>();
	
	// Processed class entry list
	private List<LogEntry> classesEntryList = new ArrayList<LogEntry>();
	
	// Processed method entry list
	private List<LogEntry> methodEntryList = new ArrayList<LogEntry>();
	
	public TraceAnalyzer(List<String> files){
		traceFiles = files;
	}
	
	// Main analysis function responsible for core processing and update the classes and methods information
	public void analyze() throws IOException {		
		for (String file : traceFiles) {
			Map<String, AtomicInteger> classMap = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> methodMap = new HashMap<String, AtomicInteger>();
			
			CSVReader traceReader = new CSVReader(new FileReader(file), ',');
			
			// skips first line - header
			String[] firstLine = traceReader.readNext(); 
			//String[] fileColumnHeader = firstLine;
			
			// process columns for class name and method name
			String[] nextLine;
			while ((nextLine = traceReader.readNext()) != null) {
				String className = nextLine[1];
				String methodName = nextLine[2];
				
				updateOccuranceCount(classMap, className);
				updateOccuranceCount(methodMap, getMethodKey(className, methodName));
			}
			classMapList.add(classMap);
			methodMapList.add(methodMap);
			traceReader.close();
		}
		
		classesEntryList.addAll(processMapList(classMapList));
		methodEntryList.addAll(processMapList(methodMapList));
	}
	
	/**
	 * Utility function responsible for processing the map list and generate list of LogEntries
	 * @param mapList
	 * @return
	 */
	private Collection<LogEntry> processMapList(List<Map<String, AtomicInteger>> mapList) {
		// find command list of class names
		Set<String> commonClassList = new HashSet<String>();
		for (Map<String, AtomicInteger> classMap : mapList) {
			if (commonClassList.size() == 0) {
				commonClassList.addAll(classMap.keySet());
			} else {
				commonClassList.retainAll(classMap.keySet());
			}
		}
		
		Map<String, LogEntry> logEntryMap = new HashMap<String, LogEntry>();
		
		for (Map<String, AtomicInteger> map : mapList) {
			for (String className : map.keySet()) {
				updateOccuranceCount(logEntryMap, className, map.get(className).get(),
						commonClassList.contains(className));
			}
		}
		
		return logEntryMap.values();		
	}
	
	/**
	 * Utility function to update an entry in a map
	 * @param map
	 * @param key
	 */
	private void updateOccuranceCount(Map<String, AtomicInteger> map, String key){
		AtomicInteger count = map.get(key);
		if (count != null) {
			count.incrementAndGet();
		} else {
			map.put(key, new AtomicInteger(1));
		}
	}
	
	/**
	 * Utility function to update an entry in a map
	 * @param map
	 * @param key
	 */
	private void updateOccuranceCount(Map<String, LogEntry> map, String key, int count, boolean alwaysUsed){
		LogEntry entry = map.get(key);
		if (entry != null) {
			entry.addOccurance(count);
		} else {
			map.put(key, new LogEntry(key, count, alwaysUsed));
		}
	}
	
	// Utility function to generate classes CSV
	public String generateClassesCSV(){
		return generateCSV(classesEntryList);
	}
	
	// Utility function to generate methods CSV
	public String generateMethodsCSV(){
		return generateCSV(methodEntryList);
	}

	// generic function to generate CSV from the given LogEntry list
	private String generateCSV(List<LogEntry> entryList) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Name, TotalOccurances, AlwaysUsed \r\n");
		for (LogEntry entry : entryList) {
			sb.append(entry);
		}

		return sb.toString();
	}
	
	/**
	 * Utility function to get the topmost classes based on total occurane
	 * @param numberOfClasses
	 * @return
	 */
	public List<LogEntry> getTopmostClasses(int numberOfClasses) {		
		Collections.sort(classesEntryList, new SortByOccurance());
		
		// if the number of topmost classes requested is more than class list size itself
		// set it to maximum
		if (classesEntryList.size() < numberOfClasses) {
			numberOfClasses = classesEntryList.size();
		}
		List<LogEntry> topClasses = new ArrayList<LogEntry>();
		for (int index = 0; index < numberOfClasses; index++) {
			System.out.println(classesEntryList.get(index).getName() + " " + classesEntryList.get(index).getOccurance());
			topClasses.add(classesEntryList.get(index));
		}
		
		return topClasses;		
	}

	/**
	 * Generates a unique key for a class name and a method
	 * @param className
	 * @param methodName
	 * @return
	 */
	private String getMethodKey(String className, String methodName) {
		return className + " : " + methodName;
	}
	
	/**
	 * Primary function responsible for TimeSeries
	 * @return
	 * @throws IOException
	 */
	public List<String> generatePhaseAnalysis() throws IOException {
		List<String> timeSeriesList = new ArrayList<String>();
		
		Map<String, Boolean> methodUsedMap = new HashMap<>();
		for (LogEntry e : methodEntryList) {
			methodUsedMap.put(e.getName(), e.getAlwaysUsed());
        }
		
		for (String file : traceFiles) {
			System.out.println("Processing trace file: " + file);
			StringBuilder timeSeriesOutput = new StringBuilder();
			String newLine = System.lineSeparator();
			CSVReader traceReader = new CSVReader(new FileReader(file), '\t');	
			
			// skips first line - header
			String[] firstLine = traceReader.readNext();
			timeSeriesOutput.append("TraceNumber,");
			timeSeriesOutput.append(StringUtils.join(firstLine, ","));
			timeSeriesOutput.append(",AlwaysUsed");
			timeSeriesOutput.append(newLine);
			//String[] fileColumnHeader = firstLine;
			
			// process columns for class name and method name
			String[] nextLine;
			int index = 1;
			while ((nextLine = traceReader.readNext()) != null) {
				String className = nextLine[1];
				String methodName = nextLine[2];
				String methodKey = getMethodKey(className, methodName);
				
				boolean used = methodUsedMap.get(methodKey).booleanValue();
				
				List<String> traceLine = new ArrayList<>();
				traceLine.add(Integer.toString(index));
				traceLine.addAll(Arrays.asList(nextLine));
				traceLine.add(used ? "true" : "false");
				timeSeriesOutput.append(StringUtils.join(traceLine, ","));
				timeSeriesOutput.append(newLine);
				index++;
			}
			traceReader.close();
			timeSeriesList.add(timeSeriesOutput.toString());
		}
		
		return timeSeriesList;
	}
}
