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
@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE)
public @interface BeanInfo
{
	String description() default "--";
	String displayName() default "--";
	boolean hidden() default false;
	boolean expert() default false;
	boolean preferred() default false;
//	String moduleWidgetDefaultLayerTitle() default "--";
	String i18nBundle() default "--";
	int defaultPropertyIndex() default -1;
	int defaultEventIndex() default -1;
	String imageMono16() default "--";
	String imageMono32() default "--";
	String imageColor16() default "--";
	String imageColor32() default "--";
	KeyValueStore[] keyValueStore() default {};
	String exceptionHandling() default "e.printStackTrace();";
}