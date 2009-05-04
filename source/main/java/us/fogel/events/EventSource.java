/*
 EventSource.java
 
 Author: David Fogel
 
 Copyright 2004-2009
 
 All rights reserved.
 */

// *** package ***
package us.fogel.events;

// *** imports ***

import java.lang.reflect.Method;
import java.util.EventListener;

/**
 * EventSource
 *
 * Comment here.  Author: David Fogel
 */
public interface EventSource {
	// *** Class Members ***

	// *** Public Methods ***
	
	public void addEventTarget(EventListener target, String method);

	public void addWeakEventTarget(EventListener target, String method);

	public void addEventTarget(EventListener target, Dispatcher dispatcher);

	public void addWeakEventTarget(EventListener target, Dispatcher dispatcher);

	public void addEventTarget(Class<?> eventType, Object target);

	public void addWeakEventTarget(Class<?> eventType, Object target);

	public void addEventTarget(Class<?> eventType, Object target, String methodName);

	public void addWeakEventTarget(Class<?> eventType, Object target, String methodName);

	public void addEventTarget(Class<?> eventType, Object target, Method method);

	public void addWeakEventTarget(Class<?> eventType, Object target, Method method);

	public void removeEventTarget(EventListener target);

	public void addEventTarget(Class<?> eventType, Object target, Dispatcher dispatcher, boolean strongReference);

	public void removeEventTarget(Class<?> eventType, Object target);

	public void clearEventTargets();

	public boolean hasEventTargetsFor(Class<?> eventType);

	public void dispatchEvent(Object event);
}

/* end */