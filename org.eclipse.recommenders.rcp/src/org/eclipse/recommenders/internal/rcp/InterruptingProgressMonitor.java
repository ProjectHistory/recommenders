package org.eclipse.recommenders.internal.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.rcp.utils.CountingProgressMonitor;

public class InterruptingProgressMonitor implements IProgressMonitor {

    private final CountingProgressMonitor delegate;

    @Override
    public void beginTask(final String name, final int plannedTotalWork) {
        delegate.beginTask(name, plannedTotalWork);
    }

    @Override
    public void done() {
        delegate.done();
    }

    @Override
    public void internalWorked(final double work) {
        delegate.internalWorked(work);
    }

    @Override
    public boolean isCanceled() {
        return delegate.isCanceled();
    }

    @Override
    public void setCanceled(final boolean value) {
        delegate.setCanceled(value);
    }

    @Override
    public void setTaskName(final String name) {
        delegate.setTaskName(name);
    }

    @Override
    public void subTask(final String name) {
        delegate.subTask(name);
    }

    @Override
    public void worked(final int work) {
        delegate.worked(work);
    }

    public void worked() {
        delegate.worked();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public InterruptingProgressMonitor(final CountingProgressMonitor monitor) {
        this.delegate = monitor;
    }

}
