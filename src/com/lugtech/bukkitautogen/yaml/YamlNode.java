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
	private static final String nodeRegexLn = "(\t*)(\\w+)(:)(\\s*)";
	private static final String valueRegexLn = "(\t*)([\\w|-]+)(:)(\\s)(\\[?)(')(.*)(')(\\]?)";
	private static final String keyRegex = "(\\b)(\\w*)(:)";
	private static final String valueRegex = "(')(.*?)(')";
	private static final String commentRegex = "(\t*)(#)(.*)";
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
		this.localRegexLn = "(\t{" + level + "})(\\w+)(:)(.*)";
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
		if (line == null || line.matches(commentRegex) || line.matches(spaceRegex)) //null / comment / whitespace line
			return true;
		
		
		if (line.equals("	end:"))
			log("! end line here");
		if (line.matches(localRegexLn)) { //if it's a valid line
			YamlNode node;
			if (line.matches(valueRegexLn)) { //if its a value
				log("Level " + level + ": VALUE: " + parseName(line));
				node = new YamlNode(level + 1, parseValues(line));
			}
			else { //if its a node
				log("Level " + level + ": NODE: " + parseName(line));
				node = new YamlNode(level + 1, scanner);
			}
			
			for (String orphan : node.getOrphans()) {
				log("         GROWING ORPHAN: " + parseName(orphan));
				grow(scanner, orphan); //grow last amputated branch if any
			}
			
			children.put(parseName(line), node); //add it to children
			return true;
		}
		else { //invalid line
			log("Level " + level + ": INVALID: " + parseName(line));
			if (line.matches(nodeRegexLn)) { //if it's a different level
				log("         ORPHAN NODE: " + parseName(line));
				orphans.add(line);
			}
			else if (line.matches(valueRegexLn)) {
				log("         ORPHAN VALUE: " + parseName(line));
				orphans.add(line);
			}
			return false;
		}
	}
	
	private void log(String str) {
		if (Yaml.log)
			System.out.println(str);
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
		if (!children.isEmpty()) {
			out.print("\n");
			for (String nodeName : children.keySet()) {
				for (int i = 0; i < level; i++, out.print("\t"));
				out.print(nodeName + ": ");
				children.get(nodeName).write(out);
			}
		}
		else if (values != null) {
			out.print("['");
			for (int i = 0; i < values.length; i++) {
				if (i != 0) //not first
					out.print(", '");
				out.print(values[i] + "'");
			}
			out.print("]\n");
		}
	}
}
