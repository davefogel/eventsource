/*
	StandardEventSource.java
	
	Author: David Fogel
	
	Copyright 2004-2009
	
	All rights reserved.
*/

// *** package ***
package us.fogel.events;

// *** imports ***

import java.lang.ref.*;
import java.util.*;

/**
 * StandardEventSource
 * 
 * inspired by Peter Eastman's StandardEventSource in his 
 * Buoy framework: http://buoy.sourceforge.net/
 *
 * Comment here.  Author: David Fogel
 */
public class StandardEventSource extends AbstractEventSource {
	// *** Class Members ***

	// *** Instance Members ***
	
	private List<Object> theEntryLists;

	// *** Constructors ***
	
	public StandardEventSource() {
		theEntryLists = null;
	}

	// *** Object Methods ***
	
	public String toString() {
		
		return getClass().getName() + " " + targetsToString();
	}
	
	public String targetsToString() {
		
		if (theEntryLists == null || theEntryLists.size() == 0)
			return "[no event targets]";
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("[");
		
		synchronized (theEntryLists) {
			
			int length = theEntryLists.size();
			for (int i = 0 ; i < length ; i+=2) {
				Class<?> type = (Class<?>) theEntryLists.get(i);
				List<?> entries = (List<?>) theEntryLists.get(i+1);
				int n = entries.size() / 2;
				if (n == 0)
					continue;
				if (i > 0)
					sb.append(", ");
				sb.append(type.getName());
				sb.append(": ");
				sb.append(n);
				if (n == 1)
					sb.append( " target");
				else
					sb.append( " targets");
			}
		}
		
		sb.append("]");
		
		return sb.toString();
	}

	// *** AbstractEventSource Methods ***
	
	public void addEventTarget(Class<?>eventType,
								Object target,
								Dispatcher dispatcher,
								boolean strongReference) {
		
		// TODO - yeah, yeah, double-checked locking doesn't quite work, but whatever.
		if (theEntryLists == null) {
			synchronized (this) {
				if (theEntryLists == null)
					theEntryLists = new ArrayList<Object>();
			}
		}
		
		synchronized (theEntryLists) {
			
			List<Object> entries = null;
			int length = theEntryLists.size();
			for (int i = 0 ; i < length ; i+=2) {
				Class<?> t = (Class<?>) theEntryLists.get(i);
				if (eventType == t) {
					entries = (List<Object>) theEntryLists.get(i + 1);
					break;
				}
			}
			
			if (entries == null) {
				entries = new ArrayList<Object>();
				theEntryLists.add(eventType);
				theEntryLists.add(entries);
			}
			
			entries.add(strongReference ? target : new ESWeakReference(target));
			entries.add(dispatcher);
		}
	}
	
	public void removeEventTarget(Class<?> eventType, Object target) {
		
		if (theEntryLists == null)
			return;
		
		synchronized (theEntryLists) {
			
			List<Object> entries = null;
			int length = theEntryLists.size();
			for (int i = 0 ; i < length ; i+=2) {
				Class<?> type = (Class<?>) theEntryLists.get(i);
				if (eventType == type) {
					entries = (List<Object>) theEntryLists.get(i + 1);
					break;
				}
			}
			
			if (entries == null)
				return;
			
			// we iterate backwards in order to make removing cleared
			// weak references a little easier (don't have to recalc length...)
			for (int i = (entries.size() - 2) ; i >= 0 ; i -= 2) {
				Object t = entries.get(i);
				
				// deal with references
				if (t instanceof ESWeakReference) {
					t = ((ESWeakReference) t).get();
					if (t == null) {
						// in this case, t is not the target, but needs to be removed anyway.
						entries.remove(i + 1); // dispatcher
						entries.remove(i); // target
						continue;
					}
				}
				
				// remove and return if equal
				if (t == target) {
					entries.remove(i + 1); // dispatcher
					entries.remove(i); // target
					break;
				}
			}
			
			// remove the entries list if empty:
			if (entries.isEmpty()) {
				theEntryLists.remove(entries);
				theEntryLists.remove(eventType);
			}
		}
	}
	
	public void clearEventTargets() {
		
		if (theEntryLists == null)
			return;
		
		synchronized (theEntryLists) {
			
			theEntryLists.clear();
		}
	}
	
	public boolean hasEventTargetsFor(Class<?> eventType) {
		
		if (theEntryLists == null || theEntryLists.size() == 0)
			return false;
		
		synchronized (theEntryLists) {
			
			int length = theEntryLists.size();
			for (int i = 0 ; i < length ; i+=2) {
				Class<?> type = (Class<?>) theEntryLists.get(i);
				if (type.isAssignableFrom(eventType))
					return true;
			}
		}
		
		return false;
	}
	
	public void dispatchEvent(Object event) {
		
		if (theEntryLists == null || theEntryLists.size() == 0)
			return;
		
		synchronized (theEntryLists) {
			
			// we iterate backwards in order to make removing empty
			// entry lists a little easier (don't have to recalc length...)
			for (int i = (theEntryLists.size() - 2) ; i >= 0 ; i-=2) {
				Class<?> type = (Class<?>) theEntryLists.get(i);
				if (type.isInstance(event)) {
					List<Object> entries = (List<Object>) theEntryLists.get(i+1);
					
					// we iterate backwards in order to make removing cleared
					// weak references a little easier (don't have to recalc length...)
					for (int j = (entries.size() - 2) ; j >= 0 ; j-=2) {
						
						Object target = entries.get(j);
						Dispatcher dispatcher = (Dispatcher) entries.get(j+1);
						
						// deal with references
						if (target instanceof ESWeakReference) {
							Object o = ((ESWeakReference)target).get();
							if (o == null) {
								
								entries.remove(j + 1); // dispatcher
								entries.remove(j); // target
								continue; // can't dispatch to null..
							}
							target = o; // this is the real target
						}
						
						// dispatch it
						try {
							
							dispatcher.dispatchEvent(event, target);
							
						} catch (Exception e) {
							handleEventDispatchException(event, target, dispatcher, e);
						}
					}
					
					// remove the entries list if empty:
					if (entries.isEmpty()) {
						theEntryLists.remove(entries);
						theEntryLists.remove(type);
					}
				}
			}
		}
	}
	
	// *** Public Methods ***

	// *** Protected Methods ***

	// *** Package Methods ***

	// *** Private Methods ***

	// *** Private Classes ***
	
	private static class ESWeakReference extends WeakReference<Object> {
		public ESWeakReference(Object referent) {
			super(referent);
		}
	}
	
}










/* end */