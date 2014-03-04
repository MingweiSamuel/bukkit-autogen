package com.lugtech.bukkitautogen.yaml;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlNode {
	
	private final Map<String, YamlNode> children = new HashMap<String, YamlNode>(0);
	private final String[] values;
	
	private final int level;
	
	private final String localRegexLn;
	private static final String nodeRegexLn = "(( )*)([\\w\\-\\.]+)(:)(\\s*)";
	private static final String valueRegexLn = "(( )*)([\\w\\-\\.]+)(:)(\\s)(\\[?)(')(.*)(')(\\]?)";
	private static final String commentRegexLn = "(( )*)(#)(.*)";
	
	private static final String keyRegex = "(\\b)([\\w\\-\\.]+)(:)";
	private static final String valueRegex = "(')(.*?)(')";
	private static final String spaceRegex = "(\\s*)";
	
	private List<String> orphans = new ArrayList<String>(0);
	
	// Values //
	public YamlNode(int level, String... values) {
		this.localRegexLn = null; //unused
		this.level = level;
		this.values = values;
	}
	
	// Nodes //
	public YamlNode(int level, Scanner scanner) {
		this(level);
		while (scanner.hasNextLine() && grow(scanner, scanner.nextLine()));
	}
	public YamlNode(int level) {
		this.localRegexLn = "(( ){" + level + "})([\\w\\-\\.]+)(:)(.*)";
		this.values = null;
		this.level = level;
	}
	
	public boolean addValue(String key, String... values) {
		if (this.values != null)
			return false; //value not node
		children.put(key, new YamlNode(level + 1, values));
		return true;
	}
	public boolean addNode(String key) {
		if (this.values != null)
			return false; //value not node
		children.put(key, new YamlNode(level + 1));
		return true;
	}
	
	private boolean grow(Scanner scanner, String line) {
		if (line == null || line.matches(commentRegexLn) || line.matches(spaceRegex)) //null / comment / whitespace line
			return true;
		
		if (line.matches(localRegexLn)) { //if it's a valid line
			YamlNode node;
			if (line.matches(valueRegexLn)) { //if its a value
				Yaml.log(level, "VALUE: " + parseName(line));
				node = new YamlNode(level + 1, parseValues(line));
			}
			else { //if its a node (goes on to the next line in the new YamlNode)
				Yaml.log(level, "NODE: " + parseName(line));
				node = new YamlNode(level + 1, scanner);
			}
			
			for (String orphan : node.getOrphans()) {
				Yaml.log(level, "GROWING ORPHAN: " + parseName(orphan));
				if (!grow(scanner, orphan)) //grow last amputated branch if any / return false if failed to grow: means its at least one more level down
					return false;
			}
			
			children.put(parseName(line), node); //add it to children
			return true;
		}
		else { //invalid line
			if (line.matches(nodeRegexLn)) { //if it's a different level
				Yaml.log(level, "ORPHAN NODE: " + parseName(line));
				orphans.add(line);
			}
			else if (line.matches(valueRegexLn)) {
				Yaml.log(level, "ORPHAN VALUE: " + parseName(line));
				orphans.add(line);
			}
			else
				Yaml.log(level, "INVALID LINE: " + parseName(line));
			return false;
		}
	}
	
	private String[] getOrphans() {
		return orphans.toArray(new String[orphans.size()]);
	}
	
	public Map<String, YamlNode> getChildren() {
		return children;
	}
	
	public String[] getValues() {
		return values;
	}
	
	public int getLevel() {
		return level;
	}
	
	int hasInfo() {
		if (values == null) {
			return children.size();
		}
		int infos = 0;
		for (String value : values) {
			if (value != null && !value.matches(spaceRegex))
				infos++;
		}
		return infos;
	}
	
	public static String parseName(String line) { //gets string between tabs and colon - 1
		Matcher name = Pattern.compile(keyRegex).matcher(line);
		name.find();
		return line.substring(name.start(), name.end() - 1);
	}
	
	public static String[] parseValues(String line) { //gets string between single quotes
		List<String> values = new ArrayList<String>(0);
		Matcher name = Pattern.compile(valueRegex).matcher(line);
		while(name.find()) {
			values.add(line.substring(name.start() + 1, name.end() - 1));
		}
		return values.toArray(new String[values.size()]);
	}
	
	void write(PrintStream out) {
		int infos = hasInfo();
//		if (infos == 0) //don't do anything if this is empty / this should not happen b/c it should be caught in the previous node
//			return;
		if (values == null) { //node
			out.print("\n");
			for (String nodeName : children.keySet()) {
				if (children.get(nodeName).hasInfo() == 0) //skip this node if it's empty
					continue;
				for (int i = 0; i < level; i++, out.print(" ")); //space indent
				Yaml.log(level, "WRITING ELEMENT: " + nodeName);
				out.print(nodeName + ": ");
				children.get(nodeName).write(out);
			}
			return;
		} //single value
		if (infos == 1) { //has one valid info
			for (String value : values) {
				if (value == null || value.matches(spaceRegex)) //we need to find the valid value
					continue;
				Yaml.log(level, "WRITING VALUE: " + value);
				out.print("'" + value + "'\n");
				break;
			}
			return;
		} //multi-value
		Yaml.log(level, "WRITING MULTI-VALUE");
		out.print("['");
		int count = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null || values[i].matches(spaceRegex))
				continue; //skip if empty
			count++;
			Yaml.log(level, "WRITING SUB-VALUE: " + values[i]);
			out.print(values[i] + "'");
			if (count != infos) //not last
				out.print(", '");
		}
		out.print("]\n");
	}
}
