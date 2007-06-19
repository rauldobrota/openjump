/*
 * ImageParameterDialog.java
 * -------------------------
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
import javax.swing.JFormattedTextField;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JButton;

import javax.swing.text.NumberFormatter;

import javax.swing.border.TitledBorder;

import java.text.NumberFormat;
import java.text.ParseException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.apache.batik.transcoder.Transcoder;

import de.intevation.printlayout.I18N;

/**
 * Simple abstract JDialog used to ask the user for the 
 * size the an exported Image. Subclass it to add more
 * parameters.
 */
public abstract class ImageParameterDialog
extends               JDialog
{
	/**
	 * initial width when opening the dialog.
	 */
	public static final int INITIAL_WIDTH = 640;

	/**
	 * TextField containing the current width.
	 */
	protected JFormattedTextField widthTF;

	/**
	 * TextField containing the current height.
	 */
	protected JFormattedTextField heightTF;

	/**
	 * should the aspect ratio of the image dimension should be kept?
	 */
	protected JCheckBox           keepAspect;

	/**
	 * used to indicate if the dialog was canceled.
	 */
	protected boolean             transcode;

	/**
	 * scale to calculate height from width when
	 * keeping aspect ratio.
	 */
	protected double              scale = 1d;

	/**
	 * construct a new parameter dialog.
	 * @param owner owner frame of this dialog
	 */
	public ImageParameterDialog(Frame owner) {
		super(owner);
		init();
	}

	/**
	 * called from constructor. initializes the dialog.
	 * override if you want some extra initialization.
	 * but in this case call super.init().
	 */
	protected void init() {
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

	/**
	 * called from init() to create the widgets.
	 */
	protected JPanel createComponents() {

		JPanel inner = new JPanel(new BorderLayout());
		String sizeS = I18N.getString(
			"ImageParameterDialog.Size", "size:");
		inner.setBorder(new TitledBorder(sizeS));

		JPanel dimC = new JPanel(new FlowLayout(FlowLayout.CENTER));

		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setMinimum(new Integer(1));

		String widthHeight = I18N.getString(
			"ImageParameterDialog.WidthHeight", "width x height:");

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

		widthTF .addPropertyChangeListener(cl);
		heightTF.addPropertyChangeListener(cl);

		dimC.add(heightTF);

		inner.add(dimC, BorderLayout.CENTER);

		JPanel dimS = new JPanel(new FlowLayout(FlowLayout.CENTER));

		String keepAspectS = I18N.getString(
			"ImageParameterDialog.KeepAspect", "keep aspect ratio");

		keepAspect = new JCheckBox(keepAspectS, true);
		dimS.add(keepAspect);

		inner.add(dimS, BorderLayout.SOUTH);

		JPanel middle = new JPanel(new BorderLayout());
		middle.add(inner, BorderLayout.CENTER);

		JComponent extraComponents = createExtraComponents();

		if (extraComponents != null)
			middle.add(extraComponents, BorderLayout.SOUTH);

		JPanel outer = new JPanel(new BorderLayout());
		outer.add(middle, BorderLayout.CENTER);

		JPanel ok = new JPanel(new FlowLayout(FlowLayout.CENTER));

		String okayS = I18N.getString(
    	"ImageParameterDialog.Save", "save");

		JButton okay   = new JButton(okayS);

		String cancelS = I18N.getString(
    	"ImageParameterDialog.Cancel", "cancel");

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

	/**
	 * override this to create widgets that control
	 * extra encoding parameters.
	 */
	protected JComponent createExtraComponents() {
		return null;
	}

	/**
	 * called when dialog was canceled.
	 */
	protected void cancel() {
		transcode = false;
		setVisible(false);
	}

	/**
	 * called when dialog was aproved.
	 */
	protected void okay() {
		transcode = true;
		setVisible(false);
	}

	/**
	 * when keeping ascpect ratio this method calculates
	 * the height belonging to width accordingly the scale.
	 * @param width the width
	 * @return      the height
	 */
	protected int heightFromWidth(int width) {
		return Math.max(1, (int)Math.round(scale*width));
	}

	/**
	 * when keeping ascpect ratio this method calculates
	 * the width belonging to height accordingly the scale.
	 * @param height the height
	 * @return       the width
	 */
	protected int widthFromHeight(int height) {
		return Math.max(1, (int)Math.round(height/scale));
	}

	/**
	 * called on property change in width TextField.
	 * Used to adapt the height TextField.
	 * @param width the current width
	 * @return true if adapting height TextField. Else false.
	 */
	protected boolean widthChanged(Number width) {
		if (keepAspect.isSelected()) {
			int w = width.intValue();
			int h = heightFromWidth(w);
			heightTF.setValue(new Integer(h));
			return true;
		}
		return false;
	}

	/**
	 * called on property change in height TextField.
	 * Used to adapt the width TextField.
	 * @param height the current height
	 * @return true if adapting width TextField. Else false.
	 */
	protected boolean heightChanged(Number height) {
		if (keepAspect.isSelected()) {
			int h = height.intValue();
			int w = widthFromHeight(h);
			widthTF.setValue(new Integer(w));
			return true;
		}
		return false;
	}

	/**
	 * sets the width and height TextFields to
	 * width and heightFromWidth(width).
	 * @param width the width
	 */
	protected void setImageSize(int width) {
		int height = heightFromWidth(width);
		setImageSize(width, height);
	}

	/**
	 * sets the width and height TextFields to
	 * width and height.
	 * @param width  the width
	 * @param height the height
	 */
	protected void setImageSize(int width, int height) {
		widthTF .setValue(new Integer(width));
		heightTF.setValue(new Integer(height));
	}

	/**
	 * set the scale for keeping aspect ratio.
	 * @param scale the new scale
	 */
	protected void setScale(double scale) {
		this.scale = Math.abs(scale);
	}

	/**
	 * set the scale for keeping aspect ratio.
	 * @param size array with two elements containing the paper size.
	 */
	protected void setScale(double [] size) {
		if (size != null && size.length > 1)
			setScale(Math.abs(size[1])/Math.abs(size[0]));
	}

	/**
	 * override this to create a concrete instance of the transcoder
	 * that got initialized by this dialog.
	 */
	protected abstract Transcoder createTrancoder();

	/**
	 * returns the current size of the image.
	 * @param size two element array to store the size
	 */
	protected void getImageSize(int [] size) {
		if (size != null && size.length > 1) {
			size[0] = ((Number)widthTF.getValue()).intValue();
			size[1] = keepAspect.isSelected()
				? heightFromWidth(size[0])
				: ((Number)heightTF.getValue()).intValue();
		}
	}

	/**
	 * called from LayoutFrame to asked for the export parameters.
	 * @param size array with two elements containing the paper size.
	 * @return the initialized transcoder. null if canceled.
	 */
	public Transcoder getTranscoder(double [] size) {

		if (size == null || size.length < 2)
			return null;

		setScale(size);

		setImageSize(INITIAL_WIDTH);

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

		return createTrancoder();
	}
}
// end of file
