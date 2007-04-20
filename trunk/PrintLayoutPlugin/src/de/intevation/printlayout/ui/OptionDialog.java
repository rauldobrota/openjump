/*
 * OptionDialog.java
 * -----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Iterator;

import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JFormattedTextField;
import javax.swing.JButton;

import de.intevation.printlayout.Options;
import de.intevation.printlayout.I18N;

public class OptionDialog extends JDialog {

	protected abstract class Option {
		protected String optionName;
		protected String description;
		abstract void put();
		abstract JComponent createPanel();
	}

	protected class BooleanOption extends Option {
		private JCheckBox box;
		
		protected BooleanOption(String optionName, String description) {
			super.optionName = optionName;
			super.description = description;
		}

		protected void put() {
			if (box != null)
				Options.getInstance().putBoolean(optionName, box.isSelected());
		}

		protected JComponent createPanel() {
			box = new JCheckBox(
					description, Options.getInstance().getBoolean(optionName));
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(box);
			return panel;
		}
	}

	protected class IntOption extends Option {
		JFormattedTextField field;
		
		protected IntOption(String optionName, String description) {
			super.optionName = optionName;
			super.description = description;
		}

		protected void put() {
			if (field != null)
				try {
					field.commitEdit();
					Object value = field.getValue();
					if (value instanceof Number)
						Options.getInstance().putInteger(
							optionName, new Integer(((Number)value).intValue()));
				}
				catch (ParseException pe) {
					pe.printStackTrace();
				}
		}

		protected JComponent createPanel() {
			field = new JFormattedTextField(
					Options.getInstance().getInteger(optionName, new Integer(0)));
			field.setPreferredSize(TF_SIZE);
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(new JLabel(description));
			panel.add(field);
			return panel;
		}
	}

	protected class DoubleOption extends Option {
	  
		protected JFormattedTextField field;
		
		protected DoubleOption(String optionName, String description) {
			super.optionName = optionName;
			super.description = description;
		}

		protected void put() {
			if (field != null)
				try {
					field.commitEdit();
					Object value = field.getValue();
					if (value instanceof Number)
						Options.getInstance().putDouble(
							optionName, new Double(((Number)value).doubleValue()));
				}
				catch (ParseException pe) {
					pe.printStackTrace();
				}
		}

		protected JComponent createPanel() {
			field = new JFormattedTextField(
					Options.getInstance().getDouble(optionName, new Double(0.0)));
			field.setPreferredSize(TF_SIZE);
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(new JLabel(description));
			panel.add(field);
			return panel;
		}
	}

	protected Dimension TF_SIZE = new Dimension(50, 20);
	protected ArrayList optionList = new ArrayList();
	
	public OptionDialog(JFrame frame) {
		super(frame, I18N.getString("OptionDialog.Title"));
		setSize(new Dimension(300, 300));
	}

	public void addBooleanOption(String optionName, String description) {
		optionList.add(new BooleanOption(optionName, description));
	}

	public void addIntegerOption(String optionName, String description) {
		optionList.add(new IntOption(optionName, description));
	}

	public void addDoubleOption(String optionName, String description) {
		optionList.add(new DoubleOption(optionName, description));
	}

	public void showDialog() {
		JPanel panel = new JPanel(new GridLayout(optionList.size(), 1));
		for(Iterator iter = optionList.iterator(); iter.hasNext(); )
			panel.add(((Option)iter.next()).createPanel());

		JScrollPane pane = new JScrollPane(panel);
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel();

		JButton okButton = new JButton(I18N.getString("PropertiesDialog.Ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				OptionDialog.this.put();
				OptionDialog.this.dispose();
			}
		});
		
		JButton cancelButton = 
			new JButton(I18N.getString("PropertiesDialog.Cancel"));
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				OptionDialog.this.dispose();
			}
		});
		
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	protected void put() {
		for (Iterator iter = optionList.iterator(); iter.hasNext(); ) {
			((Option) iter.next()).put();
		}
	}
}
