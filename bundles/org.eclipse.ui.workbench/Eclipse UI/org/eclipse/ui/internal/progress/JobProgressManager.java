package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IProgressProvider;
import org.eclipse.core.runtime.jobs.Job;

/**
 * JobProgressManager provides the progress monitor to the 
 * job manager and informs any ProgressContentProviders of changes.
 */
public class JobProgressManager implements IProgressProvider {

	private ArrayList providers = new ArrayList();
	private static JobProgressManager singleton;

	public static JobProgressManager getInstance() {
		if (singleton == null)
			singleton = new JobProgressManager();
		return singleton;
	}

	private class JobMonitor implements IProgressMonitor {
		Job job;

		JobMonitor(Job newJob) {
			job = newJob;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
		 */
		public void beginTask(String name, int totalWork) {
			Iterator iterator = providers.iterator();
			while (iterator.hasNext()) {
				ProgressContentProvider provider =
					(ProgressContentProvider) iterator.next();
				provider.beginTask(job, name, totalWork);
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#done()
		 */
		public void done() {
			// XXX Auto-generated method stub

		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
		 */
		public void internalWorked(double work) {
			Iterator iterator = providers.iterator();
			while (iterator.hasNext()) {
				ProgressContentProvider provider =
					(ProgressContentProvider) iterator.next();
				provider.worked(job, work);
			}

		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
		 */
		public boolean isCanceled() {
			// XXX Auto-generated method stub
			return false;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
		 */
		public void setCanceled(boolean value) {
			// XXX Auto-generated method stub

		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
		 */
		public void setTaskName(String name) {
			// XXX Auto-generated method stub

		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
		 */
		public void subTask(String name) {
			Iterator iterator = providers.iterator();
			while (iterator.hasNext()) {
				ProgressContentProvider provider =
					(ProgressContentProvider) iterator.next();
				provider.subTask(job, name);
			}

		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
		 */
		public void worked(int work) {
			Iterator iterator = providers.iterator();
			while (iterator.hasNext()) {
				ProgressContentProvider provider =
					(ProgressContentProvider) iterator.next();
				provider.worked(job, work);
			}
		}
	};

	public JobProgressManager() {
		JobManager.getInstance().setProgressProvider(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressProvider#createMonitor(org.eclipse.core.runtime.jobs.Job)
	 */
	public IProgressMonitor createMonitor(Job job) {
		return new JobMonitor(job);
	}

	void addProvider(ProgressContentProvider provider) {
		providers.add(provider);
	}

	void removeProvider(ProgressContentProvider provider) {
		providers.remove(provider);
	}

}
