package de.elbosso.util.processors;

import de.elbosso.util.lang.annotations.*;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.helpers.NOPLogger;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by elbosso on 2/18/17.
 * <p>
 * http://stackoverflow.com/questions/34534947/how-to-check-if-the-package-exists-from-inside-the-annotation-processor-in-comp
 * https://deors.wordpress.com/2011/10/31/annotation-generators/
 */
@SupportedAnnotationTypes({"de.elbosso.util.lang.annotations.BeanInfo"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BeanInfoProcessor
		extends AbstractProcessor
{
	private boolean quiet;
	private Map<String, VariableElement> fields ;
	private Map<String, Map<String, String>> indexedProperties ;
	private Map<String, Map<String, String>> properties ;
	private Map<String, java.util.Map<java.lang.String, java.lang.Object> > methods ;
	private Map<String, Map<String, String>> events ;

	public BeanInfoProcessor()
	{
		super();
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv); //To change body of generated methods, choose Tools | Templates.
		quiet = processingEnv.getOptions().containsKey("quiet");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
						   RoundEnvironment roundEnv)
	{
		String fqClassName = null;
		String className = null;
		String packageName = null;
		fields = new HashMap<String, VariableElement>();
		indexedProperties = new HashMap();
		properties = new HashMap();
		methods = new HashMap();
		events = new HashMap();
		java.util.Map<java.lang.String, java.lang.Object> contextContent=new java.util.HashMap();

		for (Element e : roundEnv.getElementsAnnotatedWith(de.elbosso.util.lang.annotations.BeanInfo.class))
		{
			fields.clear();
			indexedProperties.clear();
			properties.clear();
			methods.clear();
			events.clear();
			if (e.getKind() == ElementKind.CLASS)
			{
				TypeElement classElement = (TypeElement) e;
				java.util.List<java.lang.String> i18nBundles=workOnSuperClass(classElement);
				PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

//				if (!quiet)
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.NOTE,
						"annotated class: " + classElement.getQualifiedName(), e);

				fqClassName = classElement.getQualifiedName().toString();
				className = classElement.getSimpleName().toString();
				packageName = packageElement.getQualifiedName().toString();
				de.elbosso.util.lang.annotations.BeanInfo beanInfo = e.getAnnotation(de.elbosso.util.lang.annotations.BeanInfo.class);
				if(beanInfo != null)
				{
					if(beanInfo.description().equals("--")==false)
						contextContent.put("beanDescriptorDescription", beanInfo.description());
					if(beanInfo.displayName().equals("--")==false)
						contextContent.put("beanDescriptorDisplayName", beanInfo.displayName());
//					if(beanInfo.moduleWidgetDefaultLayerTitle().equals("--")==false)
//						contextContent.put("beanDescriptorModuleWidgetDefaultLayerTitle", beanInfo.moduleWidgetDefaultLayerTitle());
					if(beanInfo.i18nBundle().equals("--")==false)
						i18nBundles.add(beanInfo.i18nBundle());
					if(beanInfo.imageColor16().equals("--")==false)
						contextContent.put("beanDescriptorImageColor16", beanInfo.imageColor16());
					if(beanInfo.imageColor32().equals("--")==false)
						contextContent.put("beanDescriptorImageColor32", beanInfo.imageColor32());
					if(beanInfo.imageMono16().equals("--")==false)
						contextContent.put("beanDescriptorImageMono16", beanInfo.imageMono16());
					if(beanInfo.imageMono32().equals("--")==false)
						contextContent.put("beanDescriptorImageMono32", beanInfo.imageMono32());
					contextContent.put("beanDescriptorHidden",beanInfo.hidden());
					contextContent.put("beanDescriptorExpert",beanInfo.expert());
					contextContent.put("beanDescriptorPreferred",beanInfo.preferred());
					contextContent.put("beanDescriptorDefaultEventIndex",beanInfo.defaultEventIndex());
					contextContent.put("beanDescriptorDefaultPropertyIndex",beanInfo.defaultPropertyIndex());
					contextContent.put("beanDescriptorAlternativeI18nBundles",i18nBundles);
//					contextContent.put("beanDescriptor",beanInfo);
					de.elbosso.util.lang.annotations.KeyValueStore[] keyValueStores=beanInfo.keyValueStore();
					if(keyValueStores!=null)
					{
						java.util.Map<java.lang.String, java.lang.String> valueMap=new java.util.HashMap();
						for (de.elbosso.util.lang.annotations.KeyValueStore keyValueStore : keyValueStores)
						{
							valueMap.put(keyValueStore.key(),keyValueStore.value());
						}
						contextContent.put("beanDescriptorValueMap",valueMap);
					}
					contextContent.put("beanDescriptorExceptionHandling",beanInfo.exceptionHandling());
				}
			}
			java.util.List<? extends Element> l=e.getEnclosedElements();
			for(Element element:l)
			{
				if((element!=null)&&ExecutableElement.class.isAssignableFrom(element.getClass()))

				{
					ExecutableElement exeElement = (ExecutableElement) element;
					Annotation ann = element.getAnnotation(de.elbosso.util.lang.annotations.Method.class);
					if (ann != null)
					{
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.NOTE,
								"Method: " + ann.toString());
						handleMethod(fqClassName, exeElement);
					}
					ann = element.getAnnotation(de.elbosso.util.lang.annotations.Property.class);
					if (ann != null)
					{
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.NOTE,
								"Property: " + ann.toString());
						handleProperty(fqClassName, exeElement);
					}
					ann = element.getAnnotation(de.elbosso.util.lang.annotations.IndexedProperty.class);
					if (ann != null)
					{
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.NOTE,
								"IndexedProperty: " + ann.toString());
						handleIndexedProperty(fqClassName, exeElement);
					}
					ann = element.getAnnotation(de.elbosso.util.lang.annotations.Event.class);
					if (ann != null)
					{
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.NOTE,
								"Event: " + ann.toString());
						handleEvent(fqClassName, exeElement);
					}
				}
			}
			if (fqClassName != null)
			{
				try
				{
					TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(fqClassName + "BeanInfo");
/*				if (typeElement != null)
				{
					FileObject in_file = processingEnv.getFiler().getResource(
							StandardLocation.SOURCE_PATH, "",
							typeElement.asType().toString().replace(".", "/") + ".java");
					processingEnv.getMessager().printMessage(
								Diagnostic.Kind.NOTE,
								in_file.getName());
					java.io.File f=new java.io.File(in_file.getName());
					processingEnv.getMessager().printMessage(
							Diagnostic.Kind.NOTE,
							""+f.exists());
					f.delete();
					processingEnv.getMessager().printMessage(
							Diagnostic.Kind.NOTE,
							""+f.exists());
				}
*/
					{
						java.util.Properties props = new java.util.Properties();
						java.net.URL url = this.getClass().getClassLoader().getResource("de/elbosso/util/data/Processor.properties");
						props.load(url.openStream());

						org.apache.velocity.app.VelocityEngine ve = new org.apache.velocity.app.VelocityEngine(props);
						ve.setProperty(VelocityEngine.RUNTIME_LOG_INSTANCE, NOPLogger.NOP_LOGGER);
						ve.init();

						org.apache.velocity.VelocityContext vc = new org.apache.velocity.VelocityContext();
						vc.put("className", className);
						vc.put("packageName", packageName);
						vc.put("methods", methods);
						vc.put("indexedProperties", indexedProperties);
						vc.put("properties", properties);
						vc.put("eventSets", events);
						vc.put("generatorname", this.getClass().getName());
						for (java.lang.String key : contextContent.keySet())
						{
							vc.put(key, contextContent.get(key));
						}

						org.apache.velocity.Template vt = ve.getTemplate("de/elbosso/util/data/BeanInfoClass.template");
						if (!quiet)
							processingEnv.getMessager().printMessage(
									Diagnostic.Kind.NOTE,
									"applying velocity template: " + vt.getName());

						JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
								fqClassName + "BeanInfo");


						if (!quiet)
							processingEnv.getMessager().printMessage(
									Diagnostic.Kind.NOTE,
									"creating source file: " + jfo.toUri());

						java.io.Writer writer = jfo.openWriter();


						vt.merge(vc, writer);

						writer.close();
					}
				} catch (java.lang.Throwable t)
				{
					t.printStackTrace();
				}
			}
		}
		return true; // no further processing of this annotation type
	}

	private void handleMethod(String fqClassName, ExecutableElement exeElement)
	{
		String sn = exeElement.getSimpleName().toString();
		if (!quiet)
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.NOTE,
					"annotated method: " + sn+" ("+fqClassName+")", exeElement);
		List params = exeElement.getParameters();
		java.util.Map<java.lang.String, java.lang.Object> thingummys=new java.util.HashMap();
		thingummys.put("params", params);
		thingummys.put("fqClassName", fqClassName);

		methods.put(sn,thingummys);
		de.elbosso.util.lang.annotations.Method methodInfo = exeElement.getAnnotation(de.elbosso.util.lang.annotations.Method.class);
		if(methodInfo != null)
		{
			java.util.Map<java.lang.String, java.lang.Object> contextContent=new java.util.HashMap();
			thingummys.put("contextContent",contextContent);
			if(methodInfo.description().equals("--")==false)
				contextContent.put("Description", methodInfo.description());
			if(methodInfo.displayName().equals("--")==false)
				contextContent.put("DisplayName", methodInfo.displayName());
//					if(methodInfo.moduleWidgetDefaultLayerTitle().equals("--")==false)
//						contextContent.put("beanDescriptorModuleWidgetDefaultLayerTitle", methodInfo.moduleWidgetDefaultLayerTitle());
			contextContent.put("Hidden",methodInfo.hidden());
			contextContent.put("Expert",methodInfo.expert());
			contextContent.put("Preferred",methodInfo.preferred());
//					contextContent.put("beanDescriptor",methodInfo);
			de.elbosso.util.lang.annotations.KeyValueStore[] keyValueStores=methodInfo.keyValueStore();
			if(keyValueStores!=null)
			{
				java.util.Map<java.lang.String, java.lang.String> valueMap=new java.util.HashMap();
				for (de.elbosso.util.lang.annotations.KeyValueStore keyValueStore : keyValueStores)
				{
					if (valueMap.containsKey(keyValueStore.key()))
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.WARNING,
								"key "+keyValueStore.key()+" set more than once with different values! (annotation Method on "+exeElement+" in "+exeElement.getEnclosingElement()+")");
					valueMap.put(keyValueStore.key(),keyValueStore.value());
				}
				contextContent.put("ValueMap",valueMap);
			}
		}

	}

	private void handleProperty(String fqClassName, ExecutableElement exeElement)
	{
		String sn = exeElement.getSimpleName().toString();
		String pn = null;
		String readMethod = null;
		String writeMethod = null;
		String type = null;
		if (sn.startsWith("set"))
		{
			writeMethod = sn;
			type = exeElement.getParameters().get(0).asType().toString();
			pn = sn.substring(3);
		}
		else
		{
			readMethod = sn;
			type = exeElement.getReturnType().toString();
			if (sn.startsWith("get"))
				pn = sn.substring(3);
			if (sn.startsWith("is"))
				pn = sn.substring(2);
		}
		if ((pn != null) && ((readMethod != null) || (writeMethod != null)))
		{
			pn = pn.substring(0, 1).toLowerCase() + pn.substring(1);
			if (!quiet)
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.NOTE,
						"annotated property: " + pn, exeElement);
			java.util.Map holder = properties.get(pn);
			if (holder == null)
			{
				holder = new java.util.HashMap();
				holder.put("type", type);
				properties.put(pn, holder);
			}
			if (readMethod != null)
				holder.put("readMethod", readMethod);
			if (writeMethod != null)
				holder.put("writeMethod", writeMethod);
			holder.put("declaringClass",fqClassName);
			de.elbosso.util.lang.annotations.Property propertyInfo = exeElement.getAnnotation(de.elbosso.util.lang.annotations.Property.class);
			if(propertyInfo != null)
			{
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Description","description",propertyInfo.description());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"DisplayName","displayName",propertyInfo.displayName());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Hidden","hidden",propertyInfo.hidden());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Expert","expert",propertyInfo.expert());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Preferred","preferred",propertyInfo.preferred());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Bound","bound",propertyInfo.bound());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Constrained","constrained",propertyInfo.constrained());
				List<? extends TypeMirror> l = null;
				try
				{
					propertyInfo.propertyEditorClass();
				} catch (MirroredTypesException mte)
				{
					l = mte.getTypeMirrors();
				}
				List<de.netsysit.util.lang.Tupel<java.lang.String, java.lang.String>> ll = new java.util.LinkedList();
				for (TypeMirror tm : l)
				{
					java.lang.String a = tm.toString();
					a = a.substring(a.lastIndexOf('.') + 1);
					a = a.replace("[]", "Array");
					ll.add(new de.netsysit.util.lang.Tupel(a, tm.toString()));
				}
				if((ll.size()>0)&&(ll.get(0).getRighty().equals("java.lang.Void")==false))
				{
					manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"PropertyEditorClass","propertyEditorClass",ll.get(0).getRighty());
				}
				de.elbosso.util.lang.annotations.KeyValueStore[] keyValueStores=propertyInfo.keyValueStore();
				if(keyValueStores!=null)
				{
					java.util.Map<java.lang.String, java.lang.String> valueMap=null;
					if(holder.containsKey("ValueMap"))
						valueMap=(java.util.Map<java.lang.String, java.lang.String>)holder.get("ValueMap");
					else
					{
						valueMap = new java.util.HashMap();
						holder.put("ValueMap", valueMap);
					}
					for (de.elbosso.util.lang.annotations.KeyValueStore keyValueStore : keyValueStores)
					{
						if (valueMap.containsKey(keyValueStore.key()))
							processingEnv.getMessager().printMessage(
									Diagnostic.Kind.WARNING,
									"key "+keyValueStore.key()+" set more than once with different values! (annotation Property on "+exeElement+" in "+exeElement.getEnclosingElement()+")");
						valueMap.put(keyValueStore.key(),keyValueStore.value());
					}
				}
			}
		}
	}
	private void handleIndexedProperty(String fqClassName, ExecutableElement exeElement)
	{
		String sn = exeElement.getSimpleName().toString();
		String pn = null;
		String readMethod = null;
		String indexedReadMethod = null;
		String writeMethod = null;
		String indexedWriteMethod = null;
		String type = null;
		if (sn.startsWith("set"))
		{
			if(exeElement.getParameters().size()==2)
			{
				indexedWriteMethod = sn;
				type = exeElement.getParameters().get(1).asType().toString();
			}
			else
			{
				writeMethod = sn;

				type = processingEnv.getTypeUtils().getArrayType(exeElement.getParameters().get(0).asType()).toString();
			}
			pn = sn.substring(3);
		}
		else
		{
			if(exeElement.getParameters().size()==2)
			{
				indexedReadMethod = sn;
				type = exeElement.getReturnType().toString();
			}
			else
			{
				readMethod = sn;
				type = processingEnv.getTypeUtils().getArrayType(exeElement.getReturnType()).toString();
			}
			if (sn.startsWith("get"))
				pn = sn.substring(3);
			if (sn.startsWith("is"))
				pn = sn.substring(2);
		}
		if ((pn != null) && (((readMethod != null) || (writeMethod != null))||((indexedReadMethod != null)||(indexedWriteMethod != null))))
		{
			pn = pn.substring(0, 1).toLowerCase() + pn.substring(1);
			if (!quiet)
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.NOTE,
						"annotated indexed property: " + pn, exeElement);
			java.util.Map holder = indexedProperties.get(pn);
			if (holder == null)
			{
				holder = new java.util.HashMap();
				holder.put("type", type);
				indexedProperties.put(pn, holder);
			}
			if (indexedReadMethod != null)
				holder.put("indexedReadMethod", indexedReadMethod);
			if (readMethod != null)
				holder.put("readMethod", readMethod);
			if (indexedWriteMethod != null)
				holder.put("indexedWriteMethod", indexedWriteMethod);
			if (writeMethod != null)
				holder.put("writeMethod", writeMethod);
			holder.put("declaringClass",fqClassName);
			de.elbosso.util.lang.annotations.Property propertyInfo = exeElement.getAnnotation(de.elbosso.util.lang.annotations.Property.class);
			if(propertyInfo != null)
			{
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Description","description",propertyInfo.description());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"DisplayName","displayName",propertyInfo.displayName());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Hidden","hidden",propertyInfo.hidden());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Expert","expert",propertyInfo.expert());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Preferred","preferred",propertyInfo.preferred());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Bound","bound",propertyInfo.bound());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"Constrained","constrained",propertyInfo.constrained());
				List<? extends TypeMirror> l = null;
				try
				{
					propertyInfo.propertyEditorClass();
				} catch (MirroredTypesException mte)
				{
					l = mte.getTypeMirrors();
				}
				List<de.netsysit.util.lang.Tupel<java.lang.String, java.lang.String>> ll = new java.util.LinkedList();
				for (TypeMirror tm : l)
				{
					java.lang.String a = tm.toString();
					a = a.substring(a.lastIndexOf('.') + 1);
					a = a.replace("[]", "Array");
					ll.add(new de.netsysit.util.lang.Tupel(a, tm.toString()));
				}
				if((ll.size()>0)&&(ll.get(0).getRighty().equals("java.lang.Void")==false))
				{
					manageAttribute(exeElement, de.elbosso.util.lang.annotations.Property.class,holder,"PropertyEditorClass","propertyEditorClass",ll.get(0).getRighty());
				}
				de.elbosso.util.lang.annotations.KeyValueStore[] keyValueStores=propertyInfo.keyValueStore();
				if(keyValueStores!=null)
				{
					java.util.Map<java.lang.String, java.lang.String> valueMap=null;
					if(holder.containsKey("ValueMap"))
						valueMap=(java.util.Map<java.lang.String, java.lang.String>)holder.get("ValueMap");
					else
					{
						valueMap = new java.util.HashMap();
						holder.put("ValueMap", valueMap);
					}
					for (de.elbosso.util.lang.annotations.KeyValueStore keyValueStore : keyValueStores)
					{
						if (valueMap.containsKey(keyValueStore.key()))
							processingEnv.getMessager().printMessage(
									Diagnostic.Kind.WARNING,
									"key "+keyValueStore.key()+" set more than once with different values! (annotation Property on "+exeElement+" in "+exeElement.getEnclosingElement()+")");
						valueMap.put(keyValueStore.key(),keyValueStore.value());
					}
				}
			}
		}
	}
	private void manageAttribute(ExecutableElement exeElement,Class<?> clazz,java.util.Map holder,java.lang.String key, java.lang.String methodName,java.lang.Object attributeValue)
	{
		{
			boolean setit=false;
			try
			{
				java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName);
				Object defaultValue = method.getDefaultValue();
				setit=defaultValue.equals(attributeValue)==false;
				if((setit)&&(holder.containsKey(key)))
				{
					if (holder.get(key).equals(attributeValue) == false)
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.ERROR,
								"property attribute "+methodName+" set more than once with different values ("+attributeValue+" / "+holder.get(key)+") (annotation "+clazz.getSimpleName()+" on "+exeElement+" in "+exeElement.getEnclosingElement()+")");
				}
			}
			catch(java.lang.NoSuchMethodException exp)
			{
				setit=false;
			}
			if(setit)
				holder.put(key,attributeValue);
		}
	}
	private void handleEvent(java.lang.String fqClassName,ExecutableElement exeElement)
	{
		String sn = exeElement.getSimpleName().toString();
		String en = null;
		String ln = null;
		String addMethod = null;
		String removeMethod = null;
		String type = null;
		if (sn.startsWith("add"))
		{
			addMethod = sn;
			type = exeElement.getParameters().get(0).asType().toString();
			ln = sn.substring(3);
		}
		else
		{
			removeMethod = sn;
			type = exeElement.getParameters().get(0).asType().toString();
			ln = sn.substring(6);
		}
		if ((ln != null) && ((addMethod != null) || (removeMethod != null)))
		{
			ln = ln.substring(0, 1).toLowerCase() + ln.substring(1);
			en=ln.substring(0,ln.lastIndexOf("Listener"));
			if (!quiet)
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.NOTE,
						"annotated eventSet: " + ln, exeElement);
			java.util.Map holder = events.get(en);
			if (holder == null)
			{
				holder = new java.util.HashMap();
				holder.put("type", type);
				holder.put("listener",ln);
				holder.put("declaringClass",fqClassName);
				events.put(en, holder);
			}
			if (addMethod != null)
			{
				holder.put("addMethod", addMethod);
				holder.put("declaringClass",fqClassName);
			}
			if (removeMethod != null)
			{
				holder.put("removeMethod", removeMethod);
				holder.put("declaringClass",fqClassName);
			}
			de.elbosso.util.lang.annotations.Event eventInfo = exeElement.getAnnotation(de.elbosso.util.lang.annotations.Event.class);
			if(eventInfo != null)
			{
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Event.class,holder,"InDefaultEventSet","inDefaultEventSet",eventInfo.inDefaultEventSet());
				manageAttribute(exeElement, de.elbosso.util.lang.annotations.Event.class,holder,"Unicast","unicast",eventInfo.unicast());
/*				if(holder.containsKey("InDefaultEventSet")==false)
					holder.put("InDefaultEventSet", eventInfo.inDefaultEventSet());
				if(holder.containsKey("Unicast")==false)
					holder.put("Unicast", eventInfo.unicast());
*/			}
		}
	}

	private java.util.List<java.lang.String> workOnSuperClass(TypeElement classElement)
	{
		java.util.List<java.lang.String> i18nBundles=new java.util.LinkedList();
		processingEnv.getMessager().printMessage(
				Diagnostic.Kind.NOTE,
				"workOnSuperClass - class: " + classElement.getQualifiedName(), classElement);
		TypeElement superClassElement =processingEnv.getElementUtils().getTypeElement(classElement.getSuperclass().toString());
		if(superClassElement!=null)
		{
			processingEnv.getMessager().printMessage(
					Diagnostic.Kind.NOTE,
					"workOnSuperClass - super class: " + superClassElement.getQualifiedName(), superClassElement);
			java.lang.String fqClassName = superClassElement.getQualifiedName().toString();
			List<? extends Element> superClassElements = superClassElement.getEnclosedElements();
			de.elbosso.util.lang.annotations.BeanInfo bi=superClassElement.getAnnotation(de.elbosso.util.lang.annotations.BeanInfo.class);
			if(bi!=null)
			{
				if(bi.i18nBundle().equals("--")==false)
				{
					processingEnv.getMessager().printMessage(
							Diagnostic.Kind.NOTE,
							"found i18n bundle: " + bi.i18nBundle());
					i18nBundles.add(bi.i18nBundle());
				}
			}
			for(Element e:superClassElements)
			{
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.NOTE,
						"element: " + e.getSimpleName()+" "+(e.getKind() == ElementKind.METHOD)+" "+e.getAnnotationMirrors().toString());
				if(e.getKind() == ElementKind.METHOD)
				{
					ExecutableElement exeElement = (ExecutableElement) e;
					//processingEnv.getMessager().printMessage(
					//Diagnostic.Kind.NOTE,exeElement.getAnnotationMirrors().toString());
					if(exeElement.getAnnotation(de.elbosso.util.lang.annotations.Event.class)!=null)
					{
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.NOTE,
								"found event");
						handleEvent(fqClassName,exeElement);
					}
					else if(exeElement.getAnnotation(de.elbosso.util.lang.annotations.Property.class)!=null)
					{
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.NOTE,
								"found property");
						handleProperty(fqClassName,exeElement);
					}
					else if(exeElement.getAnnotation(de.elbosso.util.lang.annotations.Method.class)!=null)
					{
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.NOTE,
								"found method "+fqClassName+" "+exeElement);

						handleMethod(fqClassName,exeElement);
					}
				}
			}
			i18nBundles.addAll(workOnSuperClass(superClassElement));
		}
		return i18nBundles;
	}
}
