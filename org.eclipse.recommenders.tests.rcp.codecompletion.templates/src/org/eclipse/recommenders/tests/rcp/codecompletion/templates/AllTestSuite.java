/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.rcp.codecompletion.templates;

import org.eclipse.recommenders.tests.rcp.codecompletion.templates.ui.UiTestSuite;
import org.eclipse.recommenders.tests.rcp.codecompletion.templates.unit.UnitTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Suite for running both test systems at once, the JUnit tests and the UI tests
 * driven by SWTBot.
 */
@RunWith(Suite.class)
@SuiteClasses({ UnitTestSuite.class, UiTestSuite.class })
public class AllTestSuite {
}
