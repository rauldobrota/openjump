/**
 * @author Eric Lemesre eric.lemesre@gmail.com
 * Direction          : AGFFinanceConseil
 * Direction Régional : Paris Normandie Centre
 * 
 * 5 nov. 07
 */
package net.refractions.postgis;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;

/**
 * @author Eric Lemesre
 *
 */
public class AddPostGISLayerWizardPanel extends JPanel implements WizardPanel {
	private static final String KEY = AddPostGISLayerWizardPanel.class.getName();

	private static final String PKG_KEY = "net.refractions.postgis";
	private static final String TITLE = I18N.getText(PKG_KEY,KEY);
	private static final String INSTRUCTIONS = I18N.getText(PKG_KEY,KEY + ".instructions");

	private PostGISCommonDriverPanel postGISPanel;
	private HashMap properties;

	/**
	 * 
	 */
	public AddPostGISLayerWizardPanel() {
		super(new BorderLayout());
		postGISPanel = new PostGISCommonDriverPanel();
		add(postGISPanel,BorderLayout.CENTER);
	}

	/**
	 * @param layout
	 */
	public AddPostGISLayerWizardPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param isDoubleBuffered
	 */
	public AddPostGISLayerWizardPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public AddPostGISLayerWizardPanel(LayoutManager layout,
			boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public void add(InputChangedListener listener) {
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.wizard.WizardPanel#enteredFromLeft(java.util.Map)
	 */
	public void enteredFromLeft(Map arg0) {
		// TODO Populate field by connection information

	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.wizard.WizardPanel#exitingToRight()
	 */
	public void exitingToRight() throws Exception {
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.wizard.WizardPanel#getID()
	 */
	public String getID() {
		return KEY;
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.wizard.WizardPanel#getInstructions()
	 */
	public String getInstructions() {
		return INSTRUCTIONS;
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.wizard.WizardPanel#getNextID()
	 */
	public String getNextID() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.wizard.WizardPanel#getTitle()
	 */
	public String getTitle() {
		return TITLE;
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.wizard.WizardPanel#isInputValid()
	 */
	public boolean isInputValid() {
// add listener befor uncomment 
//	    if (postGISPanel.getServer().equals("")) return(false);
//	    if (postGISPanel.getPort().equals("")) return(false);
//	    if (postGISPanel.getDatabase().equals("")) return(false);
//	    if (postGISPanel.getTable().equals("")) return(false);
//	    if (postGISPanel.getUsername().equals("")) return(false);
	    return(true);
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.wizard.WizardPanel#remove(com.vividsolutions.jump.workbench.ui.InputChangedListener)
	 */
	public void remove(InputChangedListener arg0) {
	}

	/**
	 * @return the postGISPanel
	 */
	public PostGISCommonDriverPanel getPostGISPanel() {
		return postGISPanel;
	}

	public String getDatasetName() {
		return (String) postGISPanel.getTable();
	}
	
	protected HashMap getProperties() {
		if (properties == null) properties = new HashMap();
		properties.put(PostGISDataSource.SERVER_KEY, postGISPanel.getServer());
		properties.put(PostGISDataSource.PORT_KEY, postGISPanel.getPort());
		properties.put(PostGISDataSource.DATABASE_KEY, postGISPanel.getDatabase());
		properties.put(PostGISDataSource.TABLE_KEY, postGISPanel.getTable());
		if (!postGISPanel.getWhere().trim().equals("")) {
			properties.put(PostGISDataSource.WHERE_KEY, postGISPanel.getWhere());
		} else {
			properties.put(PostGISDataSource.WHERE_KEY, "true");
		}
		properties.put(PostGISDataSource.USERNAME_KEY, postGISPanel.getUsername());
		properties.put(PostGISDataSource.PASSWORD_KEY, postGISPanel.getPassword());
		return(properties);
	}
}
