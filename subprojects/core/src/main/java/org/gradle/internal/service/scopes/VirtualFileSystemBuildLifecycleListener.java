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

package org.gradle.internal.service.scopes;

import com.google.common.collect.ImmutableList;
import org.gradle.StartParameter;
import org.gradle.api.internal.GradleInternal;
import org.gradle.initialization.RootBuildLifecycleListener;
import org.gradle.internal.watch.vfs.WatchingAwareVirtualFileSystem;
import org.gradle.util.IncubationLogger;

class VirtualFileSystemBuildLifecycleListener implements RootBuildLifecycleListener {
    public interface StartParameterSwitch {
        boolean isEnabled(StartParameter startParameter);
    }

    private final WatchingAwareVirtualFileSystem virtualFileSystem;
    private final StartParameterSwitch dropVfs;

    public VirtualFileSystemBuildLifecycleListener(
        WatchingAwareVirtualFileSystem virtualFileSystem,
        StartParameterSwitch dropVfs
    ) {
        this.virtualFileSystem = virtualFileSystem;
        this.dropVfs = dropVfs;
    }

    @Override
    public void afterStart(GradleInternal gradle) {
        StartParameter startParameter = gradle.getStartParameter();
        boolean watchFileSystem = startParameter.isWatchFileSystem();
        if (watchFileSystem) {
            IncubationLogger.incubatingFeatureUsed("Watching the file system");
            if (dropVfs.isEnabled(startParameter)) {
                virtualFileSystem.invalidateAll();
            }
        }
        virtualFileSystem.afterBuildStarted(watchFileSystem);
        gradle.settingsEvaluated(settings -> virtualFileSystem.updateMustWatchDirectories(ImmutableList.of(settings.getRootDir())));
    }

    @Override
    public void beforeComplete(GradleInternal gradle) {
        virtualFileSystem.beforeBuildFinished(gradle.getStartParameter().isWatchFileSystem());
    }
}
