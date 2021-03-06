package com.vividsolutions.jump.workbench.ui.plugin.datastore;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datastore.ConnectionDescriptor;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelDialog;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;

public class AddDatastoreLayerPanel extends ConnectionPanel {

    private JTextArea whereTextArea = null;

    private JComboBox geometryAttributeComboBox = null;

    private JComboBox datasetComboBox = null;

    // dummy constructor for JBuilder - do not use!!!
    public AddDatastoreLayerPanel() {
      super(null);
    }

    public AddDatastoreLayerPanel(WorkbenchContext context) {
        super(context);
        initialize();
        getConnectionComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDatasetComboBox().setSelectedItem(null);
            }
        });
    }

    private void populateGeometryAttributeComboBox() {
        if (getConnectionDescriptor() == null) {
            return;
        }
        if (getDatasetName() == null) {
            return;
        }
        if (getDatasetName().length() == 0) {
            return;
        }
        try {
            String selectedGeometryAttributeName = getGeometryAttributeName();
            geometryAttributeComboBox.setModel(new DefaultComboBoxModel(
                    sortByString(geometryAttributeNames(getDatasetName(),
                            getConnectionDescriptor()))));
            geometryAttributeComboBox
                    .setSelectedItem(selectedGeometryAttributeName);
        } catch (Exception e) {
            getContext().getErrorHandler().handleThrowable(e);
            geometryAttributeComboBox.setModel(new DefaultComboBoxModel());
        }
    }

    private void populateDatasetComboBox() {
        if (getConnectionDescriptor() == null) {
            return;
        }
        try {
            String selectedDatasetName = getDatasetName();
            datasetComboBox.setModel(new DefaultComboBoxModel(
                    sortByString(datasetNames(getConnectionDescriptor()))));
            datasetComboBox.setSelectedItem(selectedDatasetName);
        } catch (Exception e) {
            getContext().getErrorHandler().handleThrowable(e);
            datasetComboBox.setModel(new DefaultComboBoxModel());
        }
    }

    public static interface Block {
        public Object yield() throws Exception;
    }

    public static Object runInKillableThread(final String description,
            WorkbenchContext context, final Block block) {
        final Object[] result = new Object[] { null };
        // ThreadedBasePlugIn displays a dialog that the user can
        // use to kill the thread by pressing the close button
        // [Jon Aquino 2005-03-14]
        AbstractPlugIn.toActionListener(new ThreadedBasePlugIn() {
            public String getName() {
                return description;
            }

            public boolean execute(PlugInContext context) throws Exception {
                return true;
            }

            public void run(TaskMonitor monitor, PlugInContext context)
                    throws Exception {
                monitor.report(description);
                result[0] = block.yield();
            }
        }, context, new TaskMonitorManager()).actionPerformed(null);
        return result[0];
    }

    private String[] geometryAttributeNames(final String datasetName,
            final ConnectionDescriptor connectionDescriptor) throws Exception {
        // Prompt for a password outside the ThreadedBasePlugIn thread,
        // which is not the GUI thread. [Jon Aquino 2005-03-16]
        new PasswordPrompter().getOpenConnection(connectionManager(),
                connectionDescriptor, this);
        // Retrieve the dataset names using a ThreadedBasePlugIn, so
        // that the user can kill the thread if desired
        // [Jon Aquino 2005-03-16]
        return (String[]) runInKillableThread(
                "Retrieving list of geometry attributes", getContext(),
                new Block() {
                    public Object yield() throws Exception {
                        try {
                            return new PasswordPrompter().getOpenConnection(
                                    connectionManager(), connectionDescriptor,
                                    AddDatastoreLayerPanel.this).getMetadata()
                                    .getGeometryAttributeNames(datasetName);
                        } catch (Exception e) {
                            // Can get here if dataset name is not found in the
                            // datastore [Jon Aquino 2005-03-16]
                            e.printStackTrace(System.err);
                            return new String[] {};
                        }
                    }
                });
    }

    private String[] datasetNames(
            final ConnectionDescriptor connectionDescriptor) throws Exception {
        if (!connectionDescriptorToDatasetNamesMap
                .containsKey(connectionDescriptor)) {
            // Prompt for a password outside the ThreadedBasePlugIn thread,
            // which is not the GUI thread. [Jon Aquino 2005-03-11]
            new PasswordPrompter().getOpenConnection(connectionManager(),
                    connectionDescriptor, this);
            // Retrieve the dataset names using a ThreadedBasePlugIn, so
            // that the user can kill the thread if desired
            // [Jon Aquino 2005-03-11]
            String[] datasetNames = (String[]) runInKillableThread(
                    "Retrieving list of datasets", getContext(), new Block() {
                        public Object yield() throws Exception {
                            return new PasswordPrompter().getOpenConnection(
                                    connectionManager(), connectionDescriptor,
                                    AddDatastoreLayerPanel.this).getMetadata()
                                    .getDatasetNames();
                        }
                    });
            // Don't cache the dataset array if it is empty, as a problem
            // likely occurred. [Jon Aquino 2005-03-14]
            if (datasetNames.length != 0) {
                connectionDescriptorToDatasetNamesMap.put(connectionDescriptor,
                        datasetNames);
            }
        }
        return (String[]) connectionDescriptorToDatasetNamesMap
                .get(connectionDescriptor);
    }

    private Map connectionDescriptorToDatasetNamesMap = new HashMap();

    private JCheckBox cachingCheckBox = null;

    private void initialize() {
        addRow("Dataset:", getDatasetComboBox(), null);
        addRow("Geometry:", getGeometryAttributeComboBox(), null);
        addRow("Where:", new JScrollPane(getWhereTextArea()) {
            {
                setPreferredSize(new Dimension(MAIN_COLUMN_WIDTH, 100));
            }
        }, null);
        addRow("Caching:", getCachingCheckBox(), null);
    }

    private JTextArea getWhereTextArea() {
        if (whereTextArea == null) {
            whereTextArea = new JTextArea();
        }
        return whereTextArea;
    }

    /**
     * Workaround for undesirable Java 1.5 behaviour: after showing a dialog in
     * the #popupMenuWillBecomeVisible event handler, the combobox popup would
     * not hide.
     */
    private void addSafePopupListener(final JComboBox comboBox,
            final Block listener) {
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            private boolean ignoringPopupEvent = false;

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (ignoringPopupEvent) {
                    ignoringPopupEvent = false;
                    return;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        comboBox.hidePopup();
                        try {
                            listener.yield();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            ignoringPopupEvent = true;
                            comboBox.showPopup();
                        }
                    }
                });
            }
        });
    }

    private JComboBox getDatasetComboBox() {
        if (datasetComboBox == null) {
            datasetComboBox = new JComboBox();
            datasetComboBox.setPreferredSize(new Dimension(MAIN_COLUMN_WIDTH,
                    (int) datasetComboBox.getPreferredSize().getHeight()));
            datasetComboBox.setEditable(true);
            datasetComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    populateGeometryAttributeComboBox();
                    if (geometryAttributeComboBox.getItemCount() > 0) {
                        geometryAttributeComboBox.setSelectedIndex(0);
                    }
                }
            });
            // Populate the dataset combobox only if the user pushes the
            // drop-down button, as it requires a time-consuming query.
            // The user can also simply type in the datset name. If they
            // inadvertently press the drop-down button, they can press
            // the X button on the progress dialog to kill the thread.
            // [Jon Aquino 2005-03-14]
            addSafePopupListener(datasetComboBox, new Block() {
                public Object yield() throws Exception {
                    populateDatasetComboBox();
                    return null;
                }
            });
        }
        return datasetComboBox;
    }

    private JComboBox getGeometryAttributeComboBox() {
        if (geometryAttributeComboBox == null) {
            geometryAttributeComboBox = new JComboBox();
            geometryAttributeComboBox.setPreferredSize(new Dimension(
                    MAIN_COLUMN_WIDTH, (int) geometryAttributeComboBox
                            .getPreferredSize().getHeight()));
            geometryAttributeComboBox.setEditable(true);
            addSafePopupListener(geometryAttributeComboBox, new Block() {
                public Object yield() throws Exception {
                    populateGeometryAttributeComboBox();
                    return null;
                }
            });
        }
        return geometryAttributeComboBox;
    }

    private JCheckBox getCachingCheckBox() {
        if (cachingCheckBox == null) {
            cachingCheckBox = new JCheckBox();
            cachingCheckBox.setText("Cache features");
            cachingCheckBox
                    .setToolTipText("<html>Prevents unnecessary queries to the datastore.<br>The recommended setting is to leave this checked.</html>");
            cachingCheckBox.setSelected(true);
        }
        return cachingCheckBox;
    }

/*
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        OKCancelDialog dialog = new OKCancelDialog((Frame) null,
                "Add Datastore Layer", true, new AddDatastoreLayerPanel(
                        new WorkbenchContext() {
                            {
                                getRegistry()
                                        .createEntry(
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
                        return ((AddDatastoreLayerPanel) component)
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

    public String getDatasetName() {
        return datasetComboBox.getSelectedItem() != null ? ((String) datasetComboBox
                .getSelectedItem()).trim()
                : null;
    }

    public String getGeometryAttributeName() {
        return geometryAttributeComboBox.getSelectedItem() != null ? ((String) geometryAttributeComboBox
                .getSelectedItem()).trim()
                : null;
    }

    public String getWhereClause() {
        return getWhereClauseProper().toLowerCase().startsWith("where") ? getWhereClauseProper()
                .substring("where".length()).trim()
                : getWhereClauseProper();
    }

    public String getWhereClauseProper() {
        return whereTextArea.getText().trim();
    }

    public String validateInput() {
        if (super.validateInput() != null) {
            return super.validateInput();
        }
        if (((String) LangUtil.ifNull(getDatasetName(), "")).length() == 0) {
            return "Required field missing: Dataset";
        }
        if (((String) LangUtil.ifNull(getGeometryAttributeName(), "")).length() == 0) {
            return "Required field missing: Geometry";
        }
        return null;
    }

    public boolean isCaching() {
        return getCachingCheckBox().isSelected();
    }

    public void setCaching(boolean caching) {
        getCachingCheckBox().setSelected(caching);
    }
} //  @jve:decl-index=0:visual-constraint="10,10"
