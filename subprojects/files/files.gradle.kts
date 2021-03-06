/*
 * Copyright 2019 the original author or authors.
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
plugins {
    gradlebuild.distribution.`core-api-java`
    gradlebuild.`publish-public-libraries`
}

description = "Base tools to work with files"

gradlebuildJava.usedInWorkers()

dependencies {
    implementation(project(":baseAnnotations"))
    implementation(library("guava")) { version { require(libraryVersion("guava")) } }
    implementation(library("slf4j_api")) { version { require(libraryVersion("slf4j_api")) } }

    testImplementation(project(":native"))
    testImplementation(project(":baseServices")) {
        because("TextUtil is needed")
    }
    testImplementation(testFixtures(project(":native")))
}
