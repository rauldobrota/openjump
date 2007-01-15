package de.intevation.printlayout;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;

import com.vividsolutions.jump.util.Blackboard;

import com.vividsolutions.jump.task.TaskMonitor;  

import javax.swing.JFrame;

public class PrintLayoutPlugin 
extends      AbstractPlugIn 
implements   ThreadedPlugIn
{
	protected Blackboard blackboard;

	public void initialize(PlugInContext context)
	throws Exception
	{
		EnableCheckFactory check = 
		  new EnableCheckFactory(context.getWorkbenchContext());

		context
			.getFeatureInstaller()
			.addMainMenuItem(
				this, new String[] { "File" }, getName(),
				false, null, check.createAtLeastNLayersMustExistCheck(1));

		 blackboard = new Blackboard();
	}
   

	public boolean execute(PlugInContext context)
	throws Exception
	{
		return true;
	}

	public void run(TaskMonitor monitor, PlugInContext context)
	{
		monitor.allowCancellationRequests();

		JFrame frame = new JFrame("Print/Layout plugin");

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		LayoutFrame viewer = new LayoutFrame(context);

		frame.setContentPane(viewer.createComponents());

		frame.setSize(210*2, 297*2);

		frame.setVisible(true);
	}
}		
// end of file
