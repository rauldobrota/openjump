/*
 * TextInteractor.java
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

import java.awt.Font;
import java.awt.Color;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.apache.batik.swing.gvt.InteractorAdapter;

import de.intevation.printlayout.DocumentManager;

public class TextInteractor 
extends InteractorAdapter 
implements Tool	
{
	public static final String IDENTIFIER = "text-tool";

	protected boolean inUse = false;
	protected boolean finished = true;
	
	protected DocumentManager docManager;
  protected Consumer consumer; 

	public interface Consumer {
		DocumentManager.DocumentModifier createNewText(String text,
				AffineTransform xform,
				Color color,
				Font font);
	}

	public void setConsumer(Consumer con) {
		consumer = con;
	}

	public void setDocumentManager(DocumentManager docManager) {
		this.docManager = docManager;
	}

	public DocumentManager getDocumentManager() {
		return docManager;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}
	
	public boolean getInUse() {
		return inUse;
	}
	
	public String getToolIdentifier() {
		return IDENTIFIER;
	}

	public boolean startInteraction(InputEvent ie) {
		if (!inUse) return false;

		boolean start = ie instanceof MouseEvent
			&& (ie.getModifiers() & InputEvent.BUTTON1_MASK)
				== InputEvent.BUTTON1_MASK;

		if(start)
			finished = false;
		
		return start;	
	}

	public boolean endInteraction() {
		return finished;
	}

	public void mouseClicked(MouseEvent e) {
		final TextDialog dialog = new TextDialog(null);
		dialog.setVisible(true);	

		if(dialog.isAccepted() && dialog.getChoosenText().trim().length() > 0) {
			AffineTransform xform  = null;
			try {
				xform = BoxInteractor.getScreenXForm(
						docManager.getSVGDocument()).createInverse();
			}
			catch(NoninvertibleTransformException nite) {
				nite.printStackTrace();
				return;
			}
			
			Point2D pt = new Point2D.Double();
			xform.transform(new Point2D.Double(e.getX(), e.getY()), pt);

			xform = AffineTransform.getTranslateInstance(pt.getX(), pt.getY());
			docManager.modifyDocumentLater(consumer.createNewText(
					dialog.getChoosenText(),
					xform,
					dialog.getChoosenColor(),
					dialog.getChoosenFont()));
		
		}
		finished = true;
	}
	
}
