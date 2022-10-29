/*******************************************************************************
 * Copyright (c) 2011, 2022 Red Hat Inc. and others.
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class DevHelpContentProducer implements IHelpContentProducer {

    @Override
    public InputStream getInputStream(String pluginID, String href, Locale locale) {
        InputStream stream = null;
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        try {
            URI uri = new URI(href);

            IPreferenceStore ps = new ScopedPreferenceStore(InstanceScope.INSTANCE, bundle.getSymbolicName());
            String[] paths = ps.getString(PreferenceConstants.DEVHELP_DIRECTORY).split(File.pathSeparator);
            Optional<Path> doc = Stream.of(paths).map(p -> Path.of(p, uri.getPath())).filter(Files::exists).findFirst();
            if (doc.isPresent()) {
                stream = Files.newInputStream(doc.get());
            }
        } catch (URISyntaxException | IOException e) {
            Platform.getLog(bundle)
                    .warn(MessageFormat.format("Unable to retrieve Devhelp document for URI '{0}'", href), e);
        }
        return stream;
    }
}
