/*
	MethodDispatcher.java
	
	Author: David Fogel
	
	Copyright 2004-2009
	
	All rights reserved.
*/

// *** package ***
package us.fogel.events;

// *** imports ***
import java.lang.reflect.*;

/**
 * MethodDispatcher
 *
 * Comment here.  Author: David Fogel
 */
public class MethodDispatcher implements Dispatcher {
	// *** Class Members ***

	// *** Instance Members ***
	private Method theMethod;
	private boolean usesEvent;

	// *** Constructors ***
	
	public MethodDispatcher(Method method) {
		theMethod = method;
		usesEvent = theMethod.getParameterTypes().length > 0;
	}

	// *** Dispatcher Methods ***
	
	public void dispatchEvent(Object event, Object target) throws Exception {
		try {
			
			if (usesEvent)
				theMethod.invoke(target, event);
			else
				theMethod.invoke(target);
			
		} catch (InvocationTargetException ite) {
			Throwable t = ite.getCause();
			if (t != null && t instanceof Exception)
				throw (Exception) t;
			else
				throw ite;
		}
	}

	// *** Public Methods ***

	// *** Protected Methods ***

	// *** Package Methods ***

	// *** Private Methods ***

	// *** Private Classes ***
}










/* end */