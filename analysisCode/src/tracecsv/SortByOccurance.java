package tracecsv;

import java.util.Comparator;

/**
 * Utility Comparator class used for comparing of 2 LogEntry object based on occurence
 * @author Shashidar Ette - se146
 *
 */
public class SortByOccurance implements Comparator<LogEntry>
{
    public int compare(LogEntry a, LogEntry b)
    {
    	return b.getOccurance() -  a.getOccurance();
    }
}
