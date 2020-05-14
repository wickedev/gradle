/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.initialization;

import org.gradle.api.Project;
import org.gradle.api.internal.properties.GradleProperties;
import org.gradle.initialization.layout.BuildLayout;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Map;

public class DefaultGradlePropertiesController implements GradlePropertiesController {

    private State state;
    private final GradleProperties sharedGradleProperties = new SharedGradleProperties();
    private final IGradlePropertiesLoader propertiesLoader;

    public DefaultGradlePropertiesController(BuildLayout buildLayout, IGradlePropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
        this.state = new NotLoaded(propertiesLoader.loadGradlePropertiesFiles(buildLayout.getSettingsDir()));
    }

    @Override
    public GradleProperties getGradleProperties() {
        return sharedGradleProperties;
    }

    @Override
    public void loadGradlePropertiesFrom(File settingsDir) {
        state = state.loadGradlePropertiesFrom(settingsDir);
    }

    private class SharedGradleProperties implements GradleProperties {

        @Nullable
        @Override
        public String find(String propertyName) {
            return gradleProperties().find(propertyName);
        }

        @Override
        public Map<String, String> mergeProperties(Map<String, String> properties) {
            return gradleProperties().mergeProperties(properties);
        }

        private GradleProperties gradleProperties() {
            return state.gradleProperties();
        }
    }

    private interface State {

        GradleProperties gradleProperties();

        State loadGradlePropertiesFrom(File settingsDir);
    }

    private class NotLoaded implements State {

        private final GradleProperties loadedGradlePropertiesFiles;

        public NotLoaded(GradleProperties loadedGradlePropertiesFiles) {
            this.loadedGradlePropertiesFiles = loadedGradlePropertiesFiles;
        }

        @Override
        public GradleProperties gradleProperties() {
            return loadedGradlePropertiesFiles;
        }

        @Override
        public State loadGradlePropertiesFrom(File settingsDir) {
            for (Map.Entry<String, String> entry : loadedGradlePropertiesFiles.mergeProperties(Collections.emptyMap()).entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(Project.SYSTEM_PROP_PREFIX + '.')) {
                    System.setProperty(key.substring((Project.SYSTEM_PROP_PREFIX + '.').length()), entry.getValue());
                }

            }
            return new Loaded(
                loadedGradlePropertiesFiles,
                settingsDir
            );
        }
    }

    private static class Loaded implements State {

        private final GradleProperties gradleProperties;
        private final File propertiesDir;

        public Loaded(GradleProperties gradleProperties, File propertiesDir) {
            this.gradleProperties = gradleProperties;
            this.propertiesDir = propertiesDir;
        }

        @Override
        public GradleProperties gradleProperties() {
            return gradleProperties;
        }

        @Override
        public State loadGradlePropertiesFrom(File settingsDir) {
            if (!propertiesDir.equals(settingsDir)) {
                throw new IllegalStateException(
                    String.format(
                        "GradleProperties has already been loaded from '%s' and cannot be loaded from '%s'.",
                        propertiesDir, settingsDir
                    )
                );
            }
            return this;
        }
    }
}
