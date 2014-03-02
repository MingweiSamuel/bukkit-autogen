package com.lugtech.bukkitautogen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("com.lugtech.bukkitautogen.*")
public class AutogenProcessor extends AbstractProcessor {
	
	private static final String hasSpace = "(?s)(.*)(\\s+)(.*)";
	private static final String isSpace = "(\\s*)";
	
	private StringBuilder file = new StringBuilder();
	
	/**
	 * Write to the plugin.yml
	 * @param depth Depth (number of tabs) to use with the key
	 * @param key Key name
	 * @param value Values of the key
	 * @return True if value was not empty
	 */
	private boolean write(boolean force, int depth, String key, String... value) {
		if (value.length < 1 || value[0] == null || value[0].matches(isSpace)) {
			if (force) {
				for (int i = 0; i < depth; i++, file.append("\t"));
				file.append(key + ":\n");
			}
			return false;
		}
		for (int i = 0; i < depth; i++, file.append("\t"));
		file.append(key + ": ");
		if (value.length == 1) {
			file.append("'" + value[0] + "'\n");
			return true;
		}
		for (int i = 0; i < value.length; i++) {
			if (value[i] == null || value[i].matches(isSpace))
				continue;
			file.append("\n");
			for (int j = 0; j < depth + 1; j++, file.append("\t"));
			file.append("- '" + value[i] + "'");
		}
		file.append("\n");
		return true;
	}
	
	private boolean done = false;
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		
		if (done) return true;
		done = true; //only do this once
		
		try {
			
			file.append("# This file was automatically generated. Any changes will likely be rewritten.\n");
			
			Messager msg = super.processingEnv.getMessager();
			Types typeUtil = super.processingEnv.getTypeUtils();
			Elements elementUtil = super.processingEnv.getElementUtils();
			
			
			Set<? extends Element> plugins = roundEnv.getElementsAnnotatedWith(PluginInfo.class);
			
			for (Element element : plugins) {
				if (element instanceof TypeElement) {
					TypeElement typeElement = (TypeElement) element;
					Iterator<? extends TypeMirror> interfaces = typeElement.getInterfaces().iterator();
					
					boolean valid;
					while (true) {
						if (!interfaces.hasNext()) {
							msg.printMessage(Diagnostic.Kind.ERROR, "Classes using the @PluginInfo annotation must implement the org.bukkit.plugin.Plugin interface", element);
							valid = false;
							break;
						}
						if (typeUtil.isSameType(interfaces.next(), elementUtil.getTypeElement("org.bukkit.plugin.Plugin").asType())) {
							valid = true;
							break;
						}
					}
					if (!valid)
						continue;
					
					//usage is valid
					PluginInfo info = element.getAnnotation(PluginInfo.class);
					if (!write(false, 0, "name", info.name())) {
						msg.printMessage(Diagnostic.Kind.ERROR, "name must be set in @PluginInfo", element);
						return true;
					}
					if (!write(false, 0, "version", info.version())) {
						msg.printMessage(Diagnostic.Kind.ERROR, "version must be set in @PluginInfo", element);
						return true;
					}
					if (!write(false, 0, "main", info.main())) {
						msg.printMessage(Diagnostic.Kind.ERROR, "main must be set in @PluginInfo", element);
						return true;
					}
					write(false, 0, "description", info.description());
					write(false, 0, "load", info.load());
					write(false, 0, "author", info.author());
					write(false, 0, "authors", info.authors());
					write(false, 0, "website", info.website());
					write(false, 0, "database", info.database());
					write(false, 0, "depend", info.depend());
					write(false, 0, "prefix", info.prefix());
					write(false, 0, "softdepend", info.softdepend());
					write(false, 0, "loadbefore", info.loadbefore());
				}
			}
			//}
			
			write(true, 0, "commands"); //force this to write
			Set<? extends Element> commands = roundEnv.getElementsAnnotatedWith(CommandInfo.class);
			for (Element element : commands) {
				if (element instanceof TypeElement) {
					TypeElement typeElement = (TypeElement) element;
					Iterator<? extends TypeMirror> interfaces = typeElement.getInterfaces().iterator();
					
					boolean valid;
					while (true) {
						if (!interfaces.hasNext()) {
							msg.printMessage(Diagnostic.Kind.ERROR, "Any class using the @CommandInfo annotation must implement the org.bukkit.command.CommandExecutor interface", element);
							valid = false;
							break;
						}
						if (typeUtil.isSameType(interfaces.next(), elementUtil.getTypeElement("org.bukkit.command.CommandExecutor").asType())) {
							valid = true;
							break;
						}
					}
					if (!valid)
						continue;
					
					//the usage is valid
					CommandInfo info = element.getAnnotation(CommandInfo.class);
					if (info.command().matches(hasSpace)) { //contains whitespace
						msg.printMessage(Diagnostic.Kind.ERROR, "Command may not contain whitespace in @CommandInfo", element);
						continue; //go on to next command / ignore this one
					}
					if (info.command().matches(isSpace)) { //sorta redundant
						msg.printMessage(Diagnostic.Kind.ERROR, "Command must be set in @CommandInfo", element);
						continue; //go on to next command / ignore this one
					}
					write(true, 1, info.command());
					
					List<String> aliases = new ArrayList<String>(0);
					for (int i = 0; i < info.aliases().length; i++) {
						if (info.aliases()[i].matches(hasSpace)) { //contains whitespace
							msg.printMessage(Diagnostic.Kind.ERROR, "Alias string may not contain whitespace in @CommandInfo", element);
							continue; //go on to next alias
						}
						aliases.add(info.aliases()[i]);
					}
					write(false, 2, "aliases", aliases.toArray(new String[aliases.size()]));
					write(false, 2, "description", info.description());
					write(false, 2, "permissions", info.permission());
					write(false, 2, "permission-message", info.permission_message());
					write(false, 2, "usage", info.usage());
				}
			}
			
			write(true, 0, "permissions"); //forced to write
			Set<? extends Element> missions = roundEnv.getElementsAnnotatedWith(PermissionInfo.class);
			for (Element element : missions) {
				if (element instanceof TypeElement) {
					PermissionInfo info = element.getAnnotation(PermissionInfo.class);
					
					if (info.permission().matches(isSpace)) { //sorta redundant
						msg.printMessage(Diagnostic.Kind.ERROR, "Command must be set in @CommandInfo", element);
						continue; //go on to next command / ignore this one
					}
					write(true, 1, info.permission());
					write(false, 2, "default", info.default_value());
					write(false, 2, "description", info.description());
					write(false, 2, "childern", info.children());
				}
			}
			
			try {
				Filer filer = super.processingEnv.getFiler();
				FileObject fileObj = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "../plugin2.yml");
				PrintWriter writer = new PrintWriter(fileObj.openOutputStream()); //open the generated file
				writer.append(file); //write the strings
				writer.close();
			} catch (IOException ioe) {
				msg.printMessage(Diagnostic.Kind.WARNING, "Autogen failed to access to plugin.yml: " + ioe.getLocalizedMessage());
			}
		} catch (Exception e) { //pokemon catch
			try {
				Filer filer = super.processingEnv.getFiler();
				FileObject fileObj = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "../autogen-errors.log");
				PrintWriter writer = new PrintWriter(fileObj.openOutputStream()); //open the generated file
				writer.println(e); //write the strings
				writer.println(e.getLocalizedMessage());
				for (StackTraceElement trace : e.getStackTrace()) {
					writer.println(trace);
				}
				writer.close();
			} catch (IOException ioe) { } //we're sunk
		}
		
		return true;
	}
}
