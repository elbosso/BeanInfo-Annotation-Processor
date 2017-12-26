/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.elbosso.util.lang.annotations;

/**
 *
 * @author elbosso
 */
@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE)
public @interface Method
{
	String description() default "--";
	String displayName() default "--";
	boolean hidden() default false;
	boolean expert() default false;
	boolean preferred() default false;
	KeyValueStore[] keyValueStore() default {};
}