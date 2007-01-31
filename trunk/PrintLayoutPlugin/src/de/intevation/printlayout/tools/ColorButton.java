/*
 * ColorButton.java
 * ----------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

public class ColorButton 
extends JButton
implements ActionListener
{
	public class ColorChangedEvent {}
	public interface ColorChangedListener {
		void colorChanged(ColorChangedEvent cce);
	}
	
	protected JColorChooser chooser = new JColorChooser(Color.BLACK);
  protected ArrayList listeners = new ArrayList(5);
	
	public ColorButton() {
		setBackground(chooser.getColor());
		setPreferredSize(new Dimension(40, 20));
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent ae) {
		JDialog dialog = JColorChooser.createDialog(
				this,
				"Color",
				false,
				chooser,
				new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ColorButton.this.setBackground(chooser.getColor());
						ColorButton.this.fireColorChanged();
					}
				},
				null);
		
		dialog.setVisible(true);
	}
	
	public void addColorChangedListener(ColorChangedListener listener) {
		listeners.add(listener);
	}

	public void removeColorChangeListener(ColorChangedListener listener) {
		while(listeners.contains(listener))
			listeners.remove(listeners.indexOf(listener));
	}
	
	protected void fireColorChanged() {
		ColorChangedEvent event = new ColorChangedEvent();
		
		for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
			Object listener = iter.next();
			if (listener instanceof ColorChangedListener)
				((ColorChangedListener) listener).colorChanged(event);
		}
	}
	
	protected Color getColor() {
		return chooser.getColor();
	}

	public void setColor(Color color) {
		chooser.setColor(color);
		
		setBackground(chooser.getColor());
		fireColorChanged();
	}
	
	
	public static void main(String[] argv) {
		JFrame frame = new JFrame("test");
		frame.setSize(new Dimension(400, 400));
		
		JPanel panel = new JPanel();
		panel.add(new ColorButton());
	
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		frame.setVisible(true);
	}
}
