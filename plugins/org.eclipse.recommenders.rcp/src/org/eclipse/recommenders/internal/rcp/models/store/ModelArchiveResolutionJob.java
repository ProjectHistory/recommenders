/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.models.store;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.core.runtime.Status.CANCEL_STATUS;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus.RESOLVED;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("rawtypes")
public class ModelArchiveResolutionJob extends Job {

    private Logger log = LoggerFactory.getLogger(getClass());
    private final ModelArchiveMetadata metadata;
    private final IClasspathEntryInfoProvider cpeInfos;
    private final IModelRepository repository;

    private ClasspathEntryInfo cpeInfo;
    private File pkgRoot;
    private final IModelRepositoryIndex index;
    private final String classifier;
    private Artifact model;

    @Inject
    public ModelArchiveResolutionJob(@Assisted ModelArchiveMetadata metadata,
            IClasspathEntryInfoProvider cpeInfoProvider, IModelRepository repository, IModelRepositoryIndex index,
            @Assisted String classifier) {
        super("Resolving model for " + metadata.getLocation().getName() + "...");
        this.metadata = metadata;
        this.cpeInfos = cpeInfoProvider;
        this.repository = repository;
        this.index = index;
        this.classifier = classifier;
    }

    @Override
    @VisibleForTesting
    public IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(String.format("Looking for '%s' model for %s", classifier, metadata.getLocation().getName()),
                5);
        monitor.worked(1);
        metadata.setStatus(ModelArchiveResolutionStatus.UNRESOLVED);
        try {

            if (!findClasspathInfo()) {
                metadata.setError(format("No class path info available for '%s'. Skipped.", pkgRoot));
                return CANCEL_STATUS;
            }

            if (!findInIndex()) {
                metadata.setError(format(
                        "No call model found for '%s'. Neither fingerprint '%s' nor symbolic name '%s' are known.",
                        cpeInfo.getLocation(), cpeInfo.getFingerprint(), cpeInfo.getSymbolicName()));
                return CANCEL_STATUS;
            }
            monitor.worked(1);
            findBestMatchingLatestModel();
            monitor.worked(1);
            repository.resolve(model, monitor);
            monitor.worked(2);
            updateMetadata();
            return OK_STATUS;
        } catch (Exception x) {
            metadata.setStatus(ModelArchiveResolutionStatus.UNRESOLVED);
            metadata.setError(x.getMessage());
            return CANCEL_STATUS;
        } finally {
            monitor.done();
        }
    }

    private void updateMetadata() {
        metadata.setStatus(RESOLVED);
        metadata.setArtifact(model);
        metadata.setCoordinate(model.toString());
    }

    private boolean findClasspathInfo() {
        pkgRoot = metadata.getLocation();
        cpeInfo = cpeInfos.getInfo(pkgRoot).orNull();
        return cpeInfo != null;
    }

    private boolean findInIndex() {
        Optional<Artifact> tmp = Optional.absent();
        if (!isEmpty(cpeInfo.getFingerprint())) {
            tmp = index.searchByFingerprint(cpeInfo.getFingerprint(), classifier);
        }
        if (!tmp.isPresent() && !isEmpty(cpeInfo.getSymbolicName())) {
            tmp = index.searchByArtifactId(cpeInfo.getSymbolicName(), classifier);
        }
        model = tmp.orNull();
        return tmp.isPresent();
    }

    private void findBestMatchingLatestModel() {
        Version version = cpeInfo.getVersion();
        Artifact copy = model;
        Artifact query = null;
        String upperBound = version.isUnknown() ? "10000.0" : format("%d.%d", version.major, version.minor + 1);
        query = model.setVersion("[0," + upperBound + ")");
        copy = repository.findHigestVersion(query).orNull();
        if (copy == null) {
            query = model.setVersion("[" + upperBound + ",)");
            copy = repository.findLowestVersion(query).orNull();
        }
        if (copy != null) {
            model = copy;
        }
    }
}