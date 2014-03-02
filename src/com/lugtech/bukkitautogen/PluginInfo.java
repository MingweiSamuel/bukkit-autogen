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
@Retention(RetentionPolicy.SOURCE) //remove at compile
public @interface PluginInfo {
	String name();
	String version();
	String description() default "";
	String load() default "";
	String author() default "";
	String[] authors() default "";
	String website() default "";
	String main();
	String database() default "";
	String depend() default "";
	String prefix() default "";
	String softdepend() default "";
	String loadbefore() default "";
	//permissions & commands included separately
}
