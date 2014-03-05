package com.lugtech.bukkitautogen.yaml;

import static com.lugtech.bukkitautogen.yaml.Yaml.log;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlNode extends HashMap<String, YamlNode> {
	
	private static final long serialVersionUID = -9189590559404045260L;

	private final String[] strings; //refered to as values b/c conflict with Map
	private final int level;
	
	private final String localRegexLn;
	private static final String nodeRegexLn = "(( )*)([\\w\\-\\.]+)(:)(\\s*)";
	private static final String valueRegexLn = "(( )*)([\\w\\-\\.]+)(:)(\\s)(\\[?)(')(.*)(')(\\]?)";
	private static final String commentRegexLn = "(( )*)(#)(.*)";
	
	private static final String keyRegex = "(\\b)([\\w\\-\\.]+)(:)";
	private static final String valueRegex = "(')(.*?)(')";
	private static final String spaceRegex = "(\\s*)";
	
	private List<String> orphans = new ArrayList<String>(0);
	
	// Constructors //
	// Values //
	public YamlNode(int level, String... values) {
		this.localRegexLn = null; //unused
		this.level = level;
		this.strings = values;
	}
	
	// Nodes //
	public YamlNode(int level, Scanner scanner) {
		this(level);
		while (scanner.hasNextLine() && grow(scanner, scanner.nextLine()));
	}
	public YamlNode(int level) {
		this.localRegexLn = "(( ){" + level + "})([\\w\\-\\.]+)(:)(.*)";
		this.strings = null;
		this.level = level;
	}
	
	// Convenience Put //
	public boolean putValue(String key, String... values) {
		log(level, "ADDING VALUE: " + key);
		if (this.strings != null)
			return false; //value not node
		this.put(key, new YamlNode(level + 1, values));
		return true;
	}
	public boolean putNode(String key) {
		log(level, "ADDING NODE: " + key);
		if (this.strings != null) { //if it has values, then it's not a node
			log(0, "FAILED TO ADD NODE TO KEY: " + key);
			return false;
		}
		this.put(key, new YamlNode(level + 1));
		return true;
	}
	
	// Override //
	@Override
	public boolean containsKey(Object key) {
		boolean value = super.containsKey(key);
		if (value)
			log(level, "CONTAINS KEY: " + key);
		else
			log(level, "MISSING KEY: " + key);
		return value;
	}
	@Override
	public YamlNode get(Object key) {
		log (level, "GETTING: " + key);
		return super.get(key);
	}
	
	// Util //
	private boolean grow(Scanner scanner, String line) {
		if (line == null || line.matches(commentRegexLn) || line.matches(spaceRegex)) //null / comment / whitespace line
			return true;
		
		if (line.matches(localRegexLn)) { //if it's a valid line
			YamlNode node;
			if (line.matches(valueRegexLn)) { //if its a value
				log(level, "VALUE: " + parseName(line));
				node = new YamlNode(level + 1, parseValues(line));
			}
			else { //if its a node (goes on to the next line in the new YamlNode)
				log(level, "NODE: " + parseName(line));
				node = new YamlNode(level + 1, scanner);
			}
			this.put(parseName(line), node); //add it to children
			
			for (String orphan : node.getOrphans()) {
				log(level, "GROWING ORPHAN: " + parseName(orphan));
				if (!grow(scanner, orphan)) //grow last amputated branch if any / return false if failed to grow: means its at least one more level down
					return false;
			}
			return true;
		}
		else { //invalid line
			if (line.matches(nodeRegexLn)) { //if it's a different level
				log(level, "ORPHAN NODE: " + parseName(line));
				orphans.add(line);
			}
			else if (line.matches(valueRegexLn)) {
				log(level, "ORPHAN VALUE: " + parseName(line));
				orphans.add(line);
			}
			else
				log(level, "INVALID LINE: " + parseName(line));
			return false;
		}
	}
	
	private String[] getOrphans() {
		return orphans.toArray(new String[orphans.size()]);
	}
	
	public String[] getStrings() {
		return strings;
	}
	
	public int getLevel() {
		return level;
	}
	
	private int hasInfo() {
		if (strings == null) {
			return 1;
			//return this.size();
		}
		int infos = 0;
		for (String value : strings) {
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
		//node
		if (strings == null) {
			out.print("\n");
			for (String nodeName : this.keySet()) {
				YamlNode node = this.get(nodeName);
				if (node.hasInfo() == 0) { //skip this node if it's empty
					log(level, "SKIPPING: " + nodeName + " " + node.hasInfo());
					continue;
				}
				for (int i = 0; i < level; i++, out.print(" ")); //space indent
				log(level, "WRITING ELEMENT: " + nodeName);
				out.print(nodeName + ": ");
				node.write(out);
			}
			return;
		}
		
		//value (not node)
		int infos = hasInfo();
		if (infos == 1) { //single value (one info)
			for (String value : strings) {
				if (value == null || value.matches(spaceRegex)) //we need to find the one valid value
					continue;
				log(level, "WRITING VALUE: " + value);
				out.print("'" + value + "'\n");
				break;
			}
			return;
		} //multi-value
		log(level, "WRITING MULTI-VALUE:");
		out.print("['");
		int count = 0;
		for (int i = 0; i < strings.length; i++) {
			if (strings[i] == null || strings[i].matches(spaceRegex))
				continue; //skip if empty
			count++;
			log(level, "WRITING SUB-VALUE: " + strings[i]);
			out.print(strings[i] + "'");
			if (count != infos) //not last
				out.print(", '");
		}
		out.print("]\n");
	}
}
