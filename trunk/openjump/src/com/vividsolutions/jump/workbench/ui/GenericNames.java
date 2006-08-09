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
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.I18N;

/**
 * Use these for generic names as layer, layer A, function...
 * 
 * @author Basile Chandesris - <chandesris@pt-consulting.lu>
 */
public interface GenericNames {
	public static String LAYER = I18N.get("ui.GenericNames.LAYER");
	public static String LAYER_A = I18N.get("ui.GenericNames.LAYER_A");
	public static String LAYER_B = I18N.get("ui.GenericNames.LAYER_B");
	public static String RESULT_LAYER=I18N.get("ui.GenericNames.ResultLayer");
	public static String LAYER_GRID=I18N.get("ui.GenericNames.LayerGrid");
	public static String ANGLE=I18N.get("ui.GenericNames.ANGLE");
	public static String SOURCE_LAYER=I18N.get("ui.GenericNames.Source-Layer");
	public static String TARGET_LAYER=I18N.get("ui.GenericNames.Target-Layer");
	
	
	public static String CALCULATE_IN_PROGRESS=I18N.get("ui.GenericNames.CalculateInProgress");
	public static String GLOBAL_BOX=I18N.get("ui.GenericNames.GlobalBox");
	
}
