/*
 * JPEGParameterDialog.java
 * ------------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */

package de.intevation.printlayout.ui;

import java.awt.Frame;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JSlider;
import javax.swing.JFormattedTextField;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

import javax.swing.text.NumberFormatter;

import javax.swing.border.TitledBorder;

import java.text.NumberFormat;
import java.text.ParseException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.apache.batik.transcoder.Transcoder;

import org.apache.batik.transcoder.image.JPEGTranscoder;

import de.intevation.printlayout.I18N;

/**
 * Simple JDialog used to ask the user for the size and
 * the quality of the exported JPEG.
 */
public class JPEGParameterDialog
extends      JDialog
{
	public static final int INITIAL_WIDTH   = 640;
	public static final int INITIAL_QUALITY =  85;

	protected JFormattedTextField widthTF;
	protected JFormattedTextField heightTF;

	protected JFormattedTextField qualityTF;

	protected JSlider             quality;

	protected JCheckBox           keepAspect;

	protected boolean             transcode;

	protected double              scale;

	public JPEGParameterDialog(Frame owner) {
		super(owner);
		setTitle(I18N.getString(
			"JPEGParameterDialog.Title", "JPEG export parameter"));
		
		setModal(true);

		setContentPane(createComponents());

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				cancel();
			}
		});

		pack();
	}

	protected JPanel createComponents() {

		JPanel inner = new JPanel(new BorderLayout());
		String sizeS = I18N.getString(
			"JPEGParameterDialog.Size", "size:");
		inner.setBorder(new TitledBorder(sizeS));

		JPanel dimC = new JPanel(new FlowLayout(FlowLayout.CENTER));

		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setMinimum(new Integer(1));

		String widthHeight = I18N.getString(
			"JPEGParameterDialog.WidthHeight", "width x height:");

		dimC.add(new JLabel(widthHeight, JLabel.RIGHT));

		widthTF = new JFormattedTextField(formatter);
		widthTF.setColumns(4);
		widthTF.setHorizontalAlignment(JFormattedTextField.TRAILING);

		dimC.add(widthTF);
		dimC.add(new JLabel(" x ", JLabel.CENTER));

		heightTF = new JFormattedTextField(formatter);
		heightTF.setColumns(4);
		heightTF.setHorizontalAlignment(JFormattedTextField.TRAILING);

		PropertyChangeListener cl = new PropertyChangeListener() {

			boolean changing; // protection against recursion

			public void propertyChange(PropertyChangeEvent e) {
				if (!"value".equals(e.getPropertyName()))
					return;
				Number value = (Number)e.getNewValue();
				if (value == null)
					return;
				if (changing = !changing) {
					boolean changed = e.getSource() == widthTF
						? widthChanged(value)
						: heightChanged(value);
					if (!changed)
						changing = false;
				}
			}
		};

		widthTF.addPropertyChangeListener(cl);

		heightTF.addPropertyChangeListener(cl);

		dimC.add(heightTF);

		inner.add(dimC, BorderLayout.CENTER);

		JPanel dimS = new JPanel(new FlowLayout(FlowLayout.CENTER));

		String keepAspectS = I18N.getString(
			"JPEGParameterDialog.KeepAspect", "keep aspect ratio");

		keepAspect = new JCheckBox(keepAspectS, true);
		dimS.add(keepAspect);

		inner.add(dimS, BorderLayout.SOUTH);

		JPanel middle = new JPanel(new BorderLayout());
		middle.add(inner, BorderLayout.CENTER);

		JPanel qual = new JPanel(new FlowLayout(FlowLayout.CENTER));

		String qualS = I18N.getString(
    	"JPEGParameterDialog.Quality", "quality:");

		qual.setBorder(new TitledBorder(qualS));

		quality = new JSlider(JSlider.HORIZONTAL, 0, 100, INITIAL_QUALITY);
		quality.setMajorTickSpacing(25);
		quality.setMinorTickSpacing(5);
		quality.setPaintTicks(true);
		quality.setPaintLabels(true);

		quality.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				JSlider slider = (JSlider)ce.getSource();
				Integer value = new Integer(slider.getValue());
				if (!slider.getValueIsAdjusting())
					qualityTF.setValue(value);
				else
        	qualityTF.setText(value.toString());
			}
		});

		qual.add(quality);

		NumberFormat qFormat = NumberFormat.getIntegerInstance();
		qFormat.setGroupingUsed(false);
		NumberFormatter qFormatter = new NumberFormatter(qFormat);
		qFormatter.setMinimum(new Integer(0));
		qFormatter.setMaximum(new Integer(100));

		qualityTF = new JFormattedTextField(qFormatter);
		qualityTF.setColumns(3);
		qualityTF.setHorizontalAlignment(JFormattedTextField.TRAILING);

		qualityTF.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if ("value".equals(e.getPropertyName())) {
					Number value = (Number)e.getNewValue();
					if (quality != null && value != null)
						quality.setValue(new Integer(value.intValue()));
				}
			}
		});
		qualityTF.setValue(new Integer(INITIAL_QUALITY));
		qual.add(qualityTF);

		middle.add(qual, BorderLayout.SOUTH);

		JPanel outer = new JPanel(new BorderLayout());
		outer.add(middle, BorderLayout.CENTER);

		JPanel ok = new JPanel(new FlowLayout(FlowLayout.CENTER));

		String okayS = I18N.getString(
    	"JPEGParameterDialog.Save", "save");

		JButton okay   = new JButton(okayS);

		String cancelS = I18N.getString(
    	"JPEGParameterDialog.Cancel", "cancel");

		JButton cancel = new JButton(cancelS);

		ok.add(okay);
		ok.add(cancel);

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) { cancel(); }
		});

		okay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) { okay(); }
		});

		outer.add(ok, BorderLayout.SOUTH);
		
		return outer;
	}

	protected void cancel() {
		transcode = false;
		setVisible(false);
	}

	protected void okay() {
		transcode = true;
		setVisible(false);
	}

	protected boolean widthChanged(Number width) {
		if (keepAspect.isSelected()) {
			int w = width.intValue();
			int h = Math.max(1, (int)Math.round(scale*w));
			heightTF.setValue(new Integer(h));
			return true;
		}
		return false;
	}

	protected boolean heightChanged(Number height) {
		if (keepAspect.isSelected()) {
			int h = height.intValue();
			int w = Math.max(1, (int)Math.ceil(h/scale));
			widthTF.setValue(new Integer(w));
			return true;
		}
		return false;
	}

	protected void setImageSize(int width, int height) {
		widthTF .setValue(new Integer(width));
		heightTF.setValue(new Integer(height));
	}

	/**
	 * called from LayoutFrame to asked for the export parameters.
	 * @param size array with two elements containing the paper size.
	 * @return the initialized transcoder. null if canceled.
	 */
	public Transcoder getTranscoder(double [] size) {

		if (size == null || size.length < 2)
			return null;

		scale = Math.abs(size[1])/Math.abs(size[0]);

		int h = Math.max(1, (int)Math.round(scale*INITIAL_WIDTH));

		setImageSize(INITIAL_WIDTH, h);

		transcode = false;
		setVisible(true);

		if (!transcode)
			return null;

		try {
			widthTF.commitEdit();
			heightTF.commitEdit();
		}
		catch (ParseException pe) {
		}

		Float W = new Float(((Number)widthTF.getValue()).floatValue());
		Float H = new Float(keepAspect.isSelected()
			?	Math.max(1f,
				Math.round(((Number)widthTF.getValue()).floatValue()*(float)scale))
			: ((Number)heightTF.getValue()).floatValue());

		Float Q = new Float(quality.getValue()/100d);

		JPEGTranscoder transcoder = new JPEGTranscoder();

		transcoder.addTranscodingHint(JPEGTranscoder.KEY_WIDTH,   W);
		transcoder.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT,  H);
		transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, Q);

		return transcoder;
	}
}
// end of file
