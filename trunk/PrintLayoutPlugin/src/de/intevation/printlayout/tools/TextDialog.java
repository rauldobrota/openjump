/*
 * TextDialog.java
 * ---------------
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
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.GraphicsEnvironment;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import javax.swing.border.TitledBorder;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import de.intevation.printlayout.I18N;

public class TextDialog 
extends JDialog
implements ColorButton.ColorChangedListener,
	ListSelectionListener, ItemListener
{
	private static int[] pointSizes = { 3, 5, 8, 10, 12, 14, 18, 24, 36, 48 };
	private static StyleComboElement[] styles = {
		new StyleComboElement(Font.PLAIN, "plain"),
		new StyleComboElement(Font.BOLD, "bold"),
		new StyleComboElement(Font.BOLD + Font.ITALIC, "bolditalic"),
		new StyleComboElement(Font.ITALIC, "italic") };
		

	protected static class StyleComboElement {
		private int style = 0;
		private String name;
		
		protected StyleComboElement(int style, String name) {
			this.style = style;
			this.name = name;
		}
		
		protected int getStyle() {
			return style;
		}

		protected String getName() {
			return name;
		}

		protected String getNameForFontDecode() {
			if (style == Font.PLAIN)
				return "plain";
			else if (style == Font.BOLD)
				return "bold";
			else if (style == Font.BOLD + Font.ITALIC) 
				return "bolditalic";
			else if (style == Font.ITALIC)
				return "italic";
			
			return "plain";
		}
		
		public String toString() {
			return getName();
		}
	}
	
	protected JTextArea textArea = new JTextArea();
	protected ColorButton colorBtn = new ColorButton();
	protected JList familyL = new JList();
	protected JComboBox sizeCB = new JComboBox();
	protected JList styleL = new JList(styles);
	
	protected boolean accepted = false;

	public TextDialog(JFrame owner) {
		this(owner, "", null, Color.BLACK);	
	}

	public TextDialog(JFrame owner, String text, Font font, Color color) {
		super(owner, I18N.getString("TextDialog.Title", "text properties"), true);
		fillFamilyL();
		fillsizeCB();
	
		if(font != null)
			textArea.setFont(font);
		textArea.setText(text);

		colorBtn.setColor(color);
		textArea.setForeground(color);
		if (font != null) {
			setFontName(font.getFamily());
			setSize(font.getSize());
			setStyle(font.getStyle());
		}
		
		distributeListeners();	
		createGUI();
		pack();

	}

	protected void setFontName(String fontname) {
		familyL.setSelectedValue(fontname, true);
	}

	protected void setSize(int size) {
		for (int i = 0, N = sizeCB.getItemCount(); i < N; i++) {
			if (size == ((Integer) sizeCB.getItemAt(i)).intValue())
				sizeCB.setSelectedIndex(i);
		}
	}

	protected void setStyle(int style) {
		for (int i = styles.length -1 ; i >= 0; i--) {
			if (style == styles[i].getStyle()) {
				styleL.setSelectedIndex(i);
			} 
		}
	}
	
	public void colorChanged(ColorButton.ColorChangedEvent dummy) {
		textArea.setForeground(getChoosenColor());
	}
	
	public void valueChanged(ListSelectionEvent lse) {
		textArea.setFont(getChoosenFont());
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED)
			textArea.setFont(getChoosenFont());
	}

	protected void distributeListeners() {
		colorBtn.addColorChangedListener(this);
		familyL.addListSelectionListener(this);
		styleL.addListSelectionListener(this);
		sizeCB.addItemListener(this);
	}
	
	protected void createGUI() {
		textArea.setRows(10);
		textArea.setBorder(new TitledBorder(
					I18N.getString("TextDialog.Text", "text")));
		familyL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		styleL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		styleL.setVisibleRowCount(4);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(createPropertiesPanel(), BorderLayout.NORTH);
		panel.add(textArea, BorderLayout.CENTER);
		panel.add(createControlButtonsPanel(), BorderLayout.SOUTH);
			
		setContentPane(panel);
	}
	
	protected JPanel createPropertiesPanel() {
		JPanel panel = new JPanel(new GridLayout(1,2));

		panel.add(createTitledPanel(
					I18N.getString("TextDialog.FontFamily", "font family"),
					new JScrollPane(familyL)));
		panel.add(createStyleSizeColorPanel());
		
		return panel;
	}
	
	protected void fillFamilyL() {
		String[] fonts = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		familyL.setListData(fonts);
	}

	protected void fillsizeCB() {
		for (int i = 0; i < pointSizes.length; i++) {
			sizeCB.addItem(new Integer(pointSizes[i]));
		}
	}

	protected JPanel createStyleSizeColorPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel stylePanel = createTitledPanel(
				I18N.getString("TextDialog.Style", "style"), new JScrollPane(styleL));

		JPanel sizeColorPanel = new JPanel(new GridLayout(2,1));
		sizeColorPanel.add(createTitledPanel(
					I18N.getString("TextDialog.Size", "size"), sizeCB));
		sizeColorPanel.add(createTitledPanel(
					I18N.getString("TextDialog.Color", "color"), colorBtn));

		panel.add(stylePanel, BorderLayout.CENTER);
		panel.add(sizeColorPanel, BorderLayout.SOUTH);

		return panel;
	}
	
	/*protected JPanel createSmallPanel(String title, JComponent component) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(title));
		panel.add(component);
		panel.setBorder(new TitledBorder(""));

		return panel;
	}*/

	protected JPanel createTitledPanel(String title, JComponent component) {
		JPanel panel = new JPanel();
		panel.add(component);
		panel.setBorder(new TitledBorder(title));
		
		return panel;
	}
	
	protected JPanel createControlButtonsPanel() {
		JButton okBtn = new JButton(I18N.getString("PropertiesDialog.Ok", "Ok"));
		JButton cancelBtn = new JButton(
				I18N.getString("PropertiesDialog.Cancel", "Cancel"));
		
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setAccepted(true);
				getDialog().setVisible(false);
			}
		});

		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getDialog().setVisible(false);
			}
		});
		
		JPanel panel = new JPanel();
		panel.add(okBtn);
		panel.add(cancelBtn);
		
		return panel;
	}
	
	protected void setAccepted(boolean b) {
		accepted = b;
	}
	
	public boolean isAccepted() {
		return accepted;
	}
	
	protected JDialog getDialog() {
		return this;
	}

	public String getChoosenText() {
		String text = textArea.getText();
		//text = text.replaceAll("\t", "    ");
		//text = text.replaceAll("\n", "");
		//text = text.replaceAll(" ", "&nbsp;");
		return text;
	}

	public Font getChoosenFont() {
		if(styleL.isSelectionEmpty() || familyL.isSelectionEmpty())
			return getFont();
		
		int indexOfStyle = styleL.getSelectedIndex();
		Font font =Font.decode("" + familyL.getSelectedValue() 
				+ "-" + styles[indexOfStyle].getNameForFontDecode()
				+ "-" + sizeCB.getSelectedItem());
		return font;
	}

	public Color getChoosenColor() {
		return colorBtn.getColor();
	}
}
