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

package org.gradle.api;

import org.gradle.api.internal.tasks.JavaToolChainFactory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.jvm.internal.toolchain.JavaToolChainInternal;
import org.gradle.jvm.toolchain.JvmToolchainRequirements;

import javax.inject.Inject;
import java.io.File;

public class JvmToolchainResolver {
    ObjectFactory objectFactory;
    private final ProviderFactory providerFactory;
    JavaToolChainFactory toolChainFactory;

    @Inject
    public JvmToolchainResolver(ObjectFactory objectFactory, ProviderFactory providerFactory, JavaToolChainFactory toolChainFactory) {
        this.objectFactory = objectFactory;
        this.providerFactory = providerFactory;
        this.toolChainFactory = toolChainFactory;
    }

    public Provider<JavaToolChainInternal> resolve(JvmToolchainRequirements toolchainSelection) {
        return providerFactory.provider(() -> {
            JavaToolChainInternal selectedToolchain = null;
            selectedToolchain = getRunningToolchain();

            if (!toolchainSelection.getRequireMinVersion().isPresent()) {
                System.out.println("no toolchains configured, using default JVM: " + selectedToolchain.getName() + "-" + selectedToolchain.getJavaVersion());
                return selectedToolchain;
            }

            if (!matches(toolchainSelection, selectedToolchain)) {
                System.out.println("== JVM running Gradle (" +
                    selectedToolchain.getName() + "-" + selectedToolchain.getJavaVersion() +
                    ") is not compatible with toolchain requirements (" +
                    toolchainSelection +
                    ")");
                selectedToolchain = null;
            }

            selectedToolchain = getConfiguredToolchain();
            if (!matches(toolchainSelection, selectedToolchain)) {
                System.out.println("== Toolchain (" +
                    selectedToolchain.getName() + "-" + selectedToolchain.getJavaVersion() +
                    ") is not compatible with toolchain requirements (" +
                    toolchainSelection +
                    ")");
                selectedToolchain = null;
            }
            return selectedToolchain;
        });
    }

    private boolean matches(JvmToolchainRequirements selection, JavaToolChainInternal toolchain) {
        return toolchain.getJavaVersion().isCompatibleWith(selection.getRequireMinVersion().get());
    }

    // TODO: factory currently does matching, merge Matcher and Factory or clarify responsbilities
    private JavaToolChainInternal getRunningToolchain() {
        final CompileOptions options = objectFactory.newInstance(CompileOptions.class);
        options.setFork(false);
        return (JavaToolChainInternal) toolChainFactory.forCompileOptions(options);
    }

    private JavaToolChainInternal getConfiguredToolchain() {
        final CompileOptions options = objectFactory.newInstance(CompileOptions.class);
        options.setFork(true);
        options.getForkOptions().setJavaHome(new File(System.getProperty("org.gradle.jvm.toolchains")));
        return (JavaToolChainInternal) toolChainFactory.forCompileOptions(options);
    }
}
