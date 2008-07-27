/* 
* Kosmo - Sistema Abierto de Información Geográfica
* Kosmo - Open Geographical Information System
*
* http://www.saig.es
* (C) 2006, SAIG S.L.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation;
* version 2.1 of the License.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*
* For more information, contact:
* 
* Sistemas Abiertos de Información Geográfica, S.L.
* Avnda. República Argentina, 28
* Edificio Domocenter Planta 2ª Oficina 7
* C.P.: 41930 - Bormujos (Sevilla)
* España / Spain
*
* Teléfono / Phone Number
* +34 954 788876
* 
* Correo electrónico / Email
* info@saig.es
*
*/

package org.openjump.tin.ui;
//package org.saig.jump.widgets.util;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.openjump.tin.ui.FormUtils;
import org.openjump.tin.i18n.I18NPlug;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

/**
* 
* <p>
*
* </p>
* @author Administrador
* @since 1.0.0
*/
public class SelectFilePanel extends JPanel {

    private JTextField pathTextField = null;
    private JButton selecFileButton = null;
    private JFileChooser fileChooser = null;
    private FileFilter fileFilter;
    
    // Indica si se quiere abrir un fichero existente o guardarlo (por defecto, abrir)
    // Indicates if it is wanted abrir an existing file or to keep it (by defect, abrir) 
    private boolean open = true;
    
    private ArrayList actionListeners = new ArrayList();
    
    private static String name = "selectfilepanel";
    private String noFile = "The selected file does not exist";
    private String badRead = "The selected file can not be read";
    private String badWrite = "The selected file can not be written";
    private String select = "select";
    private String save = "save";
    
    // Comprobaciones que se debe cumplir si es de apertura
    // Verifications that are due to fulfill if it is of opening 
    private EnableCheck[] openEnableChecks =
        new EnableCheck[] {
            new EnableCheck() { public String check(JComponent component) {
                return !new File(getSelectedPath()).exists() ? noFile : null;
            }
        }, new EnableCheck() {
            public String check(JComponent component) {
                return !new File(getSelectedPath()).canRead() ? badRead : null;
            }
        }
    };
    
    //  Comprobaciones que se debe cumplir si es de escritura
    // Verifications that are due to fulfill if it is of writing
    private EnableCheck[] saveEnableChecks =
        new EnableCheck[] {
            new EnableCheck() { public String check(JComponent component) {
                File file = new File(getSelectedPath().trim());
                return file.exists() && !file.canWrite() ? badWrite : null;
            }
        }
    };
    private boolean okPressed;
    
    private EnableCheck[] enableChecks;
    

    /**
     * This is the default constructor
     */
    public SelectFilePanel() {
        super();
        initialize();
    }    
    
    public SelectFilePanel(String description, String[] extensions){
        this(description, extensions, true);
    }
    
    public SelectFilePanel(String description, String[] extensions, boolean open){
        super();
        this.open = open;
        
        if(open)
        {
            enableChecks = openEnableChecks;
        }
        else
        {
            enableChecks = saveEnableChecks;
        }
        initialize();
        fileFilter = GUIUtil.createFileFilter(description, extensions);
        fileChooser.setFileFilter(fileFilter);
    }
    

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
    	// set localization strings
        I18NPlug.setPlugInRessource(name, "org.openjump.tin.i18n.resources.selectfilepanel");
		if (I18NPlug.jumpi18n == true) {
			this.noFile = I18NPlug.get(name, "SelectFilePanel.NoFile");
			this.badRead = I18NPlug.get(name, "SelectFilePanel.BadRead");
			this.badWrite = I18NPlug.get(name, "SelectFilePanel.BadWrite");
			this.select = I18NPlug.get(name, "SelectFilePanel.Select");
			this.save = I18NPlug.get(name, "SelectFilePanel.Save");
		}
    	
        this.setLayout(new GridBagLayout());
        
        if(open)
        {
            fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
        }
        else
        {
            fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
        }
        
        // Solo permitimos seleccionar archivos
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Anyadimos los componentes
        FormUtils.addRowInGBL(this, 0, 0, getPathTextField(), true, false, true);
        FormUtils.addRowInGBL(this, 0, 1, getSelecFileButton(), false, false, true);        
        
    }

    /**
     * This method initializes pathTextField    
     *     
     * @return javax.swing.JTextField    
     */
    private JTextField getPathTextField() {
        if (pathTextField == null) {
            pathTextField = new JTextField();
        }
        return pathTextField;
    }

    /**
     * This method initializes selectDirectoryButton    
     *     
     * @return javax.swing.JButton    
     */
    private JButton getSelecFileButton() {
        if (selecFileButton == null) {
            selecFileButton = new JButton();
            selecFileButton.setIcon(GUIUtil.resize((ImageIcon)IconLoader.icon("Open.gif"),20)); //$NON-NLS-1$
            selecFileButton.setMinimumSize(new java.awt.Dimension(32, 32));
            selecFileButton.setPreferredSize(new java.awt.Dimension(32, 32));
            selecFileButton.addActionListener(new ActionListener(){

                public void actionPerformed( ActionEvent e ) {
                    fileChooser.setSelectedFile(new File(pathTextField.getText().trim()));
                    String message = open ? select : save; //$NON-NLS-1$ //$NON-NLS-2$
                    int returnVal = fileChooser.showDialog(SelectFilePanel.this, message);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        pathTextField.setText(file.getPath());
                        okButton_actionPerformed(e);
                    }
                    else
                    {
                        cancelButton_actionPerformed(e);
                    }
                }
            });
        }
        return selecFileButton;
    }

    /**
     * Recupera el path seleccionado por el usuario
     * It recovers path selected by the user 
     *
     * @return
     */
    public String getSelectedPath()
    {
        return getPathTextField().getText().trim();
    }
    
    /**
     * Establece el path seleccionado por el usuario
     * It establishes path selected by the user 
     *
     * @param path Ruta seleccionada por el usuario
     *             Route selected by the user 
     */
    public void setSelectedPath( String path ) {
        this.getPathTextField().setText(path);
    }
    

    public EnableCheck[] getEnableChecks() {
        return enableChecks;
    }
    
    public boolean wasOKPressed() {
        return okPressed;
    }

    public void setOKPressed(boolean okPressed) {
        this.okPressed = okPressed;
    }

    void okButton_actionPerformed(ActionEvent e) {
        okPressed = true;
        fireActionPerformed();
    }

    void cancelButton_actionPerformed(ActionEvent e) {
        okPressed = false;
        fireActionPerformed();
    }

    public void addActionListener(ActionListener l) {
        this.actionListeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
        this.actionListeners.remove(l);
    }

    private void fireActionPerformed() {
        for (Iterator i = actionListeners.iterator(); i.hasNext();) {
            ActionListener l = (ActionListener) i.next();
            l.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

    /**
     * Comprueba si se cumplen los chequeos establecidos
     * It verifies if the established controls are fulfilled 
     *
     * @return
     */
    public String firstErrorMessage()
    {
        String message = null;
                
        for( int i = 0; i < enableChecks.length; i++ ) {
            message = enableChecks[i].check(null);
            
            if(message != null)
            {
                return message;
            }
        }
        
        return message;
    }
    
    /**
     * Comprueba si la seleccion es correcta
     * It verifies if the selection is correct
     *
     * @return
     */
    public boolean isInputValid()
    {
        return firstErrorMessage() == null;
    }

    public void addChangePathListener(DocumentListener dl ) {
        pathTextField.getDocument().addDocumentListener(dl);
    }

}