package org.eclipse.recommenders.internal.completion.rcp.subwords;

import org.eclipse.core.runtime.IProgressMonitor;

public class TimeDelimitedProgressMonitor implements IProgressMonitor {

    long start = System.currentTimeMillis();

    private IProgressMonitor delegate;

    private int limit;

    public TimeDelimitedProgressMonitor(IProgressMonitor delegate, int limitInMillis) {
        this.delegate = delegate;
        this.limit = limitInMillis;
    }

    public void beginTask(String name, int totalWork) {
        delegate.beginTask(name, totalWork);
    }

    public void done() {
        delegate.done();
    }

    public void internalWorked(double work) {
        delegate.internalWorked(work);
    }

    public boolean isCanceled() {
        return delegate.isCanceled() || timedOut();
    }

    private boolean timedOut() {
        boolean res = System.currentTimeMillis() - start > limit;
        return res;
    }

    public void setCanceled(boolean value) {
        delegate.setCanceled(value);
    }

    public void setTaskName(String name) {
        delegate.setTaskName(name);
    }

    public void subTask(String name) {
        delegate.subTask(name);
    }

    public void worked(int work) {
        delegate.worked(work);
    }

}