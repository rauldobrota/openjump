/*
 * BoxProperiesDialog.java
 * ----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFrame;

import de.intevation.printlayout.I18N;

public class BoxPropertiesDialog
extends JDialog
{
	BoxPropPanel panel = new BoxPropPanel();
	boolean accepted = false;

	public BoxPropertiesDialog(JFrame owner, DrawingAttributes attributes) {
		super(owner);
		setTitle(I18N.getString("BoxPropertiesDialog.Title", "box properties"));
		
		panel.setDrawingAttributes(attributes);
		createComponents();
	}

	public static DrawingAttributes showDialog(JFrame owner,
			DrawingAttributes attributes
	) {
		BoxPropertiesDialog dialog = new BoxPropertiesDialog(owner, attributes);
		dialog.setModal(true);

		dialog.setVisible(true);
		return dialog.getDrawingAttributes();
	}

	
	protected void createComponents() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(panel, BorderLayout.CENTER);
		mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

		setContentPane(mainPanel);
		pack();
	}

	protected JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();

		JButton okBtn = new JButton(I18N.getString("Ok", "Ok"));
		JButton cancelBtn = new JButton(I18N.getString("Cancel", "Cancel"));

		buttonPanel.add(okBtn);
		buttonPanel.add(cancelBtn);

		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				accepted = true;
				setVisible(false);
			}
		});

		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				accepted = false;
				setVisible(false);
			}
		});

		return buttonPanel;
	}
	
	public DrawingAttributes getDrawingAttributes() {
		return accepted 
			? panel.getDrawingAttributes()
			: null;
	}

	public boolean isAccepted() {
		return accepted;
	}
}
