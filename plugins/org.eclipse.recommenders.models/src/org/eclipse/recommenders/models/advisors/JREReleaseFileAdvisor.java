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
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.models.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.IOUtils;

import com.google.common.base.Optional;

public class JREReleaseFileAdvisor extends AbstractProjectCoordinateAdvisor {

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        Optional<FileInputStream> optionalReleaseFileInputStream = readReleaseFileIn(dependencyInfo.getFile());
        if (!optionalReleaseFileInputStream.isPresent()) {
            return absent();
        }
        InputStream releaseFileInputStream = optionalReleaseFileInputStream.get();
        try {
            return extractProjectCoordinateOfReleaseFile(releaseFileInputStream);
        } catch (IOException e) {
            return absent();
        } finally {
            IOUtils.closeQuietly(releaseFileInputStream);
        }
    }

    private Optional<ProjectCoordinate> extractProjectCoordinateOfReleaseFile(InputStream releaseFileInputStream)
            throws IOException {
        Properties properties = new Properties();
        properties.load(releaseFileInputStream);
        String version = properties.getProperty("JAVA_VERSION");
        if (version == null) {
            return absent();
        }
        // Replace of " is needed because of the release file structure
        version = version.replace("\"", "");
        return tryNewProjectCoordinate("jre", "jre", canonicalizeVersion(version));
    }

    private Optional<FileInputStream> readReleaseFileIn(File folderPath) {
        try {
            File file = new File(folderPath.getAbsolutePath() + File.separator + "release");
            return fromNullable(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return absent();
        }
    }

    @Override
    public boolean isApplicable(DependencyType dependencyType) {
        return dependencyType == DependencyType.JRE;
    }

}
