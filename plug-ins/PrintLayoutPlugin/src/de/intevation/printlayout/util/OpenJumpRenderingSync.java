/*
 * OpenJumpRenderingSync.java
 * --------------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.util;

import com.vividsolutions.jump.workbench.ui.renderer.ThreadQueue;

public class OpenJumpRenderingSync
implements   ThreadQueue.Listener
{
	protected Runnable job;
	protected boolean  locked;

	protected OpenJumpRenderingSync() {
	}

	public OpenJumpRenderingSync(Runnable job) {
		this.job = job;
	}

	public synchronized void allRunningThreadsFinished() {
		locked = false;
		notify();
	}
		
	public void run(ThreadQueue queue) {
		run(queue, false);
	}

	public void run(ThreadQueue queue, boolean busyWait) {

		if (job == null || queue == null)
			return;

		if (busyWait) {
			job.run();
			job = null;
			try {
				do { Thread.sleep(2000); }
				while (queue.getRunningThreads() > 0);
			}
			catch (InterruptedException ie) {
			}
		}
		else {
			locked = true;

			queue.add(this);

			job.run();

			job = null;

			try {
				synchronized (this) { 
					int i = 10;
					while (locked && --i > 0)
						wait(500); 
				} 
			}
			catch (InterruptedException ie) {}

			queue.remove(this);
		}
	}
}
// end of file
