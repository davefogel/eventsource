/*
	AbstractEventSource.java
	
	Author: David Fogel
	
	Copyright 2004-2009
	
	All rights reserved.
*/

// *** package ***
package us.fogel.events;

// *** imports ***

import java.lang.reflect.*;
import java.util.*;

/**
 * AbstractEventSource
 *
 * Comment here.  Author: David Fogel
 */
public abstract class AbstractEventSource implements EventSource {
	// *** Class Members ***

	// *** Instance Members ***

	// *** Constructors ***
	
	public AbstractEventSource() {
		
	}

	// *** Interface Methods ***

	// *** Public Methods ***
	
	public void addEventTarget(EventListener target, String method) {
		
		Class<?> eventType = guessEventType(target.getClass());
		if (eventType == null)
			throw new IllegalArgumentException("No matching Event class for EventListener " + target.getClass());
		
		addEventTarget(eventType, target, method);
	}
	
	public void addWeakEventTarget(EventListener target, String method) {
		
		Class<?> eventType = guessEventType(target.getClass());
		if (eventType == null)
			throw new IllegalArgumentException("No matching Event class for EventListener " + target.getClass());
		
		addWeakEventTarget(eventType, target, method);
	}
	
	public void addEventTarget(EventListener target, Dispatcher dispatcher) {
		
		Class<?> eventType = guessEventType(target.getClass());
		if (eventType == null)
			throw new IllegalArgumentException("No matching Event class for EventListener " + target.getClass());
		
		addEventTarget(eventType, target, dispatcher, true);
	}
	
	public void addWeakEventTarget(EventListener target, Dispatcher dispatcher) {
		
		Class<?> eventType = guessEventType(target.getClass());
		if (eventType == null)
			throw new IllegalArgumentException("No matching Event class for EventListener " + target.getClass());
		
		addEventTarget(eventType, target, dispatcher, false);
	}
	
	public void addEventTarget(Class<?> eventType, Object target) {
		
		addEventTarget(eventType, target, "processEvent");
	}
	
	public void addWeakEventTarget(Class<?> eventType, Object target) {
		
		addWeakEventTarget(eventType, target, "processEvent");
	}
	
	public void addEventTarget(Class<?> eventType, Object target, String methodName) {

		Method method = getTargetMethod(eventType, target, methodName);
		if (method == null)
			throw new IllegalArgumentException("No method named " + methodName + " found for class " +
					target.getClass() + " with parameter " + eventType.getName());
		
		addEventTarget(eventType, target, new MethodDispatcher(method), true);
	}
	
	public void addWeakEventTarget(Class<?> eventType, Object target, String methodName) {
		
		Method method = getTargetMethod(eventType, target, methodName);
		if (method == null)
			throw new IllegalArgumentException("No method named " + methodName + " found for class " +
					target.getClass() + " with parameter " + eventType.getName());
		
		addEventTarget(eventType, target, new MethodDispatcher(method), false);
	}
	
	public void addEventTarget(Class<?> eventType, Object target, Method method) {
		
		if (!Modifier.isPublic(target.getClass().getModifiers()) || !Modifier.isPublic(method.getModifiers()))
			method.setAccessible(true);
		
		addEventTarget(eventType, target, new MethodDispatcher(method), true);
	}
	
	public void addWeakEventTarget(Class<?> eventType, Object target, Method method) {
		
		if (!Modifier.isPublic(target.getClass().getModifiers()) || !Modifier.isPublic(method.getModifiers()))
			method.setAccessible(true);
		
		addEventTarget(eventType, target, new MethodDispatcher(method), false);
	}
	
	public void removeEventTarget(EventListener target) {
		
		Class<?> eventType = guessEventType(target.getClass());
		if (eventType == null)
			throw new IllegalArgumentException("No matching Event class for EventListener " + target.getClass());
		
		removeEventTarget(eventType, target);
	}
	
	public abstract void addEventTarget(Class<?> eventType,
										Object target,
										Dispatcher dispatcher,
										boolean strongReference);
	
	public abstract void removeEventTarget(Class<?> eventType, Object target);
	
	public abstract void clearEventTargets();
	
	public abstract boolean hasEventTargetsFor(Class<?> eventType);
	
	public abstract void dispatchEvent(Object event);

	// *** Protected Methods ***
	
	protected void handleEventDispatchException(	Object event, 
			Object target, 
			Dispatcher dispatcher, 
			Exception e) {
		e.printStackTrace(); // okay default?
	}
	
	protected Class<?> guessEventType(Class<?> targetType) {
		
		Class<?> type = targetType;
		while (type != null) {
			
			Class<?>[] interfaces = type.getInterfaces();
			for ( int i = 0 ; i < interfaces.length ; i++) {
				
				Class<?> c = interfaces[i];
				if (EventListener.class.isAssignableFrom(c)) {
					
					Class<?> eventType = getEventType(c);
					
					if (eventType != null)
						return eventType;
				}
			}
			
			type = type.getSuperclass();
		}
		
		return null;
	}
	
	protected Class<?> getEventType(Class<?> listenerType) {
		
		String fullname = listenerType.getName();
		
		if ( ! fullname.endsWith("Listener"))
			return null; // don't know what we have here...
		
		String eventName = fullname.substring(0, fullname.lastIndexOf("Listener")) + "Event";
		
		ClassLoader loader = listenerType.getClassLoader();
		if (loader == null) // getClassLoader() will sometimes return null if it's the system loader.
			loader = ClassLoader.getSystemClassLoader();
		
		Class<?> eventType = null;
		
		try {
			eventType = loader.loadClass(eventName);
		} catch (ClassNotFoundException cnfe) {
			eventType = null;
		}
		
		return eventType;
	}
	
	protected Method getTargetMethod(Class<?> eventType, Object target, String methodName) {
		
		Class<?> type = target.getClass();    
		while (type != null)
		{
			Method[] methods = type.getDeclaredMethods();
			
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals(methodName)) {
					Class<?>[] params = methods[i].getParameterTypes();
					if ((params.length == 1 && params[0].isAssignableFrom(eventType)) ||
							params.length == 0) {
						
						Method m = methods[i];
						
						if (!Modifier.isPublic(target.getClass().getModifiers()) || 
							!Modifier.isPublic(m.getModifiers()))
							m.setAccessible(true);
						
						return m;
					}
				}
			}
			
			type = type.getSuperclass();
		}
		
		return null;
	}

	// *** Package Methods ***

	// *** Private Methods ***

	// *** Private Classes ***
}










/* end */