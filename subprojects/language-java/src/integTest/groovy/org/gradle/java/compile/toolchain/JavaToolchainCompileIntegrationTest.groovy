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

package org.gradle.java.compile.toolchain

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition

@Requires(TestPrecondition.JDK11_OR_EARLIER)
public class JavaToolchainCompileIntegrationTest extends AbstractIntegrationSpec {

    def setup() {
        settingsFile << "rootProject.name = 'consumer'\n"
        buildFile << """
                plugins {
                    id 'java-library'
                }
                group = 'org'
                version = '1.0'

                repositories {
                    ${mavenCentralRepository()}
                }

                dependencies {
                    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.4.0','org.junit.jupiter:junit-jupiter-engine:5.4.0'
                }

                def javaHome = "/Users/bmuskalla/.sdkman/candidates/java/13.0.2.hs-adpt"
            """
        propertiesFile << """
                systemProp.org.gradle.jvm.toolchains=/Users/bmuskalla/.sdkman/candidates/java/13.0.2.hs-adpt
            """.trim()
    }

    def "BEFORE: compiles a class using different JDK"() {
        given:
        buildFile << """
            tasks.withType(AbstractCompile) {
                options.with {
                    compilerArgs.addAll(['--release', '12'])
                    fork = true
                    forkOptions.javaHome = file(javaHome)
                }
            }
        """
        def file = file('src/main/java/toolchain/RequiresOtherJdk.java')
        file.text = """
            package toolchain;

            public class RequiresOtherJdk {

                public static void main(String[] args) throws Exception {
                    System.out.println("foooo".indent(3));
                }
            }
        """

        when:
        succeeds ':compileJava'

        then:
        javaClassFile('toolchain/RequiresOtherJdk.class').exists()
    }

    def "SPIKE: compiles a class using different JDK"() {
        given:
        buildFile << """
            java {
                toolchain {
                    requireVendor = JvmVendor.AdoptOpenJDK
                    requireMinVersion = JavaVersion.VERSION_13
                    requireMaxVersion = JavaVersion.VERSION_14
                }
            }
        """
        def file = file('src/main/java/toolchain/RequiresOtherJdk.java')
        file.text = """
            package toolchain;

            public class RequiresOtherJdk {

                public static void main(String[] args) throws Exception {
                    System.out.println("foooo".indent(3));
                }
            }
        """

        when:
        succeeds ':compileJava'

        then:
        javaClassFile('toolchain/RequiresOtherJdk.class').exists()
    }

    def "BEFORE: run test using different JDK"() {
        given:
        buildFile << """
            tasks.withType(Test) {
                useJUnitPlatform()
                executable = new File(javaHome, "bin/java")
            }

            tasks.withType(AbstractCompile) {
                options.with {
                    compilerArgs.addAll(['--release', '12'])
                    fork = true
                    forkOptions.javaHome = file(javaHome)
                }
            }
        """

        def file = file('src/test/java/toolchain/RequiresOtherJdkTest.java')
        file.text = """
            package toolchain;

            import org.junit.jupiter.api.Test;

            public class RequiresOtherJdkTest {
                @Test
                public void ok() {
                    System.out.println("foooo".indent(3));
                }
            }
        """

        expect:
        succeeds ':check'
    }

    def "SPIKE: run test using different JDK"() {
        given:
        buildFile << """
            java {
                toolchain {
                    requireMinVersion = JavaVersion.VERSION_12
                }
            }
        """

        def file = file('src/test/java/toolchain/RequiresOtherJdkTest.java')
        file.text = """
            package toolchain;

            import org.junit.jupiter.api.Test;

            public class RequiresOtherJdkTest {
                @Test
                public void ok() {
                    System.out.println("foooo".indent(3));
                }
            }
        """

        expect:
        succeeds ':check'
    }

}
