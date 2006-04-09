package com.vividsolutions.jump.workbench.ui.plugin.datastore;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.*;


import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datastore.ConnectionDescriptor;
import com.vividsolutions.jump.workbench.datastore.ConnectionManager;
import com.vividsolutions.jump.workbench.ui.OKCancelDialog;

/**
 * Base class for panels with a Connection combobox.
 */
public class ConnectionPanel extends JPanel
{
  // dummy constructor for JBuilder - do not use!!!
  public ConnectionPanel()  {  }

    public ConnectionPanel(WorkbenchContext context) {
        this.context = context;
        initialize();
        populateConnectionComboBox();
    }

    private WorkbenchContext context;

    protected static final int MAIN_COLUMN_WIDTH = 400;

    private JComboBox connectionComboBox = null;

    private int nextRow = 0;

    protected void addRow(String caption, Component a,
            Component b) {
        add(new JLabel(caption), new GridBagConstraints(0, nextRow, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2,
                        2, 2, 2), 0, 0));
        add(a, new GridBagConstraints(1, nextRow, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2,
                        2, 2, 2), 0, 0));
        if (b != null) {
            add(b, new GridBagConstraints(2, nextRow, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(2, 2, 2, 2), 0, 0));
        }
        nextRow++;
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        addRow("Connection:", getConnectionComboBox(), getChooseConnectionButton());
    }

    public void populateConnectionComboBox() {
        ConnectionDescriptor selectedConnectionDescriptor = getConnectionDescriptor();
        connectionComboBox.setModel(new DefaultComboBoxModel(
                sortByString(connectionDescriptors()
                        .toArray())));
        // Note that the selectedConnectionDescriptor may no longer exist,
        // in which case #setSelectedItem will have no effect.
        // [Jon Aquino 2005-03-10]
        connectionComboBox.setSelectedItem(selectedConnectionDescriptor);
    }

    protected Collection connectionDescriptors() {
        return connectionManager().getConnectionDescriptors();
    }

    protected ConnectionManager connectionManager() {
        return ConnectionManager.instance(context);
    }

    protected Object[] sortByString(Object[] objects) {
        Arrays.sort(objects, new Comparator() {
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return objects;
    }

    public ConnectionDescriptor getConnectionDescriptor() {
        return (ConnectionDescriptor) connectionComboBox.getSelectedItem();
    }

    public WorkbenchContext getContext() {
        return context;
    }

    protected JComboBox getConnectionComboBox() {
        if (connectionComboBox == null) {
            connectionComboBox = new JComboBox();
            connectionComboBox.setPreferredSize(new Dimension(MAIN_COLUMN_WIDTH,
                    (int) connectionComboBox.getPreferredSize().getHeight()));
        }
        return connectionComboBox;
    }

    private void chooseConnection() {
        ConnectionManagerPanel panel = new ConnectionManagerPanel(
                ConnectionManager.instance(getContext()),
                getContext().getRegistry(), getContext().getErrorHandler(),context);
        OKCancelDialog dialog = new OKCancelDialog((Dialog) SwingUtilities
                .windowForComponent(ConnectionPanel.this), "Connection Manager",
                true, panel, new OKCancelDialog.Validator() {
                    public String validateInput(Component component) {
                        return null;
                    }
                });
        dialog.setVisible(true);
        // Even if OK was not pressed, refresh the combobox.
        // [Jon Aquino 2005-03-16]
        populateConnectionComboBox();
        if (!dialog.wasOKPressed()) {
            return;
        }
        if (panel.getSelectedConnectionDescriptors().isEmpty()) {
            return;
        }
        getConnectionComboBox().setSelectedItem(
                panel.getSelectedConnectionDescriptors().iterator().next());
    }

    private JButton getChooseConnectionButton() {
        if (chooseConnectionButton == null) {
            chooseConnectionButton = new JButton();
            ImageIcon icon = new ImageIcon(ConnectionManagerPanel.class
                                .getResource("databases.gif"));
            chooseConnectionButton.setIcon(icon);
            chooseConnectionButton.setToolTipText("Connection Manager");
            chooseConnectionButton.setMargin(new Insets(0, 0, 0, 0));
            chooseConnectionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    chooseConnection();
                }
            });
        }
        return chooseConnectionButton;
    }

    private JButton chooseConnectionButton = null;

    public String validateInput() {
        if (getConnectionDescriptor() == null) {
            return "Required field missing: Connection";
        }
        return null;
    }
}