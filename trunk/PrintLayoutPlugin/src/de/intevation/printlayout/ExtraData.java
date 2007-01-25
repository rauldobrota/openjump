/*
 * DocumentManager.java
 * --------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.EventListener;

import org.apache.batik.dom.AbstractElement;

import java.io.Serializable;

public class ExtraData
implements   Serializable
{
	protected HashMap id2entry;

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

	public interface ChangeListener
	extends          Serializable, EventListener
	{
		void elementTransformed(Event evt);
	}

	public interface RemoveListener
	extends          Serializable, EventListener
	{
		void elementRemoved(Event event);
	}

	public static class Entry 
	implements          Serializable
	{
		protected ArrayList removeListeners;
		protected ArrayList changeListeners;

		protected Object    data;

		public Entry() {
		}

		public ArrayList getRemoveListeners() {
			return removeListeners;
		}

		public void setRemoveListeners(ArrayList removeListeners) {
			this.removeListeners = removeListeners;
		}

		public ArrayList getChangeListeners() {
			return changeListeners;
		}

		public void setChangeListeners(ArrayList changeListeners) {
			this.changeListeners = changeListeners;
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
	} // end of file

	public ExtraData() {
		id2entry = new HashMap();
	}

	public void remove(Object source, AbstractElement element) {
		String id = element.getAttributeNS(null, "id");
		if (id == null)
			return;
		Entry entry = (Entry)id2entry.remove(id);
		if (entry == null)
			return;
		entry.remove(source, element);
	}

	public void fireElementTransformed(Object source, AbstractElement element) {
		String id = element.getAttributeNS(null, "id");
		if (id == null)
			return;
		Entry entry = (Entry)id2entry.get(id);
		if (entry == null)
			return;
		entry.fireElementTransformed(source, element);
	}

	public HashMap getID2Entry() {
		return id2entry;
	}

	public void setID2Entry(HashMap id2entry) {
		this.id2entry = id2entry;
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
