/*
 * InfoDialog.java
 * ---------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JEditorPane;

/**
 * JDialog listing the basic user mouse interactions.
 */
public class InfoDialog extends JDialog {

	public static String resource = "resources/info.html";
	
	public InfoDialog(JFrame owner) {
		this(owner, resource);
	}
	
	public InfoDialog(JFrame owner, String resource) {
		super(owner);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBackground(Color.WHITE);
		setLayout(new BorderLayout());
		
		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton dismissBtn = new JButton("OK");

		dismissBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				InfoDialog.this.dispose();
			}
		});
		south.add(dismissBtn);
		add(south, BorderLayout.SOUTH);

		setTitle("Print/Layout v0.9.0");
		InputStream is = InfoDialog.class.getResourceAsStream(
				getLocalizedResource(resource));
		if (is == null) {
			is = InfoDialog.class.getResourceAsStream(resource);
		
		}
		
    String text;
		
		if (is == null)
			text = "<html><body>Resource not found</body></html>";
		else {
			try {
				byte [] buf = new byte[512];
				int r;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((r = is.read(buf)) > 0)
					out.write(buf, 0, r);
				text = out.toString("ISO-8859-1");
			}
			catch (IOException ioe) {
				text = "<html><body>IO error</body></html>";
			}
			finally {
				try { is.close(); } catch (IOException ioe) {}
			}
		}

		JEditorPane infoText = new JEditorPane("text/html", text);
		infoText.setEditable(false);

		add(infoText, BorderLayout.CENTER);
		setSize(400, 500);
		
	} 

	public static void showDialog(JFrame owner) {
		InfoDialog dialog = new InfoDialog(owner);
		dialog.setVisible(true);
	
	}

	public static void showDialog(JFrame owner, String resource) {
		InfoDialog dialog = new InfoDialog(owner, resource);
		dialog.setVisible(true);
	}

	private static String getLocalizedResource(String str) {
		Locale locale = Locale.getDefault();
		if (str.lastIndexOf('.') > -1)
			return str.substring(0, str.lastIndexOf('.')) + "_"+ locale.getLanguage()
					+ str.substring(str.lastIndexOf('.'));
		return str;
	}
}
