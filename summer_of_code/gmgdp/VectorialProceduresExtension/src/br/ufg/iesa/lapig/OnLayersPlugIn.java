package br.ufg.iesa.lapig;

import com.vividsolutions.jts.operation.union.*;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * @description:
 *    Make vectorial procedures on espefieds layers.
 *     
 * @author Leandro Leal
 *
 **/
public class OnLayersPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

    private String INFO = "Select one operation and two layers: ";
    private String L1 = "Layer 1: ";
    private String L2 = "Layer 2: ";
    private String L3 = "Resultant Layer";
    private String[] OPERATIONS = new String[]{"Union", "Intersection", "Difference",
        "Sym Difference"
    };
    private MultiInputDialog dialog;
    private VectorialProcedures procedures;

    /**
     * 
     * @param context   
     * @throws java.lang.Exception
     */
    public void initialize(PlugInContext context) throws Exception {

        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
        featureInstaller.addMainMenuItem(
                this,
                new String[]{"VectorialProcedures"},
                this.getName(),
                false, //checkbox
                null,
                createEnableCheck(context.getWorkbenchContext())); //enable check
    }

    /**
     * The menu entry will be activated only when two layers existing.
     * @param workbenchContext  
     * @return 
     */
    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

        return new MultiEnableCheck().add(checkFactory.createAtLeastNLayersMustExistCheck(2)).add(checkFactory.createTaskWindowMustBeActiveCheck());
    }

    /**
     * 
     * @param context
     * @return
     * @throws java.lang.Exception
     */
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);

        this.procedures = new VectorialProcedures();
        this.dialog = new MultiInputDialog(
                context.getWorkbenchFrame(), "Vectorial Procedures", true);
        this.setDialogValues(dialog, context);
        GUIUtil.centreOnWindow(dialog);
        this.dialog.setVisible(true);
        if (!dialog.wasOKPressed()) {
            return false;
        }
        return true;
    }

    /* Add all components in PlugInContext */
    private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
        dialog.addLabel(INFO);
        dialog.addSeparator();
        dialog.addRadioButton(OPERATIONS[0], "operationButtons", true, "Operation Union");
        dialog.addRadioButton(OPERATIONS[1], "operationButtons", true, "Operation Intersection");
        dialog.addRadioButton(OPERATIONS[2], "operationButtons", true, "Operation Difference");
        dialog.addRadioButton(OPERATIONS[3], "operationButtons", true, "Operation Sym Difference");
        dialog.addSeparator();
        dialog.addLayerComboBox(this.L1, context.getCandidateLayer(0), null, context.getLayerManager());
        dialog.addLayerComboBox(this.L2, context.getCandidateLayer(1), null, context.getLayerManager());
        dialog.addSeparator();
        dialog.addCheckBox("With buffer", false);

        JTextField doubleField = dialog.addDoubleField("Value:", 0.0, 10);
        doubleField.setEnabled(false);
        dialog.getCheckBox("With buffer").addActionListener(new CheckAction(doubleField));

        dialog.setSize(600, 540);
    }

    /* Get a specified layer.*/
    private FeatureCollectionWrapper getLayer(String L) {
        return dialog.getLayer(L).getFeatureCollectionWrapper();
    }

    /**
     * 
     * @param monitor   Current monitor for process
     * @param context
     * @throws java.lang.Exception
     */
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();

        try {
            String typeOper = getTypeOperation();
            FeatureCollection result = null;

            if (typeOper.equals("Union")) { // Union procedure
                result = procedures.Union(monitor, getLayer(L1), getLayer(L2));
            } else if (typeOper.equals("Intersection")) { // Intersection procedure
                result = procedures.Intersection(monitor, getLayer(L1), getLayer(L2));
            } else if (typeOper.equals("Difference")) { // Difference procedure
                result = procedures.difference(monitor, getLayer(L1), getLayer(L2));
            } else if (typeOper.equals("Sym Difference")) { // Sym Difference procedure
                result = procedures.symDifference(monitor, getLayer(L1), getLayer(L2));
            }

            /* Check if "With buffer" was selected */
            if (dialog.getCheckBox("With buffer").isSelected()) { // Buffer procedure
                /* Add buffer Layer */
                context.addLayer(StandardCategoryNames.WORKING, typeOper + "buffer", 
                        procedures.Buffer(monitor, result, dialog.getDouble("Value:")));
            }

            /* Add result layer */
            monitor.report("Gerate resultant Layer");
            context.getLayerManager().addCategory(StandardCategoryNames.RESULT);
            context.addLayer(StandardCategoryNames.RESULT, typeOper + " Result", result);

            System.gc();
        } catch (NullPointerException e) { // When some layer haven't Features
            JOptionPane.showMessageDialog(context.getActiveInternalFrame(),
                    "Layer without features.",
                    "Invallid Layer", JOptionPane.WARNING_MESSAGE);
        }

    }

    /* Get a selected operation */
    private String getTypeOperation() {
        for (int i = 0; i < OPERATIONS.length; i++) {
            if (dialog.getRadioButton(OPERATIONS[i]).isSelected()) {
                return OPERATIONS[i];
            }
        }
        return null;
    }

    /* Implement actions for buffer JCheckBox */
    private class CheckAction implements ActionListener {

        private final JTextField doubleField;
        private Boolean check;

        public CheckAction(JTextField doubleField) {
            this.doubleField = doubleField;
            check = false;
        }

        public void actionPerformed(ActionEvent e) {
            /* Enable the JTextField "Value:" */
            if (e.getSource() == dialog.getCheckBox("With buffer")) {
                doubleField.setEnabled(!check);
                check = !check;
            }
        }
    }
}
