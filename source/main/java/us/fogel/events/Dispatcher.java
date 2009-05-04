/*
	Dispatcher.java
	
	Author: David Fogel
	
	Copyright 2004-2009
	
	All rights reserved.
*/

// *** package ***
package us.fogel.events;

// *** imports ***

/**
 * Dispatcher
 *
 * Comment here.  Author: David Fogel
 */
public interface Dispatcher {
	// *** Class Members ***

	// *** Public Methods ***
	
	public void dispatchEvent(Object event, Object target) throws Exception;
}










/* end */