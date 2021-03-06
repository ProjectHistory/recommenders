/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models.rcp;

import static org.eclipse.jdt.core.IJavaElement.JAVA_PROJECT;
import static org.eclipse.recommenders.internal.models.rcp.Dependencies.createDependencyInfoForProject;
import static org.eclipse.recommenders.internal.models.rcp.Dependencies.createJREDependencyInfo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.models.rcp.Dependencies;
import org.eclipse.recommenders.internal.models.rcp.EclipseDependencyListener;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.rcp.JavaModelEvents.JarPackageFragmentRootAdded;
import org.eclipse.recommenders.rcp.JavaModelEvents.JarPackageFragmentRootRemoved;
import org.eclipse.recommenders.rcp.JavaModelEvents.JavaProjectClosed;
import org.eclipse.recommenders.rcp.JavaModelEvents.JavaProjectOpened;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class EclipseDependencyListenerTest {

    private static final String PROJECT_NAME = "TestProject";

    private static int projectNumber = 0;

    private File jarFileExample;
    private EventBus eventBus;
    private EclipseDependencyListener sut;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() {
        Dependencies.workspace = InjectionService.getInstance().getInjector().getInstance(IWorkspaceRoot.class);
    }

    @Before
    public void before() throws IOException {
        jarFileExample = temporaryFolder.newFile("example.jar");
    }

    private static String generateProjectName() {
        projectNumber++;
        return PROJECT_NAME + projectNumber;
    }

    private IJavaProject createProject() throws Exception {
        return createProject(generateProjectName());
    }

    private IJavaProject createProject(final String projectName) throws Exception {
        IWorkspaceRoot workspaceRoot = Dependencies.workspace;
        IProject project = workspaceRoot.getProject(projectName);
        project.create(null);
        project.open(null);
        IProjectDescription description = project.getDescription();
        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        project.setDescription(description, null);
        IJavaProject javaProject = JavaCore.create(project);

        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        entries.add(JavaRuntime.getDefaultJREContainerEntry());
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
        IFolder sourceFolder = project.getFolder("src");
        sourceFolder.create(false, true, null);

        IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(sourceFolder);
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
        javaProject.setRawClasspath(newEntries, null);

        javaProject.open(null);
        return javaProject;
    }

    private JarPackageFragmentRoot mockJarPackageFragmentRoot(final IJavaProject javaProject, final File file) {
        JarPackageFragmentRoot mock = mock(JarPackageFragmentRoot.class, RETURNS_DEEP_STUBS);
        when(mock.getParent()).thenReturn(javaProject);
        when(mock.getPath().toFile()).thenReturn(file);
        when(mock.getAncestor(JAVA_PROJECT)).thenReturn(javaProject);
        when(mock.isExternal()).thenReturn(true);
        return mock;
    }

    @Before
    public void init() throws IOException {
        eventBus = new EventBus("org.eclipse.recommenders.tests.models.rcp");
        sut = new EclipseDependencyListener(eventBus);
    }

    @Test
    public void testInitialWorkspaceParsing() throws Exception {
        String projectName = generateProjectName();
        IJavaProject javaProject = createProject(projectName);

        EclipseDependencyListener sut = new EclipseDependencyListener(new EventBus(""));
        DependencyInfo expected = Dependencies.createDependencyInfoForProject(javaProject);

        assertTrue(sut.getDependencies().contains(expected));
    }

    @Test
    public void testProjectIsAddedCorrect() throws Exception {
        String projectName = generateProjectName();
        IJavaProject javaProject = createProject(projectName);
        eventBus.post(new JavaProjectOpened(javaProject));

        DependencyInfo expected = Dependencies.createDependencyInfoForProject(javaProject);

        assertTrue(sut.getDependencies().contains(expected));
    }

    @Test
    public void testProjectIsRemovedCorrectAfterClosing() throws Exception {
        String projectName = generateProjectName();
        IJavaProject javaProject = createProject(projectName);
        eventBus.post(new JavaProjectOpened(javaProject));
        eventBus.post(new JavaProjectClosed(javaProject));

        DependencyInfo notExpected = Dependencies.createDependencyInfoForProject(javaProject);

        assertFalse(sut.getDependencies().contains(notExpected));
    }

    @Test
    public void testDependencyForSpecificProjectIsASubsetOfAllDependencies() throws Exception {
        String projectName = generateProjectName();
        IJavaProject javaProject = createProject(projectName);
        eventBus.post(new JavaProjectOpened(javaProject));
        assertTrue(sut.getDependencies().containsAll(
                sut.getDependenciesForProject(createDependencyInfoForProject(javaProject))));
    }

    @Test
    public void testProjectDependencyIsAddedCorrect() throws Exception {
        String projectName = generateProjectName();
        IJavaProject javaProject = createProject(projectName);
        eventBus.post(new JavaProjectOpened(javaProject));

        DependencyInfo projectDependencyInfo = createDependencyInfoForProject(javaProject);

        assertTrue(sut.getDependenciesForProject(projectDependencyInfo).contains(projectDependencyInfo));
    }

    @Test
    public void testJREDependencyIsAddedCorrect() throws Exception {
        String projectName = generateProjectName();
        IJavaProject javaProject = createProject(projectName);
        eventBus.post(new JavaProjectOpened(javaProject));

        DependencyInfo projectDependencyInfo = createDependencyInfoForProject(javaProject);
        Optional<DependencyInfo> jreDependencyInfo = createJREDependencyInfo(javaProject);

        if (jreDependencyInfo.isPresent()) {
            assertTrue(sut.getDependenciesForProject(projectDependencyInfo).contains(jreDependencyInfo.get()));
        } else {
            fail();
        }
    }

    @Test
    public void testDependenciesAreDeletedWhenProjectIsClosed() throws Exception {
        String projectName = generateProjectName();
        IJavaProject javaProject = createProject(projectName);
        DependencyInfo projectDependencyInfo = createDependencyInfoForProject(javaProject);

        eventBus.post(new JavaProjectOpened(javaProject));
        assertFalse(sut.getDependenciesForProject(projectDependencyInfo).isEmpty());
        eventBus.post(new JavaProjectClosed(javaProject));
        assertTrue(sut.getDependenciesForProject(projectDependencyInfo).isEmpty());
    }

    @Test
    public void testJREDependenciesAreDeletedWhenOneJARFromJREIsRemoved() throws Exception {
        String projectName = generateProjectName();
        IJavaProject javaProject = createProject(projectName);
        DependencyInfo projectDependencyInfo = createDependencyInfoForProject(javaProject);
        eventBus.post(new JavaProjectOpened(javaProject));

        Optional<DependencyInfo> optionalExpectedJREDependencyInfo = createJREDependencyInfo(javaProject);
        if (!optionalExpectedJREDependencyInfo.isPresent()) {
            fail();
        }

        DependencyInfo expectedJREDependencyInfo = createJREDependencyInfo(javaProject).get();
        assertTrue(sut.getDependenciesForProject(projectDependencyInfo).contains(expectedJREDependencyInfo));

        Set<IPackageFragmentRoot> detectJREPackageFragementRoots = EclipseDependencyListener
                .detectJREPackageFragementRoots(javaProject);
        for (IPackageFragmentRoot packageFragmentRoot : detectJREPackageFragementRoots) {
            if (packageFragmentRoot instanceof JarPackageFragmentRoot) {
                eventBus.post(new JarPackageFragmentRootRemoved((JarPackageFragmentRoot) packageFragmentRoot));
                break;
            }
        }

        assertFalse(sut.getDependenciesForProject(projectDependencyInfo).contains(expectedJREDependencyInfo));
    }

    @Test
    public void testDependencyForJarIsAddedCorrect() throws Exception {
        IJavaProject javaProject = createProject();
        DependencyInfo projectDependencyInfo = createDependencyInfoForProject(javaProject);

        eventBus.post(new JarPackageFragmentRootAdded(mockJarPackageFragmentRoot(javaProject, jarFileExample)));
        DependencyInfo expected = new DependencyInfo(jarFileExample, DependencyType.JAR);

        assertTrue(sut.getDependenciesForProject(projectDependencyInfo).contains(expected));
    }

    @Test
    public void testDependencyForJarIsRemovedCorrect() throws Exception {
        IJavaProject javaProject = createProject();
        DependencyInfo projectDependencyInfo = createDependencyInfoForProject(javaProject);

        eventBus.post(new JarPackageFragmentRootAdded(mockJarPackageFragmentRoot(javaProject, jarFileExample)));
        DependencyInfo expected = new DependencyInfo(jarFileExample, DependencyType.JAR);
        eventBus.post(new JarPackageFragmentRootRemoved(mockJarPackageFragmentRoot(javaProject, jarFileExample)));

        assertFalse(sut.getDependenciesForProject(projectDependencyInfo).contains(expected));
    }

}
