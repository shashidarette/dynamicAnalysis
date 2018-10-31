package tracecsv;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class - to find the list of CSV files given a folder path
 * @author Shashidar Ette - se146
 *
 */
public class DataFileManager {
	public static List<String> identifyDataFiles(String dataFolderPath) {
		return identifyDataFiles(dataFolderPath, "*.csv");
	}

	public static List<String> identifyDataFiles(String dataFolderPath, String pattern) {
		DirectoryStream<Path> stream = null;
		List<String> dataFiles = new ArrayList<String>();
		PathMatcher fileMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

		try {
			stream = Files.newDirectoryStream(Paths.get(dataFolderPath));

			for (Path fileEntry : stream) {
				if (fileMatcher.matches(fileEntry.getFileName())) {
					dataFiles.add(dataFolderPath + "\\" + String.valueOf(fileEntry.getFileName()));
				}
			}
		} catch (NoSuchFileException e) {
			// TODO handle the exception
		} catch (IOException e) {
			// TODO handle the exception
		}

		return dataFiles;
	}
}
