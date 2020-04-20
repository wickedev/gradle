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

package org.gradle.jvm.toolchain;

import org.gradle.api.JavaVersion;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import javax.inject.Inject;

public class JvmToolchainRequirements {

    private Property<JvmVendor> requireVendor;
    private Property<JavaVersion> requireMinVersion;
    private Property<JavaVersion> requireMaxVersion;

    @Inject
    public JvmToolchainRequirements(ObjectFactory objects) {
        requireVendor = objects.property(JvmVendor.class).convention(JvmVendor.Any);
        requireMinVersion = objects.property(JavaVersion.class).convention(JavaVersion.VERSION_1_1);
        requireMaxVersion = objects.property(JavaVersion.class).convention(JavaVersion.VERSION_HIGHER);
    }

    @Input
    public Property<JvmVendor> getRequireVendor() {
        return requireVendor;
    }

    @Input
    public Property<JavaVersion> getRequireMinVersion() {
        return requireMinVersion;
    }

    @Input
    public Property<JavaVersion> getRequireMaxVersion() {
        return requireMaxVersion;
    }

    @Override
    public String toString() {
        return "JvmToolchainSelection{" +
            "requireVendor=" + requireVendor.get() +
            ", requireMinVersion=" + requireMinVersion.get() +
            ", requireMaxVersion=" + requireMaxVersion.get() +
            '}';
    }

}
