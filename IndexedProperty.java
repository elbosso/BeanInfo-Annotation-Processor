package de.elbosso.util.lang.annotations;

@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
public @interface IndexedProperty
{
	String description() default "--";
	String displayName() default "--";
	boolean hidden() default false;
	boolean expert() default false;
	boolean preferred() default false;
	KeyValueStore[] keyValueStore() default {};
	boolean bound() default false;
	boolean constrained() default false;
	java.lang.Class propertyEditorClass() default Void.class;
}