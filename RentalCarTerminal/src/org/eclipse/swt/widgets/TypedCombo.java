package org.eclipse.swt.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Lists;

/**
 * Note: This implementation implements only the required methods for THIS
 * project. It is fairly easy to have inconsistent states when using the
 * wrong methods.
 * 
 * This class should be in the org.eclipse.swt.widgets package to work around
 * the foolish no-subclassing rule of SWT.
 * 
 * @author Bas Stottelaar
 */
public class TypedCombo<T> extends Combo {

	ArrayList<T> list = Lists.newArrayList();
	
	public TypedCombo(Composite arg0, int arg1) {
		super(arg0, arg1);
	}
	
	public void add(T item, int index) {
		this.list.add(index, item);
		this.add(item.toString(), index);
	}

	public void add(T item) {
		this.list.add(item);
		this.add(item.toString());
	}

	public T getTypedItem(int index) {
		return this.list.get(index);
	}

	public void remove(T item) {
		int index = this.list.indexOf(item);
		
		if (index != -1) {
			this.remove(index);
			this.list.remove(index);
		}
	}
	
	public void setItem(int index, T item) {
		this.setItem(index, item.toString());
		this.list.set(index, item);
	}
	
	public void setItems(List<T> items) {
		for (T item : items) {
			this.add(item);
		}
	}
	
	public T getSelected() {
		int index = this.getSelectionIndex();
		
		if (index != -1) {
			return this.list.get(index);
		}
		
		return null;
	}
	
	@Override
	public void removeAll() {
		this.removeAll();
		this.list.clear();
	}
}
