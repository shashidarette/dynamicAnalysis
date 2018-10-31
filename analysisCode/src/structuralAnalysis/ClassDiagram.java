package structuralAnalysis;


import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tracecsv.LogEntry;

/**
 * Originally created by neilwalkinshaw on 24/10/2017 for Reverse-Engineering a Class Diagram using Reflection lab session
 * Extended or modified by shashidarette for System Re-engineering assignment 2 - Dynamic analysis to generate class diagram
 */
public class ClassDiagram {
	// Main folder contains .class files (including sub-folders)
	String rootDir;
	
	// MainJar path to resolve and find required classes
	private String mainJarPath;
	
	// To store subclass and superclass relationship
	Map<String, String> classMap = new HashMap<String, String>();
	
	// This list is used as the set of classes which are considered to generate the class diagram
	List<LogEntry> desiredClasses;
	
	// Class loader is used along with main jar to resolve and find required classes
	URLClassLoader classLoader;
	
	// Setting to compute the inheritance between classes
	boolean computeInheritance;
	
	// Setting to compute the association between classes
	boolean computeAssociation;
	
	// Flag to generate orthogonal lines to show relationship or normal curved lines
	boolean createOrthoGonalLines = false;
	
	// Constructor
	public ClassDiagram(String root, List<LogEntry> classNameList,
    		String mainJarLocation,
    		boolean inhenritance, boolean assoication) throws MalformedURLException {
    	rootDir = root;        
        desiredClasses = new ArrayList<LogEntry>(classNameList);
        mainJarPath = mainJarLocation;
        computeInheritance = inhenritance;
        computeAssociation = assoication;
        
        // load the main class jar which has class definitions
        File file = new File(mainJarPath);
        classLoader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()});
    }
	
    /**
     * Given a package name and a directory returns all classes within that directory
     * @param directory
     * @param pkgname
     * @return Classes within Directory with package name
     */
    public List<Class<?>> processDirectory(File directory, String pkgname, List<String> desiredClasses) {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        String prefix = pkgname + ".";
        if(pkgname.equals("")) {
            prefix = "";
        }
        
        // Get the list of the files contained in the package
        String[] files = directory.list();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i];
            String className = null;

            // we are only interested in .class files
            if (fileName.endsWith(".class")) {
                // removes the .class extension
                className = prefix + fileName.substring(0, fileName.length() - 6);
            } 
           
            if (className != null && desiredClasses.contains(className)) {
            	//System.out.println("FoundClass: " + className);
            	Class<?> classObject = loadClass(className);
            	if (classObject != null) {
            		classes.add(classObject);   
            	}
            }
            
            // If the file is a directory recursively class this method.
            File subdir = new File(directory, fileName);
            if (subdir.isDirectory()) {
                classes.addAll(processDirectory(subdir, prefix + fileName, desiredClasses));
            }
        }
        return classes;
    }

    // Load the class and return Class object
    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, true, classLoader);
        } catch(Exception e) {
        	System.out.println("Unexpected ClassNotFoundException loading class '" + className + "'");
        	return null;
        }
    }

    /**
     * Gets the LogEntry given a class name
     * @param className
     * @return null if not found
     */
    private LogEntry getEntry(String className) {
    	LogEntry entry = null;
    	
    	for (LogEntry e : desiredClasses) {
        	if (e.getName().equals(className)) {
        		entry = e;
        		break;
        	}
        }
    	
    	return entry;
    }
    
    /**
     * Main processing function to generated .dot file format
     * @return
     */
    public String analyze() {
    	StringBuilder classDot = new StringBuilder();
    	String newLine = System.lineSeparator();
    	
    	File dir = new File(rootDir);
        List<String> classNames = new ArrayList<String>();
        
        int maxOccurance = desiredClasses.get(0).getOccurance();
        int maxheight = 5;
        
        classDot.append("digraph ClassDiagram{"); classDot.append(newLine);
        
        if (true) {
        	classDot.append("graph [splines=ortho]"); classDot.append(newLine);
        	classDot.append(newLine);
        }
        
        // add all the classes
        for (LogEntry e : desiredClasses) {
        	classNames.add(e.getName());        	
        	double h = (double) (maxheight * e.getOccurance()) / (double) maxOccurance;
        	classDot.append("\"" + e.getName() + "\"" + 
    					(e.getAlwaysUsed() ? "[shape=box,style=filled,color=\".7 .3 1.0\"" + ",height=" + h + "]" 
    							: "[shape=box" + ",height=" + h + "]" ));
        	classDot.append(newLine);
        }
        
        List<Class<?>> classes = processDirectory(dir, "", classNames);
        
        // compute inheritance and add classes if not added already
        if (computeInheritance) {
	        for (Class classObject : classes) {
	        	String superClass = classObject.getSuperclass().getName();
	        	classMap.put(classObject.getName(), superClass);
	        }
	        
	        for (String className : classMap.keySet()) {
	        	String superClassName = classMap.get(className);        	
	        	if (!classes.contains(className)) {
	        		classDot.append("\"" + className + "\"" + "[shape=box]");
	        		classDot.append(newLine);
	        	}
	        	if (!classes.contains(superClassName)) {
	        		classDot.append("\"" + superClassName + "\"" + "[shape=box]");
	        		classDot.append(newLine);
	        	}
	        	classDot.append("\"" + className + "\"" +"->" + "\"" + superClassName + "\"" + "[arrowhead=\"onormal\"]");
	        	classDot.append(newLine);
	        }
        }
        
        // compute association, generate class field list and add it to .dot format
        if (computeAssociation) {
        	List<String> classDataFieldsList = new ArrayList<String>();
        	
	        for (Class classObject : classes) {
	        	// if its an interface - it will not have any associations
	        	if (classObject.isInterface()) {
	        		continue;
	        	}
	        	Field[] fields = classObject.getDeclaredFields();
	        	if (fields != null && fields.length > 0) {
	        		List<String> fieldTypes = new ArrayList<String>();
	        		for (Field field : fields) {
	        			String fieldTypeName = field.getType().getName();
	        			String fieldName = field.getName();
	        			fieldTypes.add(fieldTypeName);
	        			
	        			// Only consider classes which are not standard types
	        			if (!field.getType().isPrimitive()) {
	        				if (!classes.contains(fieldTypeName)) {
	        					classDot.append("\"" + fieldTypeName + "\"" + "[shape=box]");
	        	        	}
	        				classDataFieldsList
	        				.add("\"" + classObject.getName() + "\"" + "->" + "\"" + fieldTypeName + "\"" + "[arrowhead=\"diamond\"]");
	        			}
	        		}
	        	}        	
	        }
	        
	        for (String classFieldA : classDataFieldsList) {
	        	classDot.append(classFieldA);
	        	classDot.append(newLine);
	        } 
        }
        classDot.append("}");
        return classDot.toString();
    }
}
