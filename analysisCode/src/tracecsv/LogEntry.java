package tracecsv;

/**
 * Utility class to maintain the information an entity while processing a  trace log
 * @author Shashidar Ette - se146
 *
 */
public class LogEntry {
	// Name of the entry
	private String name;
	// Occurrence count
	private int occurance;
	// Status whether this entity was used within a traces under consideration
	private boolean alwaysUsed;
	
	public LogEntry(String n, int occur, boolean always) {
		name = n;
		occurance = occur;
		alwaysUsed = always;
	}
	
	public String getName() {
		return name;
	}
	
	public int getOccurance() {
		return occurance;
	}
	
	public int addOccurance(int count) {
		occurance += count;
		return occurance;
	}
	
	public boolean getAlwaysUsed() {
		return alwaysUsed;
	}
	
	@Override
	// Generated basic CSV value of LogEntry
	public String toString() {
		return name + "," + occurance + "," + alwaysUsed + "\r\n";			
	}
}