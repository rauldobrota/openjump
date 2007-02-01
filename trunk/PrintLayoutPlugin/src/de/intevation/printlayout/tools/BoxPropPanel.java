/*
 * BoxPropPanel.java
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import javax.swing.border.TitledBorder;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.intevation.printlayout.I18N;

public class BoxPropPanel extends JPanel {
	private static String[] linesPattern = new String[] {
			"0", "1", "3", "5", "5,1", "7", "7,12", "9", "9,2", "15,6", "20,3"};

	private ColorButton strokeColorBtn  = new ColorButton();
	private ColorButton fillColorBtn    = new ColorButton();
	private JCheckBox   fillColorChB    = new JCheckBox();
	private JComboBox   linesPatternCB  = new JComboBox(linesPattern) {
		{
			setPreferredSize(new Dimension(120, 20));
			setRenderer(new ListCellRenderer() {
				private JPanel panel = new JPanel() {
					protected void paintComponent(Graphics g) {
						super.paintComponent(g);

						Graphics2D g2 = (Graphics2D) g;
						g2.setStroke(getStroke(linePattern));
						g2.draw(new Line2D.Double(0,
								panel.getHeight() / 2.0,
								panel.getWidth(),
								panel.getHeight() / 2.0));
					}
				};

				private String linePattern;

				public Component getListCellRendererComponent(
					JList list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
					linePattern = (String) value;
					panel.setForeground(UIManager.getColor(isSelected
							? "ComboBox.selectionForeground"
							: "ComboBox.foreground"));
					panel.setBackground(UIManager.getColor(isSelected
							? "ComboBox.selectionBackground"
							: "ComboBox.background"));

					return panel;
				}
			});
		}
	};

		
	private JSlider     lineWidthSlider = new JSlider();

	public BoxPropPanel() {
		createComponents();
	}

	protected void createComponents() {
		configureLineWidthSlider();
		configureFillColorCheckBox();

		setLayout(new BorderLayout());
		add(createStrokeColorPanel(), BorderLayout.NORTH);
		add(createStrokePanel(), BorderLayout.CENTER);
		add(createFillColorPanel(), BorderLayout.SOUTH);
	}

	protected void configureLineWidthSlider() {
		lineWidthSlider.setPreferredSize(new Dimension(130, 49));
		lineWidthSlider.setPaintLabels(true);
		lineWidthSlider.setValue(1);
		lineWidthSlider.setLabelTable(lineWidthSlider.createStandardLabels(5));
		lineWidthSlider.setMajorTickSpacing(5);
		lineWidthSlider.setMaximum(10);
		lineWidthSlider.setMinorTickSpacing(1);

		lineWidthSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				linesPatternCB.repaint();
			}
		});
	}

	protected void configureFillColorCheckBox() {
		fillColorChB.setText(
				I18N.getString("BoxPropPanel.fillPatternCheckBox", "use fill color"));
	}

	protected JPanel createStrokeColorPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(
					I18N.getString("BoxPropPanel.StrokeColorPanel", "stroke color")));
	
		panel.add(strokeColorBtn);
		return panel;
	}
	
	protected JPanel createStrokePanel() {
		JPanel panel = new JPanel(new GridLayout(2, 1));
		panel.setBorder(new TitledBorder(
					I18N.getString("BoxPropPanel.StrokePanel", "stroke")));

		panel.add(lineWidthSlider);
		JPanel patternPanel = new JPanel();
		patternPanel.add(linesPatternCB);
		panel.add(patternPanel);
		return panel;
	}

	protected JPanel createFillColorPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 1));
		panel.setBorder(new TitledBorder(
					I18N.getString("BoxPropPanel.FillColorPanel", "fill color")));

		panel.add(fillColorChB);

		JPanel btnPanel = new JPanel();
		btnPanel.add(fillColorBtn);
		panel.add(btnPanel);

		return panel;
	}

	public Color getStrokeColor() {
		return strokeColorBtn.getColor();
	}

	public Color getFillColor() {
		return fillColorChB.isSelected() 
			? fillColorBtn.getColor()
			: null;
	}

	public Stroke getStroke() {
		return getStroke(null);
	}

	protected Stroke getStroke(String value) {
		float[] linePattern = dashArray(value);	
	  float   lineWidth   = lineWidthSlider.getValue();
		
		Stroke stroke = linePattern != null 
			&& linePattern.length != 0 && lineWidth > 0
			? new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL, 1.0f, linePattern,
					0)
			: new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL);
		
		return stroke;
	}

	protected float[] dashArray(String value) {
		if(value == null)
			value = (String)linesPatternCB.getSelectedItem();
		
		StringTokenizer tokens 
			= new StringTokenizer(value, ",");
		
		float width = lineWidthSlider.getValue();
		
		float[] result = new float[tokens.countTokens()];
		int i = 0;
		while(tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			try {
				result[i] = Float.valueOf(token) * width;
			}
			catch(NumberFormatException nfe) {
				System.err.println("cannot handle token:" + token);
				result[i] = 0.0f;
			}
			i++;
		}
		if (result.length == 1 && result[0] == 0)
			return null;
		
		return result;
	}

	public DrawingAttributes getDrawingAttributes() {
		DrawingAttributes attributes = new DrawingAttributes();

		attributes.setStrokeColor(getStrokeColor());
		attributes.setStroke(getStroke());
		attributes.setFillColor(getFillColor());

		return attributes;
	}
}
