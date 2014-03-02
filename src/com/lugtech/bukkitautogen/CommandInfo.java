package com.lugtech.bukkitautogen;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target(ElementType.TYPE) //target classes
@Retention(RetentionPolicy.RUNTIME) //keep
public @interface CommandInfo {
	String command();
	String description();
	String[] aliases();
	String permission();
	String usage();
}
