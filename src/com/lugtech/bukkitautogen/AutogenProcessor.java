package com.lugtech.bukkitautogen;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"com.lugtech.bukkitautogen.CommandInfo"})
public class AutogenProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		//Set<? extends Element> annotations = roundEnv.getElementsAnnotatedWith(CommandInfo.class);
		
		Messager msg = super.processingEnv.getMessager();
		for (TypeElement element : annotations) {
			if ("".equals(element.getAnnotation(CommandInfo.class).command()))
					msg.printMessage(Diagnostic.Kind.ERROR, "Must implement @CommandInfo annotation", element);
		}
		
		return true;
	}

}
