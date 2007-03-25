/*
 * ExtraData.java
 * --------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.beans;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Map;
import java.util.Iterator;

import org.apache.batik.dom.AbstractElement;

import java.io.Serializable;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Statement;   
import java.beans.Encoder;   

/**
 * Associative lookup between the id of on SVG element and
 * some special data extracted from OpenJump.
 */

public class ExtraData
implements   Serializable
{
	protected HashMap id2entry;

	/**
	 * Event fired by ChangeListener and RemoveListener.
	 */
	public static class Event
	extends             EventObject
	{
		protected AbstractElement element;

		public Event(Object source, AbstractElement element) {
			super(source);
			this.element = element;
		}

		public AbstractElement getElement() {
			return element;
		}
	}

	/**
	 * informed if a certain SVG element with a given id has been changed
	 * (e.g. being transformed).
	 */
	public interface ChangeListener
	extends          Serializable, EventListener
	{
		void elementTransformed(Event evt);
	}

	/**
	 * informed if a certain SVG element with a given id has
	 * been removed from the SVG document.
	 */
	public interface RemoveListener
	extends          Serializable, EventListener
	{
		void elementRemoved(Event event);
	}

	/**
	 * Class that models an entry id to listeners and extra data
	 */
	public static class Entry 
	implements          Serializable
	{
		protected ArrayList removeListeners;
		protected ArrayList changeListeners;

		public static class PersistenceDelegate
		extends             DefaultPersistenceDelegate
		{
			public PersistenceDelegate() {
			}

			protected void initialize(
				Class   clazz, 
				Object  oldInstance, 
				Object  newInstance,
				Encoder encoder
			) {
				super.initialize(clazz, oldInstance, newInstance, encoder);
				Entry entry = (Entry)oldInstance;

				if (entry.removeListeners != null)
					for (int i = 0, N = entry.removeListeners.size(); i < N; ++i)
						encoder.writeStatement(
							new Statement(
								oldInstance, 
								"addRemoveListener", 
								new Object [] { entry.removeListeners.get(i) }));

				if (entry.changeListeners != null)
					for (int i = 0, N = entry.changeListeners.size(); i < N; ++i)
						encoder.writeStatement(
							new Statement(
								oldInstance, 
								"addChangeListener", 
								new Object [] { entry.changeListeners.get(i) }));

			}
		} // end of PersistenceDelegate

		protected Object    data;

		public Entry() {
		}

		public void setData(Object data) {
			this.data = data;
		}

		public Object getData() {
			return data;
		}

		public boolean allEmpty() {
			return removeListeners == null
				&&   changeListeners == null
				&&   data == null;
		}

		public void addRemoveListener(RemoveListener l) {
			if (removeListeners == null)
				removeListeners = new ArrayList(3);
			if (!removeListeners.contains(l))
				removeListeners.add(l);
		}

		public void removeRemoveListener(RemoveListener l) {
			if (removeListeners != null 
			&& removeListeners.remove(l)
			&& removeListeners.isEmpty())
				removeListeners = null;
		}

		public void addChangeListener(ChangeListener l) {
			if (changeListeners == null)
				changeListeners = new ArrayList(3);
			if (!changeListeners.contains(l))
				changeListeners.add(l);
		}

		public void removeChangeListener(ChangeListener l) {
			if (changeListeners != null 
			&& changeListeners.remove(l)
			&& changeListeners.isEmpty())
				changeListeners = null;
		}

		protected void fireElementRemoved(Object source, AbstractElement element) {
			if (removeListeners != null) {
				Event event = new Event(source, element);
				for (int i = removeListeners.size()-1; i >= 0; --i)
					((RemoveListener)removeListeners.get(i)).elementRemoved(event);
			}
		}

		protected void fireElementTransformed(
			Object          source, 
			AbstractElement element
		) {
			if (changeListeners != null) {
				Event event = new Event(source, element);
				for (int i = changeListeners.size()-1; i >= 0; --i)
					((ChangeListener)changeListeners.get(i)).elementTransformed(event);
			}
		}

		public boolean hasChangeListeners() {
			return changeListeners != null && !changeListeners.isEmpty();
		}

		public void clear() {
			if (removeListeners != null) {
				ArrayList rl = removeListeners;
				removeListeners = null;
				rl.clear();
			}
			if (changeListeners != null) {
				ArrayList cl = changeListeners;
				changeListeners = null;
				cl.clear();
			}

			data = null;
		}

		public void remove(Object source, AbstractElement element) {
			fireElementRemoved(source, element);
			clear();
		}
	} // end of Entry

	public ExtraData() {
		id2entry = new HashMap();
	}

	public static class PersistenceDelegate
	extends             DefaultPersistenceDelegate
	{
		public PersistenceDelegate() {
		}

		protected void initialize(
			Class   clazz, 
			Object  oldInstance, 
			Object  newInstance,
			Encoder encoder
		) {
			super.initialize(clazz, oldInstance, newInstance, encoder);
			ExtraData extraData = (ExtraData)oldInstance;

			for (Iterator e = extraData.id2entry.entrySet().iterator(); e.hasNext();) {
				Map.Entry entry = (Map.Entry)e.next();
				encoder.writeStatement(
					new Statement(
						oldInstance, 
						"installEntry", 
						new Object [] { entry.getKey(), entry.getValue() }));
			}
		}
	} // end of PersistenceDelegate

	public void remove(Object source, AbstractElement element) {
		String id = element.getAttributeNS(null, "id");
		if (id != null) {
			Entry entry = (Entry)id2entry.remove(id);
			if (entry != null)
				entry.remove(source, element);
		}
	}

	public void fireElementTransformed(Object source, AbstractElement element) {
		String id = element.getAttributeNS(null, "id");
		if (id != null) {
			Entry entry = (Entry)id2entry.get(id);
			if (entry != null)
				entry.fireElementTransformed(source, element);
		}
	}

	public void installEntry(String id, Entry entry) {
		if (id == null || entry == null)
			throw new IllegalArgumentException("id or entry == null");

		id2entry.put(id, entry);
	}

	public void setData(String id, Object data) {
		Entry entry = (Entry)id2entry.get(id);
		if (entry == null)
			id2entry.put(id, entry = new Entry());
		entry.setData(data);
	}

	public Object getData(String id) {
		Entry entry = (Entry)id2entry.get(id);
		return entry != null
			? entry.getData()
			: null;
	}

	public Entry getOrCreateEntry(String id) {
		Entry entry = (Entry)id2entry.get(id);
		if (entry == null)
			id2entry.put(id, entry = new Entry());
		return entry;
	}

	public void addRemoveListener(String id, RemoveListener l) {
		Entry entry = (Entry)id2entry.get(id);
		if (entry == null)
			id2entry.put(id, entry = new Entry());
		entry.addRemoveListener(l);
	}

	public void removeRemoveListener(String id, RemoveListener l) {
		Entry entry = (Entry)id2entry.get(id);
		if (entry == null)
			return;
		entry.removeRemoveListener(l);
		if (entry.allEmpty())
			id2entry.remove(id);
	}

	public void addChangeListener(String id, ChangeListener l) {
		Entry entry = (Entry)id2entry.get(id);
		if (entry == null)
			id2entry.put(id, entry = new Entry());
		entry.addChangeListener(l);
	}

	public void removeChangeListener(String id, ChangeListener l) {
		Entry entry = (Entry)id2entry.get(id);
		if (entry == null)
			return;
		entry.removeChangeListener(l);
		if (entry.allEmpty())
			id2entry.remove(id);
	}

	public boolean hasChangeListeners(String id) {
		Entry entry = (Entry)id2entry.get(id);
		return entry != null && entry.hasChangeListeners();
	}
}
// end of file
