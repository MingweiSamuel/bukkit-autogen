package com.lugtech.bukkitautogen;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.lugtech.bukkitautogen.CommandInfo")
public class AutogenProcessor extends AbstractProcessor {
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Messager msg = super.processingEnv.getMessager();
		Types typeUtil = super.processingEnv.getTypeUtils();
		Elements elementUtil = super.processingEnv.getElementUtils();
		
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CommandInfo.class);
		
		for (Element element : elements) {
			if (element instanceof TypeElement) {
				TypeElement typeElement = (TypeElement) element;
				Iterator<? extends TypeMirror> interfaces = typeElement.getInterfaces().iterator();
				
				while (true) {
					if (!interfaces.hasNext()) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Classes using the @CommandInfo annotation must implement the org.bukkit.command.CommandExecutor interface", element);
						return true;
					}
					if (typeUtil.isSameType(interfaces.next(), elementUtil.getTypeElement("org.bukkit.command.CommandExecutor").asType()))
						break;
				}
				//the usage is valid
				
			}
		}
		
		return true;
	}
}
