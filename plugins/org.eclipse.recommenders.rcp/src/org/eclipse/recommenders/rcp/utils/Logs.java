/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.utils;

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Throws.throwUnreachable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.internal.rcp.Messages;
import org.osgi.framework.Bundle;

public class Logs {

    public static IStatus newStatus(final int kind, final Throwable exception, final String pluginId,
            final String messageFormat, final Object... messageArgs) {
        final String message = messageFormat == null ? "" : format(messageFormat, messageArgs); //$NON-NLS-1$
        final IStatus res = new Status(kind, pluginId, message, exception);
        return res;
    }

    public static void logError(final Throwable exception, final Plugin plugin, final String format,
            final Object... args) {
        final IStatus error = newStatus(IStatus.ERROR, exception, getSymbolicName(plugin), format, args);
        log(error, plugin);
    }

    public static void logWarning(final Throwable exception, final Plugin plugin, final String format,
            final Object... args) {
        final IStatus status = newStatus(IStatus.WARNING, exception, getSymbolicName(plugin), format, args);
        log(status, plugin);
    }

    private static String getSymbolicName(final Plugin plugin) {
        ensureIsNotNull(plugin, "Logging requires a plug-in to be specified"); //$NON-NLS-1$
        final Bundle bundle = plugin.getBundle();
        return bundle.getSymbolicName();
    }

    public static void log(final IStatus status, final Plugin plugin) {
        final ILog log = plugin.getLog();
        try {
            // this fails sometimes with an NPE in
            // org.eclipse.core.internal.runtime.Log.isLoggable(Log.java:101)
            log.log(status);
        } catch (final Exception e) {
            System.out.println(status);
        }
    }

    public static void log(final CoreException exception, final Plugin plugin) {
        final IStatus status = exception.getStatus();
        log(status, plugin);
    }

    public static IStatus newInfo(final Throwable exception, final String pluginId, final String messageFormat,
            final Object... methodArgs) {
        return newStatus(IStatus.INFO, exception, pluginId, messageFormat, methodArgs);
    }

    public static IStatus newInfo(final String pluginId, final String messageFormat, final Object... methodArgs) {
        return newStatus(IStatus.INFO, null, pluginId, messageFormat, methodArgs);
    }

    public static IStatus newError(final Throwable exception, final String pluginId, final String messageFormat,
            final Object... methodArgs) {
        return newStatus(IStatus.ERROR, exception, pluginId, messageFormat, methodArgs);
    }

    public static IStatus newWarning(final Throwable exception, final String pluginId, final String messageFormat,
            final Object... methodArgs) {
        return newStatus(IStatus.WARNING, exception, pluginId, messageFormat, methodArgs);
    }

    public static String toString(final IStatus status) {
        final StringBuilder sb = new StringBuilder();
        appendSeverityAndMessage(status, sb);
        appendException(status, sb);
        if (status.isMultiStatus()) {
            appendChildren(status, sb);
        }
        return sb.toString();
    }

    private static void appendSeverityAndMessage(final IStatus status, final StringBuilder sb) {
        sb.append(toSeverity(status)).append(':').append(' ').append(status.getMessage());
    }

    private static String toSeverity(final IStatus status) {
        switch (status.getSeverity()) {
        case IStatus.CANCEL:
            return Messages.LOG_CANCEL;
        case IStatus.ERROR:
            return Messages.LOG_ERROR;
        case IStatus.WARNING:
            return Messages.LOG_WARNING;
        case IStatus.INFO:
            return Messages.LOG_INFO;
        case IStatus.OK:
            return Messages.LOG_OK;
        default:
            throw throwUnreachable();
        }
    }

    private static void appendException(final IStatus status, final StringBuilder sb) {
        if (status.getException() != null) {
            sb.append(' ').append(status.getException());
        }
    }

    private static void appendChildren(final IStatus status, final StringBuilder sb) {
        for (final IStatus child : status.getChildren()) {
            sb.append('\n').append(toString(child));
        }
    }
}
