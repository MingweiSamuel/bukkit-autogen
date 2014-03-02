package com.lugtech.bukkitautogen;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.lugtech.bukkitautogen.CommandInfo")
public class AutogenProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {
		Messager msg = super.processingEnv.getMessager();
		
		for (TypeElement element : elements) {
			if (element.getInterfaces().contains(AutogenCommand.class)) {
				msg.printMessage(Diagnostic.Kind.WARNING, "Warning here");
				msg.printMessage(Diagnostic.Kind.WARNING, "Warning here", element);
				if (element.getAnnotation(CommandInfo.class) == null)
					msg.printMessage(Diagnostic.Kind.ERROR, "Class must have @CommandInfo annotation", element);
			}
		}
		
		return true;
	}

}
