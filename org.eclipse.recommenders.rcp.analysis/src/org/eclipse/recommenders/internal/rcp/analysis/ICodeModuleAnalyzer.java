/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.analysis;

import java.io.File;

import org.eclipse.recommenders.internal.commons.analysis.codeelements.CodeModuleDescriptor;

public interface ICodeModuleAnalyzer {

    public void analyze(File moduleToAnalyze, CodeModuleDescriptor moduleDescriptorToComplete);

}