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
public @interface CommandInfo {
	String command();
	String description() default "";
	String[] aliases() default "";
	String permission() default "";
	String permission_message() default "";
	String usage() default "";
}
