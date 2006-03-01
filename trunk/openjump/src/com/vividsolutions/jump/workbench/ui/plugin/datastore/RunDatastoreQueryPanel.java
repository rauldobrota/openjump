package com.vividsolutions.jump.workbench.ui.plugin.datastore;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datastore.ConnectionDescriptor;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelDialog;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;

public class RunDatastoreQueryPanel extends ConnectionPanel
{
    private JTextField maxFeaturesTextField;

    public RunDatastoreQueryPanel(WorkbenchContext context) {
        super(context);
        initialize();
    }

    private void initialize() {
        addRow("Query:", new JScrollPane(getQueryTextArea()) {
            {
                setPreferredSize(new Dimension(MAIN_COLUMN_WIDTH, 100));
            }
        }, null);
        addRow("Max Features:", getMaxFeaturesTextField(), null);
    }

    private JTextField getMaxFeaturesTextField() {
        if (maxFeaturesTextField == null) {
            maxFeaturesTextField = new ValidatingTextField("", 10,
                    new ValidatingTextField.BoundedIntValidator(1,
                            Integer.MAX_VALUE));
        }
        return maxFeaturesTextField;
    }

    private JTextArea getQueryTextArea() {
        if (queryTextArea == null) {
            queryTextArea = new JTextArea();
        }
        return queryTextArea;
    }

    private JTextArea queryTextArea;

/*
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        OKCancelDialog dialog = new OKCancelDialog((Frame) null,
                "Add Datastore Ad-Hoc Query Layer", true,
                new RunDatastoreQueryPanel(new WorkbenchContext() {
                    {
                        getRegistry().createEntry(
                                DataStoreDriver.REGISTRY_CLASSIFICATION,
                                new OracleDataStoreDriver());
                    }

                    private Blackboard blackboard = new Blackboard();

                    public Blackboard getBlackboard() {
                        return blackboard;
                    }

                    public ErrorHandler getErrorHandler() {
                        return new ErrorHandler() {
                            public void handleThrowable(Throwable t) {
                                t.printStackTrace(System.err);
                            }
                        };
                    }
                }), new OKCancelDialog.Validator() {

                    public String validateInput(Component component) {
                        return ((RunDatastoreQueryPanel) component)
                                .validateInput();
                    }
                });
        dialog.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                System.exit(0);
            }
        });
        dialog.pack();
        GUIUtil.centreOnScreen(dialog);
        dialog.setVisible(true);
    }
*/

    public String validateInput() {
        if (super.validateInput() != null) {
            return super.validateInput();
        }
        if (getQuery().length() == 0) {
            return "Required field missing: Query";
        }
        return null;
    }

    public String getQuery() {
        return queryTextArea.getText().trim();
    }

    /**
     * @return null if the user has left the Max Features text field blank.
     */
    public Integer getMaxFeatures() {
        return maxFeaturesTextField.getText().trim().length() > 0 ? new Integer(
                maxFeaturesTextField.getText().trim()) : null;
    }

    protected Collection connectionDescriptors() {
        return CollectionUtil.select(super.connectionDescriptors(),
                new Block() {
                    public Object yield(Object connectionDescriptor) {
                        try {
                            return Boolean
                                    .valueOf(((DataStoreDriver) Class
                                            .forName(
                                                    ((ConnectionDescriptor) connectionDescriptor)
                                                            .getDataStoreDriverClassName())
                                            .newInstance())
                                            .isAdHocQuerySupported());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }
} //  @jve:decl-index=0:visual-constraint="10,10"
