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
package de.intevation.printlayout.ui;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dialog;

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
import javax.swing.JScrollPane;

import de.intevation.printlayout.PrintLayoutPlugin;

import de.intevation.printlayout.util.Template;

/**
 * JDialog listing the basic user mouse interactions.
 */
public class InfoDialog extends JDialog {

	/**
	 * creates a dialog for a given resource.
	 * @param owner owner dialog
	 * @param resource path to HTML resource file.
	 */
	public InfoDialog(Dialog owner, String resource) {
		this(owner, resource, null);
	}

	
	/**
	 * creates a dialog for a given resource.
	 * @param owner owner dialog
	 * @param resource path to HTML resource file.
	 * @param tmpl a template replacer that replaces variables in the HTML.
	 *        null permitted.
	 */
	public InfoDialog(Dialog owner, String resource, Template tmpl) {
		super(owner);
		initAndShow(resource, tmpl);
	}

	/**
	 * creates a dialog for a given resource.
	 * @param owner owner frame
	 * @param resource path to HTML resource file.
	 */
	public InfoDialog(JFrame owner, String resource) {
		this(owner, resource, null);
	}

	
	/**
	 * creates a dialog for a given resource.
	 * @param owner owner frame
	 * @param resource path to HTML resource file.
	 * @param tmpl a template replacer that replaces variables in the HTML.
	 *        null permitted.
	 */
	public InfoDialog(JFrame owner, String resource, Template tmpl) {
		super(owner);
		initAndShow(resource, tmpl);
	}

	/**
	 * Loads the HTML resource and displays it.
	 * @param resource the resource to load
	 * @param tmpl the optional template replacer
	 */
	protected void initAndShow(String resource, Template tmpl) {

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

		setTitle(PrintLayoutPlugin.getNameAndVersion());
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

		if (tmpl != null)
			text = tmpl.toString(text);

		JEditorPane infoText = new JEditorPane("text/html", text);
		infoText.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(infoText);

		add(scrollPane, BorderLayout.CENTER);
		setSize(400, 500);
    //infoText.scrollToReference("#");
    //scrollPane.scrollRectToVisible(new java.awt.Rectangle(0, 0, 400, 300));
		
	} 

	/**
	 * static helper to create and show a dialog for a
	 * given HTML resource.
	 * @param owner owner dialog
	 * @param resource path to HTML resource file.
	 */
	public static void showDialog(Dialog owner, String resource) {
		InfoDialog dialog = new InfoDialog(owner, resource);
		dialog.setVisible(true);
	}

	/**
	 * static helper to create and show a dialog for a
	 * given HTML resource.
	 * @param owner owner dialog
	 * @param resource path to HTML resource file.
	 * @param tmpl a template replacer for the HTML
	 */
	public static void showDialog(Dialog owner, String resource, Template tmpl) {
		InfoDialog dialog = new InfoDialog(owner, resource, tmpl);
		dialog.setVisible(true);
	}

	/**
	 * static helper to create and show a dialog for a
	 * given HTML resource.
	 * @param owner owner frame
	 * @param resource path to HTML resource file.
	 */
	public static void showDialog(JFrame owner, String resource) {
		InfoDialog dialog = new InfoDialog(owner, resource);
		dialog.setVisible(true);
	}

	/**
	 * static helper to create and show a dialog for a
	 * given HTML resource.
	 * @param owner owner frame
	 * @param resource path to HTML resource file.
	 * @param tmpl a template replacer for the HTML
	 */
	public static void showDialog(JFrame owner, String resource, Template tmpl) {
		InfoDialog dialog = new InfoDialog(owner, resource, tmpl);
		dialog.setVisible(true);
	}

	/**
	 * looks for the last occurence of '.' char in a given
	 * string and inserts a two letter language code prefixed
	 * by an '_'  before its location.
	 * e.g. index.html becomes index_de.html if you are running
	 * a german locale.
	 * @param str string to look for '.' occurence.
	 * @return the modified string. If no '.' was found the
	 *         orginal string is returned.
	 */
	private static String getLocalizedResource(String str) {
		Locale locale = Locale.getDefault();
		int index = str.lastIndexOf('.');
		return index > -1
			? str.substring(0, index) + "_"+ locale.getLanguage()
			  + str.substring(index)
			: str;
	}
}
