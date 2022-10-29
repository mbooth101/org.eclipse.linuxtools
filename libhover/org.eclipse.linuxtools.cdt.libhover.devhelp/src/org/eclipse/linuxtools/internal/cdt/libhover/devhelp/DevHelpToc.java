/*******************************************************************************
 * Copyright (c) 2012, 2022 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class DevHelpToc implements IToc {

    @Override
    public String getLabel() {
        return "Devhelp Documentation";
    }

    @Override
    public String getHref() {
        return null;
    }

    @Override
    public boolean isEnabled(IEvaluationContext context) {
        return true;
    }

    @Override
    public IUAElement[] getChildren() {
        return getTopics();
    }

    protected String[] getDevHelpDirs() {
        IPreferenceStore ps = new ScopedPreferenceStore(InstanceScope.INSTANCE,
                FrameworkUtil.getBundle(getClass()).getSymbolicName());
        return ps.getString(PreferenceConstants.DEVHELP_DIRECTORY).split(File.pathSeparator);
    }

    @Override
    public ITopic[] getTopics() {
        List<ITopic> topics = new ArrayList<>();
        // Find all *.devhelp2 files in the set of paths from preferences, create a
        // topic for each one
        for (String p : getDevHelpDirs()) {
            try (Stream<Path> htmlDirs = Files.walk(Path.of(p), 1)) {
                List<DevHelpTopic> devhelpTopics = htmlDirs.map(dir -> {
                    return dir.resolve(dir.getFileName() + ".devhelp2"); //$NON-NLS-1$
                }).filter(Files::exists).map(DevHelpTopic::new).toList();
                topics.addAll(devhelpTopics);
            } catch (IOException e) {
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                Platform.getLog(bundle).warn(MessageFormat.format("Unable to search Devhelp directory '{0}'", p), e);
            }
        }
        topics.sort((o1, o2) -> o1.getLabel().compareToIgnoreCase(o2.getLabel()));
        return topics.toArray(new ITopic[topics.size()]);
    }

    @Override
    public ITopic getTopic(String href) {
        return null;
    }
}
