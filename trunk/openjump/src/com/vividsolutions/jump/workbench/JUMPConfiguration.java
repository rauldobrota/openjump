/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.vividsolutions.jump.workbench;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Field;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.datasource.AbstractLoadDatasetPlugIn;
import com.vividsolutions.jump.workbench.datasource.AbstractSaveDatasetAsPlugIn;
import com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn;
import com.vividsolutions.jump.workbench.datasource.LoadDatasetFromFilePlugIn;
import com.vividsolutions.jump.workbench.datasource.LoadDatasetPlugIn;
import com.vividsolutions.jump.workbench.datasource.SaveDatasetAsPlugIn;
import com.vividsolutions.jump.workbench.datasource.SaveDatasetAsFilePlugIn;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.cursortool.*;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.workbench.ui.plugin.analysis.*;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.*;
import com.vividsolutions.jump.workbench.ui.plugin.generate.BoundaryMatchDataPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.InstallScaleBarPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;
import com.vividsolutions.jump.workbench.ui.plugin.test.RandomArrowsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.test.RandomTrianglesPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.wms.AddWMSQueryPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringEndpointStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.CircleLineStringEndpointStyle;
import com.vividsolutions.jump.workbench.ui.snap.InstallGridPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapToVerticesPolicy;
import com.vividsolutions.jump.workbench.ui.style.ChangeStylesPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import com.vividsolutions.jump.workbench.ui.warp.AffineTransformPlugIn;
import com.vividsolutions.jump.workbench.ui.warp.WarpingPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.*;

/**
 * Initializes the Workbench with various menus and cursor tools. Accesses the
 * Workbench structure through a WorkbenchContext.
 */
public class JUMPConfiguration implements Setup {

    private InstallScaleBarPlugIn installScaleBarPlugIn = new InstallScaleBarPlugIn();
    
    private InstallGridPlugIn installGridPlugIn = new InstallGridPlugIn();

    private PersistentBlackboardPlugIn persistentBlackboardPlugIn = new PersistentBlackboardPlugIn();

    //FirstTaskFramePlugIn will be initialized using reflection in
    // #initializePlugIns [Jon Aquino]
    private FirstTaskFramePlugIn firstTaskFramePlugIn = new FirstTaskFramePlugIn();

    private InstallZoomBarPlugIn installZoomBarPlugIn = new InstallZoomBarPlugIn();

    private MoveLayerablePlugIn moveUpPlugIn = MoveLayerablePlugIn.UP;

    private InstallStandardDataSourceQueryChoosersPlugIn installStandardDataSourceQueryChoosersPlugIn = new InstallStandardDataSourceQueryChoosersPlugIn();

    private InstallStandardFeatureTextWritersPlugIn installStandardFeatureTextWritersPlugIn = new InstallStandardFeatureTextWritersPlugIn();

    private ShortcutKeysPlugIn shortcutKeysPlugIn = new ShortcutKeysPlugIn();

    private ClearSelectionPlugIn clearSelectionPlugIn = new ClearSelectionPlugIn();

    private EditWMSQueryPlugIn editWMSQueryPlugIn = new EditWMSQueryPlugIn();

    private MoveLayerablePlugIn moveDownPlugIn = MoveLayerablePlugIn.DOWN;

    private AddWMSQueryPlugIn addWMSQueryPlugIn = new AddWMSQueryPlugIn();

    private AddNewFeaturesPlugIn addNewFeaturesPlugIn = new AddNewFeaturesPlugIn();

    private OptionsPlugIn optionsPlugIn = new OptionsPlugIn();

    private AddNewCategoryPlugIn addNewCategoryPlugIn = new AddNewCategoryPlugIn();

    private CloneWindowPlugIn cloneWindowPlugIn = new CloneWindowPlugIn();

    private CopySelectedItemsPlugIn copySelectedItemsPlugIn = new CopySelectedItemsPlugIn();

    private CopyThisCoordinatePlugIn copyThisCoordinatePlugIn = new CopyThisCoordinatePlugIn();

    private CopyImagePlugIn copyImagePlugIn = new CopyImagePlugIn();

    private MapToolTipsPlugIn toolTipsPlugIn = new MapToolTipsPlugIn();

    private CopySelectedLayersPlugIn copySelectedLayersPlugIn = new CopySelectedLayersPlugIn();

    private AddNewLayerPlugIn addNewLayerPlugIn = new AddNewLayerPlugIn();

    private AddWMSDemoBoxEasterEggPlugIn addWMSDemoBoxEasterEggPlugIn = new AddWMSDemoBoxEasterEggPlugIn();

    private EditSelectedFeaturePlugIn editSelectedFeaturePlugIn = new EditSelectedFeaturePlugIn();

    private EditingPlugIn editingPlugIn = new EditingPlugIn();

    private EditablePlugIn editablePlugIn = new EditablePlugIn(editingPlugIn);

    private FeatureStatisticsPlugIn featureStatisticsPlugIn = new FeatureStatisticsPlugIn();

    private BeanShellPlugIn beanShellPlugIn = new BeanShellPlugIn();

    private LayerStatisticsPlugIn layerStatisticsPlugIn = new LayerStatisticsPlugIn();

    private LoadDatasetPlugIn loadDatasetPlugIn = new LoadDatasetPlugIn();
    private LoadDatasetFromFilePlugIn loadDatasetFromFilePlugIn = new LoadDatasetFromFilePlugIn();
    private SaveDatasetAsPlugIn saveDatasetAsPlugIn = new SaveDatasetAsPlugIn();
    private SaveDatasetAsFilePlugIn saveDatasetAsFilePlugIn = new SaveDatasetAsFilePlugIn();
    private SaveImageAsPlugIn saveImageAsPlugIn = new SaveImageAsPlugIn();

    private GenerateLogPlugIn generateLogPlugIn = new GenerateLogPlugIn();

    private NewTaskPlugIn newTaskPlugIn = new NewTaskPlugIn();

    private OpenProjectPlugIn openProjectPlugIn = new OpenProjectPlugIn();

    private OverlayPlugIn overlayPlugIn = new OverlayPlugIn();

    private UnionPlugIn unionPlugIn = new UnionPlugIn();

    private GeometryFunctionPlugIn geometryFunctionPlugIn = new GeometryFunctionPlugIn();

    private BufferPlugIn bufferPlugIn = new BufferPlugIn();

    private PasteItemsPlugIn pasteItemsPlugIn = new PasteItemsPlugIn();

    private PasteLayersPlugIn pasteLayersPlugIn = new PasteLayersPlugIn();

    private DeleteAllFeaturesPlugIn deleteAllFeaturesPlugIn = new DeleteAllFeaturesPlugIn();

    private DeleteSelectedItemsPlugIn deleteSelectedItemsPlugIn = new DeleteSelectedItemsPlugIn();

    private RemoveSelectedLayersPlugIn removeSelectedLayersPlugIn = new RemoveSelectedLayersPlugIn();

    private RemoveSelectedCategoriesPlugIn removeSelectedCategoriesPlugIn = new RemoveSelectedCategoriesPlugIn();

    private SaveProjectAsPlugIn saveProjectAsPlugIn = new SaveProjectAsPlugIn();

    private SaveProjectPlugIn saveProjectPlugIn = new SaveProjectPlugIn(
            saveProjectAsPlugIn);

    private SelectFeaturesInFencePlugIn selectFeaturesInFencePlugIn = new SelectFeaturesInFencePlugIn();

    private ScaleBarPlugIn scaleBarPlugIn = new ScaleBarPlugIn();

    private ZoomBarPlugIn zoomBarPlugIn = new ZoomBarPlugIn();

    private ChangeStylesPlugIn changeStylesPlugIn = new ChangeStylesPlugIn();

    private UndoPlugIn undoPlugIn = new UndoPlugIn();

    private RedoPlugIn redoPlugIn = new RedoPlugIn();

    private ValidateSelectedLayersPlugIn validateSelectedLayersPlugIn = new ValidateSelectedLayersPlugIn();

    private CalculateAreasAndLengthsPlugIn calculateAreasAndLengthsPlugIn = new CalculateAreasAndLengthsPlugIn();

    private ViewAttributesPlugIn viewAttributesPlugIn = new ViewAttributesPlugIn();

    private ViewSchemaPlugIn viewSchemaPlugIn = new ViewSchemaPlugIn(
            editingPlugIn);

    private FeatureInfoPlugIn featureInfoPlugIn = new FeatureInfoPlugIn();

    private OutputWindowPlugIn outputWindowPlugIn = new OutputWindowPlugIn();

    private VerticesInFencePlugIn verticesInFencePlugIn = new VerticesInFencePlugIn();

    private ZoomNextPlugIn zoomNextPlugIn = new ZoomNextPlugIn();

    private ZoomToClickPlugIn zoomToClickPlugIn = new ZoomToClickPlugIn(0.5);

    private ZoomPreviousPlugIn zoomPreviousPlugIn = new ZoomPreviousPlugIn();

    private ZoomToFencePlugIn zoomToFencePlugIn = new ZoomToFencePlugIn();

    private ZoomToCoordinatePlugIn zoomToCoordinatePlugIn = new ZoomToCoordinatePlugIn();

    private ZoomToFullExtentPlugIn zoomToFullExtentPlugIn = new ZoomToFullExtentPlugIn();

    private ZoomToLayerPlugIn zoomToLayerPlugIn = new ZoomToLayerPlugIn();

    private ZoomToSelectedItemsPlugIn zoomToSelectedItemsPlugIn = new ZoomToSelectedItemsPlugIn();

    private CutSelectedItemsPlugIn cutSelectedItemsPlugIn = new CutSelectedItemsPlugIn();

    private CutSelectedLayersPlugIn cutSelectedLayersPlugIn = new CutSelectedLayersPlugIn();

    private CombineSelectedFeaturesPlugIn combineSelectedFeaturesPlugIn = new CombineSelectedFeaturesPlugIn();

    private ExplodeSelectedFeaturesPlugIn explodeSelectedFeaturesPlugIn = new ExplodeSelectedFeaturesPlugIn();

    public void setup(WorkbenchContext workbenchContext) throws Exception {
        configureStyles(workbenchContext);
        workbenchContext.getWorkbench().getBlackboard().put(
                SnapToVerticesPolicy.ENABLED_KEY, true);

        EnableCheckFactory checkFactory = new EnableCheckFactory(
                workbenchContext);
        FeatureInstaller featureInstaller = new FeatureInstaller(
                workbenchContext);
        configureToolBar(workbenchContext, checkFactory);
        configureMainMenus(workbenchContext, checkFactory, featureInstaller);
        configureLayerPopupMenu(workbenchContext, featureInstaller,
                checkFactory);
        configureAttributePopupMenu(workbenchContext, featureInstaller,
                checkFactory);
        configureWMSQueryNamePopupMenu(workbenchContext, featureInstaller,
                checkFactory);
        configureCategoryPopupMenu(workbenchContext, featureInstaller);
        configureLayerViewPanelPopupMenu(workbenchContext, checkFactory,
                featureInstaller);

        //Call #initializeBuiltInPlugIns after #configureToolBar so that any
        // plug-ins that
        //add items to the toolbar will add them to the *end* of the toolbar.
        // [Jon Aquino]
        initializeBuiltInPlugIns(workbenchContext);
    }

    private void configureCategoryPopupMenu(WorkbenchContext workbenchContext,
            FeatureInstaller featureInstaller) {
        featureInstaller.addPopupMenuItem(workbenchContext.getWorkbench()
                .getFrame().getCategoryPopupMenu(), loadDatasetFromFilePlugIn,
                loadDatasetFromFilePlugIn.getName() + "...", false, null,
                LoadDatasetPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(workbenchContext.getWorkbench()
                .getFrame().getCategoryPopupMenu(), addNewLayerPlugIn,
                addNewLayerPlugIn.getName(), false, null, null);
        featureInstaller.addPopupMenuItem(workbenchContext.getWorkbench()
                .getFrame().getCategoryPopupMenu(), addWMSQueryPlugIn,
                addWMSQueryPlugIn.getName() + "...", false, null, null);
        featureInstaller.addPopupMenuItem(workbenchContext.getWorkbench()
                .getFrame().getCategoryPopupMenu(), pasteLayersPlugIn,
                pasteLayersPlugIn.getNameWithMnemonic(), false, null,
                pasteLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(workbenchContext.getWorkbench()
                .getFrame().getCategoryPopupMenu(),
                removeSelectedCategoriesPlugIn, removeSelectedCategoriesPlugIn
                        .getName(), false, null, removeSelectedCategoriesPlugIn
                        .createEnableCheck(workbenchContext));
    }

    private void configureWMSQueryNamePopupMenu(
            final WorkbenchContext workbenchContext,
            FeatureInstaller featureInstaller, EnableCheckFactory checkFactory) {
        JPopupMenu wmsLayerNamePopupMenu = workbenchContext.getWorkbench()
                .getFrame().getWMSLayerNamePopupMenu();
        featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu,
                editWMSQueryPlugIn, editWMSQueryPlugIn.getName() + "...",
                false, null, editWMSQueryPlugIn
                        .createEnableCheck(workbenchContext));
        wmsLayerNamePopupMenu.addSeparator(); // ===================
        featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu, moveUpPlugIn,
                moveUpPlugIn.getName(), false, null, moveUpPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu,
                moveDownPlugIn, moveDownPlugIn.getName(), false, null,
                moveDownPlugIn.createEnableCheck(workbenchContext));
        wmsLayerNamePopupMenu.addSeparator(); // ===================
        featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu,
                cutSelectedLayersPlugIn, cutSelectedLayersPlugIn
                        .getNameWithMnemonic(), false, null,
                cutSelectedLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu,
                copySelectedLayersPlugIn, copySelectedLayersPlugIn
                        .getNameWithMnemonic(), false, null,
                copySelectedLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu,
                removeSelectedLayersPlugIn, removeSelectedLayersPlugIn
                        .getName(), false, null, removeSelectedLayersPlugIn
                        .createEnableCheck(workbenchContext));
    }

    private void configureAttributePopupMenu(
            final WorkbenchContext workbenchContext,
            FeatureInstaller featureInstaller, EnableCheckFactory checkFactory) {
        AttributeTab.addPopupMenuItem(workbenchContext, editablePlugIn,
                editablePlugIn.getName(), true, null, editablePlugIn
                        .createEnableCheck(workbenchContext));
        AttributeTab.addPopupMenuItem(workbenchContext, featureInfoPlugIn,
                featureInfoPlugIn.getName(), false, GUIUtil
                        .toSmallIcon(FeatureInfoTool.ICON), FeatureInfoPlugIn
                        .createEnableCheck(workbenchContext));
        AttributeTab.addPopupMenuItem(workbenchContext, viewSchemaPlugIn,
                viewSchemaPlugIn.getName(), false, ViewSchemaPlugIn.ICON,
                ViewSchemaPlugIn.createEnableCheck(workbenchContext));
        AttributeTab.addPopupMenuItem(workbenchContext, cutSelectedItemsPlugIn,
                cutSelectedItemsPlugIn.getName(), false, null,
                cutSelectedItemsPlugIn.createEnableCheck(workbenchContext));
        AttributeTab.addPopupMenuItem(workbenchContext,
                copySelectedItemsPlugIn, copySelectedItemsPlugIn
                        .getNameWithMnemonic(), false, null,
                CopySelectedItemsPlugIn.createEnableCheck(workbenchContext));
        AttributeTab.addPopupMenuItem(workbenchContext,
                deleteSelectedItemsPlugIn, deleteSelectedItemsPlugIn.getName(),
                false, null, DeleteSelectedItemsPlugIn
                        .createEnableCheck(workbenchContext));
    }

    private void configureLayerPopupMenu(
            final WorkbenchContext workbenchContext,
            FeatureInstaller featureInstaller, EnableCheckFactory checkFactory) {
        JPopupMenu layerNamePopupMenu = workbenchContext.getWorkbench()
                .getFrame().getLayerNamePopupMenu();
        featureInstaller.addPopupMenuItem(layerNamePopupMenu, editablePlugIn,
                editablePlugIn.getName(), true, null, editablePlugIn
                        .createEnableCheck(workbenchContext));
        layerNamePopupMenu.addSeparator(); // ===================
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                zoomToLayerPlugIn, zoomToLayerPlugIn.getName(), false, null,
                zoomToLayerPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                changeStylesPlugIn, changeStylesPlugIn.getName() + "...",
                false, GUIUtil.toSmallIcon(changeStylesPlugIn.getIcon()),
                changeStylesPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                viewAttributesPlugIn, viewAttributesPlugIn.getName(), false,
                GUIUtil.toSmallIcon(viewAttributesPlugIn.getIcon()),
                viewAttributesPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(layerNamePopupMenu, viewSchemaPlugIn,
                viewSchemaPlugIn.getName(), false, ViewSchemaPlugIn.ICON,
                ViewSchemaPlugIn.createEnableCheck(workbenchContext));
        layerNamePopupMenu.addSeparator(); // ===================
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                saveDatasetAsFilePlugIn, saveDatasetAsFilePlugIn.getName() + "...",
                false, null, AbstractSaveDatasetAsPlugIn
                        .createEnableCheck(workbenchContext));
        layerNamePopupMenu.addSeparator(); // ===================
        featureInstaller.addPopupMenuItem(layerNamePopupMenu, moveUpPlugIn,
                moveUpPlugIn.getName(), false, null, moveUpPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(layerNamePopupMenu, moveDownPlugIn,
                moveDownPlugIn.getName(), false, null, moveDownPlugIn
                        .createEnableCheck(workbenchContext));
        layerNamePopupMenu.addSeparator(); // ===================
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                cutSelectedLayersPlugIn, cutSelectedLayersPlugIn
                        .getNameWithMnemonic(), false, null,
                cutSelectedLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                copySelectedLayersPlugIn, copySelectedLayersPlugIn
                        .getNameWithMnemonic(), false, null,
                copySelectedLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                removeSelectedLayersPlugIn, removeSelectedLayersPlugIn
                        .getName(), false, null, removeSelectedLayersPlugIn
                        .createEnableCheck(workbenchContext));
        layerNamePopupMenu.addSeparator(); // ===================
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                addNewFeaturesPlugIn, addNewFeaturesPlugIn.getName() + "...",
                false, null, AddNewFeaturesPlugIn
                        .createEnableCheck(workbenchContext));

        //<<TODO:REFACTORING>> JUMPConfiguration is polluted with a lot of
        // EnableCheck
        //logic. This logic should simply be moved to the individual PlugIns.
        // [Jon Aquino]
        featureInstaller.addPopupMenuItem(layerNamePopupMenu, pasteItemsPlugIn,
                pasteItemsPlugIn.getNameWithMnemonic(), false, null,
                PasteItemsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
                deleteAllFeaturesPlugIn, deleteAllFeaturesPlugIn.getName(),
                false, null, deleteAllFeaturesPlugIn
                        .createEnableCheck(workbenchContext));
    }

    private void configureLayerViewPanelPopupMenu(
            WorkbenchContext workbenchContext, EnableCheckFactory checkFactory,
            FeatureInstaller featureInstaller) {
        JPopupMenu popupMenu = LayerViewPanel.popupMenu();
        featureInstaller.addPopupMenuItem(popupMenu, featureInfoPlugIn,
                featureInfoPlugIn.getName(), false, GUIUtil
                        .toSmallIcon(FeatureInfoTool.ICON), FeatureInfoPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller
                .addPopupMenuItem(
                        popupMenu,
                        verticesInFencePlugIn,
                        verticesInFencePlugIn.getName(),
                        false,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerViewPanelMustBeActiveCheck())
                                .add(checkFactory.createFenceMustBeDrawnCheck()));
        popupMenu.addSeparator(); // ===================
        featureInstaller
                .addPopupMenuItem(
                        popupMenu,
                        zoomToFencePlugIn,
                        I18N.get("JUMPConfiguration.fence"),
                        false,
                        GUIUtil.toSmallIcon(zoomToFencePlugIn.getIcon()),
                        new MultiEnableCheck()
                                .add(checkFactory
                                                .createWindowWithLayerViewPanelMustBeActiveCheck())
                                .add(checkFactory.createFenceMustBeDrawnCheck()));
        featureInstaller.addPopupMenuItem(popupMenu, zoomToSelectedItemsPlugIn,
                zoomToSelectedItemsPlugIn.getName(), false, GUIUtil
                        .toSmallIcon(zoomToSelectedItemsPlugIn.getIcon()),
                ZoomToSelectedItemsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(popupMenu, zoomToClickPlugIn,
                I18N.get("JUMPConfiguration.zoom-out"), false, null, null);
        popupMenu.addSeparator(); // ===================
        featureInstaller.addPopupMenuItem(popupMenu,
                selectFeaturesInFencePlugIn, selectFeaturesInFencePlugIn
                        .getName(), false, null, SelectFeaturesInFencePlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(popupMenu, cutSelectedItemsPlugIn,
                cutSelectedItemsPlugIn.getName(), false, null,
                cutSelectedItemsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(popupMenu, copySelectedItemsPlugIn,
                copySelectedItemsPlugIn.getNameWithMnemonic(), false, null,
                CopySelectedItemsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(popupMenu, copyThisCoordinatePlugIn,
                copyThisCoordinatePlugIn.getName(), false, null,
                CopyThisCoordinatePlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(popupMenu, editSelectedFeaturePlugIn,
                editSelectedFeaturePlugIn.getName(), false, null,
                EditSelectedFeaturePlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(popupMenu, deleteSelectedItemsPlugIn,
                deleteSelectedItemsPlugIn.getName(), false, null,
                DeleteSelectedItemsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(popupMenu,
                combineSelectedFeaturesPlugIn, combineSelectedFeaturesPlugIn
                        .getName(), false, null, combineSelectedFeaturesPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addPopupMenuItem(popupMenu,
                explodeSelectedFeaturesPlugIn, explodeSelectedFeaturesPlugIn
                        .getName(), false, null, explodeSelectedFeaturesPlugIn
                        .createEnableCheck(workbenchContext));
    }

    private void configureMainMenus(final WorkbenchContext workbenchContext,
            final EnableCheckFactory checkFactory,
            FeatureInstaller featureInstaller) throws Exception {
        featureInstaller.addMainMenuItem(loadDatasetFromFilePlugIn, MenuNames.FILE,
                loadDatasetFromFilePlugIn.getName() + "...", null, AbstractLoadDatasetPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(saveDatasetAsFilePlugIn, MenuNames.FILE,
                saveDatasetAsFilePlugIn.getName() + "...", null,
                AbstractSaveDatasetAsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(loadDatasetPlugIn, MenuNames.FILE,
                loadDatasetPlugIn.getName() + "...", null, LoadDatasetPlugIn
                        .createEnableCheck(workbenchContext));        
        featureInstaller.addMainMenuItem(saveDatasetAsPlugIn, MenuNames.FILE,
                saveDatasetAsPlugIn.getName() + "...", null,
                SaveDatasetAsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMenuSeparator(MenuNames.FILE); // ===================
        featureInstaller.addMainMenuItem(newTaskPlugIn, MenuNames.FILE, newTaskPlugIn
                .getName()
                + "...", null, null);
        featureInstaller.addMainMenuItem(openProjectPlugIn, MenuNames.FILE,
                openProjectPlugIn.getName() + "...", null,
                new MultiEnableCheck());
        featureInstaller.addMainMenuItem(saveProjectPlugIn, MenuNames.FILE,
                saveProjectPlugIn.getName(), null, checkFactory
                        .createTaskWindowMustBeActiveCheck());
        featureInstaller.addMainMenuItem(saveProjectAsPlugIn, MenuNames.FILE,
                saveProjectAsPlugIn.getName() + "...", null, checkFactory
                        .createTaskWindowMustBeActiveCheck());
        featureInstaller.addMenuSeparator(MenuNames.FILE); // ===================
        featureInstaller.addMainMenuItem(saveImageAsPlugIn, MenuNames.FILE,
                saveImageAsPlugIn.getName() + "...", null, SaveImageAsPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(undoPlugIn, MenuNames.EDIT, undoPlugIn
                .getName(), GUIUtil.toSmallIcon(undoPlugIn.getIcon()),
                undoPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(redoPlugIn, MenuNames.EDIT, redoPlugIn
                .getName(), GUIUtil.toSmallIcon(redoPlugIn.getIcon()),
                redoPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMenuSeparator(MenuNames.EDIT); // ===================
        featureInstaller.addMainMenuItem(addNewFeaturesPlugIn, MenuNames.EDIT,
                addNewFeaturesPlugIn.getName() + "...", null,
                AddNewFeaturesPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(editSelectedFeaturePlugIn, MenuNames.EDIT,
                editSelectedFeaturePlugIn.getName(), null,
                EditSelectedFeaturePlugIn.createEnableCheck(workbenchContext));
        featureInstaller
                .addMainMenuItem(selectFeaturesInFencePlugIn, MenuNames.EDIT,
                        selectFeaturesInFencePlugIn.getName(), null,
                        SelectFeaturesInFencePlugIn
                                .createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(clearSelectionPlugIn, MenuNames.EDIT,
                clearSelectionPlugIn.getName(), null, clearSelectionPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMenuSeparator(MenuNames.EDIT); // ===================
        featureInstaller.addMainMenuItem(cutSelectedItemsPlugIn, MenuNames.EDIT,
                cutSelectedItemsPlugIn.getName(), null, cutSelectedItemsPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(copySelectedItemsPlugIn, MenuNames.EDIT,
                copySelectedItemsPlugIn.getNameWithMnemonic(), null,
                CopySelectedItemsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(pasteItemsPlugIn, MenuNames.EDIT,
                pasteItemsPlugIn.getNameWithMnemonic(), null, PasteItemsPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(copyImagePlugIn, MenuNames.EDIT,
                copyImagePlugIn.getName(), null, CopyImagePlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMenuSeparator(MenuNames.EDIT); // ===================
        featureInstaller.addMainMenuItem(deleteSelectedItemsPlugIn, MenuNames.EDIT,
                deleteSelectedItemsPlugIn.getName(), null,
                DeleteSelectedItemsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMenuSeparator(MenuNames.EDIT); // ===================
        featureInstaller.addMainMenuItem(optionsPlugIn, MenuNames.EDIT, optionsPlugIn
                .getName()
                + "...", null, null);
        editingPlugIn.createMainMenuItem(new String[] { MenuNames.VIEW}, GUIUtil
                .toSmallIcon(EditingPlugIn.ICON), workbenchContext);
        featureInstaller.addMenuSeparator(MenuNames.VIEW); // ===================
        featureInstaller.addMainMenuItem(featureInfoPlugIn, MenuNames.VIEW,
                featureInfoPlugIn.getName(), GUIUtil
                        .toSmallIcon(FeatureInfoTool.ICON), FeatureInfoPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller
                .addMainMenuItem(
                        verticesInFencePlugIn,
                        MenuNames.VIEW,
                        verticesInFencePlugIn.getName(),
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerViewPanelMustBeActiveCheck())
                                .add(checkFactory.createFenceMustBeDrawnCheck()));
        featureInstaller.addMenuSeparator(MenuNames.VIEW); // ===================
        featureInstaller.addMainMenuItem(zoomToFullExtentPlugIn, MenuNames.VIEW,
                zoomToFullExtentPlugIn.getName(), GUIUtil
                        .toSmallIcon(zoomToFullExtentPlugIn.getIcon()),
                zoomToFullExtentPlugIn.createEnableCheck(workbenchContext));
        featureInstaller
                .addMainMenuItem(
                        zoomToFencePlugIn,
                        MenuNames.VIEW,
                        zoomToFencePlugIn.getName(),
                        GUIUtil.toSmallIcon(zoomToFencePlugIn.getIcon()),
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerViewPanelMustBeActiveCheck())
                                .add(checkFactory.createFenceMustBeDrawnCheck()));
        featureInstaller.addMainMenuItem(zoomToSelectedItemsPlugIn, MenuNames.VIEW,
                zoomToSelectedItemsPlugIn.getName(), GUIUtil
                        .toSmallIcon(zoomToSelectedItemsPlugIn.getIcon()),
                ZoomToSelectedItemsPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(zoomToCoordinatePlugIn, MenuNames.VIEW,
                zoomToCoordinatePlugIn.getName() + "...", null,
                zoomToCoordinatePlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(zoomPreviousPlugIn, MenuNames.VIEW,
                zoomPreviousPlugIn.getName(), GUIUtil
                        .toSmallIcon(zoomPreviousPlugIn.getIcon()),
                zoomPreviousPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(zoomNextPlugIn, MenuNames.VIEW, zoomNextPlugIn
                .getName(), GUIUtil.toSmallIcon(zoomNextPlugIn.getIcon()),
                zoomNextPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMenuSeparator(MenuNames.VIEW); // ===================
        featureInstaller
                .addMainMenuItem(
                        scaleBarPlugIn,
                        new String[] { MenuNames.VIEW},
                        scaleBarPlugIn.getName(),
                        true,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerViewPanelMustBeActiveCheck())
                                .add(new EnableCheck() {

                                    public String check(JComponent component) {
                                        ((JCheckBoxMenuItem) component)
                                                .setSelected(ScaleBarRenderer
                                                        .isEnabled(workbenchContext
                                                                .getLayerViewPanel()));

                                        return null;
                                    }
                                }));
        featureInstaller.addMainMenuItem(toolTipsPlugIn,
                new String[] {MenuNames.VIEW}, toolTipsPlugIn.getName(), true, null,
                MapToolTipsPlugIn.createEnableCheck(workbenchContext));
        zoomBarPlugIn.createMainMenuItem(new String[] { MenuNames.VIEW}, null,
                workbenchContext);
        featureInstaller.addMainMenuItem(outputWindowPlugIn, MenuNames.VIEW,
                outputWindowPlugIn.getName(), GUIUtil
                        .toSmallIcon(outputWindowPlugIn.getIcon()), null);
        featureInstaller.addMainMenuItem(generateLogPlugIn, MenuNames.VIEW,
                generateLogPlugIn.getName() + "...", null, null);
        featureInstaller.addLayerViewMenuItem(addNewLayerPlugIn, MenuNames.LAYER,
                addNewLayerPlugIn.getName());
        featureInstaller.addLayerViewMenuItem(addWMSQueryPlugIn, MenuNames.LAYER,
                addWMSQueryPlugIn.getName());
        featureInstaller.addMainMenuItem(addNewCategoryPlugIn, MenuNames.LAYER,
                addNewCategoryPlugIn.getName(), null, addNewCategoryPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMenuSeparator(MenuNames.LAYER); // ===================
        featureInstaller.addMainMenuItem(cutSelectedLayersPlugIn, MenuNames.LAYER,
                cutSelectedLayersPlugIn.getNameWithMnemonic(), null,
                cutSelectedLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(copySelectedLayersPlugIn, MenuNames.LAYER,
                copySelectedLayersPlugIn.getNameWithMnemonic(), null,
                copySelectedLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(pasteLayersPlugIn, MenuNames.LAYER,
                pasteLayersPlugIn.getNameWithMnemonic(), null,
                pasteLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMenuSeparator(MenuNames.LAYER); // ===================
        featureInstaller.addMainMenuItem(removeSelectedLayersPlugIn, MenuNames.LAYER,
                removeSelectedLayersPlugIn.getName(), null,
                removeSelectedLayersPlugIn.createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(removeSelectedCategoriesPlugIn,
        		MenuNames.LAYER, removeSelectedCategoriesPlugIn.getName(), null,
                removeSelectedCategoriesPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(cloneWindowPlugIn, MenuNames.WINDOW,
                cloneWindowPlugIn.getName(), null, new EnableCheck() {

                    public String check(JComponent component) {
                        return (!(workbenchContext.getWorkbench().getFrame()
                                .getActiveInternalFrame() instanceof CloneableInternalFrame)) ? I18N.get("JUMPConfiguration.not-available-for-the-current-window")
                                : null;
                    }
                });
        featureInstaller.addMenuSeparator(MenuNames.WINDOW); // ===================
        new BoundaryMatchDataPlugIn().initialize(new PlugInContext(
                workbenchContext, null, null, null, null));
        new WarpingPlugIn().initialize(new PlugInContext(workbenchContext,
                null, null, null, null));
        new AffineTransformPlugIn().initialize(new PlugInContext(
                workbenchContext, null, null, null, null));
        new RandomTrianglesPlugIn().initialize(new PlugInContext(
                workbenchContext, null, null, null, null));
        new RandomArrowsPlugIn().initialize(new PlugInContext(workbenchContext,
                null, null, null, null));
        featureInstaller
                .addMainMenuItem(
                        overlayPlugIn,
                        new String[] {  MenuNames.TOOLS, MenuNames.TOOLS_ANALYSIS},
                        overlayPlugIn.getName() + "...",
                        false,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerNamePanelMustBeActiveCheck())
                                .add(
                                        checkFactory
                                                .createAtLeastNLayersMustExistCheck(2)));
        featureInstaller
                .addMainMenuItem(
                        unionPlugIn,
                        new String[] {MenuNames.TOOLS, MenuNames.TOOLS_ANALYSIS},
                        unionPlugIn.getName() + "...",
                        false,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerNamePanelMustBeActiveCheck())
                                .add(
                                        checkFactory
                                                .createAtLeastNLayersMustExistCheck(1)));
        featureInstaller
                .addMainMenuItem(
                        geometryFunctionPlugIn,
                        new String[] { MenuNames.TOOLS, MenuNames.TOOLS_ANALYSIS},
                        geometryFunctionPlugIn.getName() + "...",
                        false,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerNamePanelMustBeActiveCheck())
                                .add(
                                        checkFactory
                                                .createAtLeastNLayersMustExistCheck(2)));
        featureInstaller
                .addMainMenuItem(
                        bufferPlugIn,
                        new String[] {MenuNames.TOOLS, MenuNames.TOOLS_ANALYSIS},
                        bufferPlugIn.getName() + "...",
                        false,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerNamePanelMustBeActiveCheck())
                                .add(
                                        checkFactory
                                                .createAtLeastNLayersMustExistCheck(1)));
        featureInstaller
                .addMainMenuItem(
                        validateSelectedLayersPlugIn,
                        new String[] { MenuNames.TOOLS, MenuNames.TOOLS_QA},
                        validateSelectedLayersPlugIn.getName() + "...",
                        false,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerNamePanelMustBeActiveCheck())
                                .add(
                                        checkFactory
                                                .createAtLeastNLayersMustBeSelectedCheck(1)));
        featureInstaller
                .addMainMenuItem(
                        layerStatisticsPlugIn,
                        new String[] { MenuNames.TOOLS, MenuNames.TOOLS_ANALYSIS},
                        layerStatisticsPlugIn.getName(),
                        false,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerNamePanelMustBeActiveCheck())
                                .add(
                                        checkFactory
                                                .createAtLeastNLayersMustBeSelectedCheck(1)));
        featureInstaller
                .addMainMenuItem(
                        featureStatisticsPlugIn,
                        new String[] { MenuNames.TOOLS, MenuNames.TOOLS_QA},
                        featureStatisticsPlugIn.getName(),
                        false,
                        null,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerNamePanelMustBeActiveCheck())
                                .add(
                                        checkFactory
                                                .createAtLeastNLayersMustBeSelectedCheck(1)));
        featureInstaller.addMainMenuItem(calculateAreasAndLengthsPlugIn,
                new String[] { MenuNames.TOOLS, MenuNames.TOOLS_ANALYSIS},
                calculateAreasAndLengthsPlugIn.getName() + "...", false, null,
                calculateAreasAndLengthsPlugIn
                        .createEnableCheck(workbenchContext));
        featureInstaller.addMainMenuItem(shortcutKeysPlugIn, MenuNames.HELP,
                shortcutKeysPlugIn.getName() + "...", null, null);
        new FeatureInstaller(workbenchContext).addMainMenuItem(
                new AboutPlugIn(), MenuNames.HELP, I18N.get("JUMPConfiguration.about"), null, null);
    }

    private void configureStyles(WorkbenchContext workbenchContext) {
        WorkbenchFrame frame = workbenchContext.getWorkbench().getFrame();
        frame
                .addChoosableStyleClass(ArrowLineStringEndpointStyle.FeathersStart.class);
        frame
                .addChoosableStyleClass(ArrowLineStringEndpointStyle.FeathersEnd.class);
        frame
                .addChoosableStyleClass(ArrowLineStringEndpointStyle.OpenStart.class);
        frame
                .addChoosableStyleClass(ArrowLineStringEndpointStyle.OpenEnd.class);
        frame
                .addChoosableStyleClass(ArrowLineStringEndpointStyle.SolidStart.class);
        frame
                .addChoosableStyleClass(ArrowLineStringEndpointStyle.SolidEnd.class);
        frame
                .addChoosableStyleClass(ArrowLineStringEndpointStyle.NarrowSolidStart.class);
        frame
                .addChoosableStyleClass(ArrowLineStringEndpointStyle.NarrowSolidEnd.class);
        frame.addChoosableStyleClass(CircleLineStringEndpointStyle.Start.class);
        frame.addChoosableStyleClass(CircleLineStringEndpointStyle.End.class);
    }

    private QuasimodeTool add(CursorTool tool, WorkbenchContext context) {
        return context.getWorkbench().getFrame().getToolBar().addCursorTool(
                tool).getQuasimodeTool();
    }

    private void configureToolBar(final WorkbenchContext workbenchContext,
            EnableCheckFactory checkFactory) {
        WorkbenchFrame frame = workbenchContext.getWorkbench().getFrame();
        add(new ZoomTool(), workbenchContext);
        add(new PanTool(), workbenchContext);
        frame.getToolBar().addSeparator();
        frame.getToolBar().addPlugIn(zoomToFullExtentPlugIn.getIcon(),
                zoomToFullExtentPlugIn,
                zoomToFullExtentPlugIn.createEnableCheck(workbenchContext),
                workbenchContext);
        frame.getToolBar().addPlugIn(zoomToSelectedItemsPlugIn.getIcon(),
                zoomToSelectedItemsPlugIn,
                ZoomToSelectedItemsPlugIn.createEnableCheck(workbenchContext),
                workbenchContext);
        frame
                .getToolBar()
                .addPlugIn(
                        zoomToFencePlugIn.getIcon(),
                        zoomToFencePlugIn,
                        new MultiEnableCheck()
                                .add(
                                        checkFactory
                                                .createWindowWithLayerViewPanelMustBeActiveCheck())
                                .add(checkFactory.createFenceMustBeDrawnCheck()),
                        workbenchContext);
        frame.getToolBar().addPlugIn(zoomPreviousPlugIn.getIcon(),
                zoomPreviousPlugIn,
                zoomPreviousPlugIn.createEnableCheck(workbenchContext),
                workbenchContext);
        frame.getToolBar().addPlugIn(zoomNextPlugIn.getIcon(), zoomNextPlugIn,
                zoomNextPlugIn.createEnableCheck(workbenchContext),
                workbenchContext);
        frame.getToolBar().addPlugIn(changeStylesPlugIn.getIcon(),
                changeStylesPlugIn,
                changeStylesPlugIn.createEnableCheck(workbenchContext),
                workbenchContext);
        frame.getToolBar().addPlugIn(viewAttributesPlugIn.getIcon(),
                viewAttributesPlugIn,
                viewAttributesPlugIn.createEnableCheck(workbenchContext),
                workbenchContext);
        frame.getToolBar().addSeparator();

        //Null out the quasimodes for [Ctrl] because the Select tools will
        // handle that case. [Jon Aquino]
        add(new QuasimodeTool(new SelectFeaturesTool()).add(
                new QuasimodeTool.ModifierKeySpec(true, false, false), null),
                workbenchContext);
        add(new OrCompositeTool() {

            public String getName() {
                return I18N.get("JUMPConfiguration.fence");
            }
        }.add(new DrawRectangleFenceTool()).add(new DrawPolygonFenceTool()),
                workbenchContext);
        add(new FeatureInfoTool(), workbenchContext);
        frame.getToolBar().addSeparator();
        configureEditingButton(workbenchContext);
        frame.getToolBar().addSeparator();
        add(new MeasureTool(), workbenchContext);
        frame.getToolBar().addSeparator();
        frame.getToolBar().addPlugIn(undoPlugIn.getIcon(), undoPlugIn,
                undoPlugIn.createEnableCheck(workbenchContext),
                workbenchContext);
        frame.getToolBar().addPlugIn(redoPlugIn.getIcon(), redoPlugIn,
                redoPlugIn.createEnableCheck(workbenchContext),
                workbenchContext);
        frame.getToolBar().addSeparator();
        workbenchContext.getWorkbench().getFrame().getOutputFrame().setButton(
                frame.getToolBar().addPlugIn(outputWindowPlugIn.getIcon(),
                        outputWindowPlugIn, new MultiEnableCheck(),
                        workbenchContext));
        //Last of all, add a separator because some plug-ins may add
        // CursorTools.
        //[Jon Aquino]
        frame.getToolBar().addSeparator();
    }

    private void configureEditingButton(final WorkbenchContext workbenchContext) {
        final JToggleButton toggleButton = new JToggleButton();
        workbenchContext.getWorkbench().getFrame().getToolBar().add(
                toggleButton,
                editingPlugIn.getName(),
                EditingPlugIn.ICON,
                AbstractPlugIn.toActionListener(editingPlugIn,
                        workbenchContext, new TaskMonitorManager()), null);
        workbenchContext.getWorkbench().getFrame().addComponentListener(
                new ComponentAdapter() {

                    public void componentShown(ComponentEvent e) {
                        //Can't #getToolbox before Workbench is thrown.
                        // Otherwise, get
                        //IllegalComponentStateException. Thus, do it inside
                        // #componentShown. [Jon Aquino]
                        editingPlugIn.getToolbox(workbenchContext)
                                .addComponentListener(new ComponentAdapter() {

                                    //There are other ways to show/hide the
                                    // toolbox. Track 'em. [Jon Aquino]
                                    public void componentShown(ComponentEvent e) {
                                        toggleButton.setSelected(true);
                                    }

                                    public void componentHidden(ComponentEvent e) {
                                        toggleButton.setSelected(false);
                                    }
                                });
                    }
                });
    }

    /**
     * Call each PlugIn's #initialize() method. Uses reflection to build a list
     * of plug-ins.
     * 
     * @param workbenchContext
     *                   Description of the Parameter
     * @exception Exception
     *                        Description of the Exception
     */
    private void initializeBuiltInPlugIns(WorkbenchContext workbenchContext)
            throws Exception {
        Field[] fields = getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Object field = null;

            try {
                field = fields[i].get(this);
            } catch (IllegalAccessException e) {
                Assert.shouldNeverReachHere();
            }

            if (!(field instanceof PlugIn)) {
                continue;
            }

            PlugIn plugIn = (PlugIn) field;
            plugIn.initialize(new PlugInContext(workbenchContext, null, null,
                    null, null));
        }
    }
}
