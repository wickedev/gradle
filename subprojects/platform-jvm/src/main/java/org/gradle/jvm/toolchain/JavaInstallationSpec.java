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

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public class JavaInstallationSpec {

    String name;
    Property<String> path;
    private ObjectFactory objects;

    public JavaInstallationSpec(String name, ObjectFactory objects) {
        this.name = name;
        this.path = objects.property(String.class);
        this.objects = objects;
    }

    @Input
    public String getName() {
        return name;
    }

    @Input
    public Property<String> getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "JavaInstallationSpec{" +
            "name='" + name + '\'' +
            ", path='" + path + '\'' +
            '}';
    }
}
