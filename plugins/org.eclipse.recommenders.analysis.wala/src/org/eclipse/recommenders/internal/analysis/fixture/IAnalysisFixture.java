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
package org.eclipse.recommenders.internal.analysis.fixture;

import java.io.File;
import java.util.Set;

public interface IAnalysisFixture {
    String getName();

    String getDescription();

    Set<File> getApplication();

    Set<File> getJavaRuntime();

    Set<File> getDependencies();
}