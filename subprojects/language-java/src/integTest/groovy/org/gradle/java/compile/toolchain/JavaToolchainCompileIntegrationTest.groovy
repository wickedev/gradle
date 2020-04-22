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
                    forkOptions.javaHome = new File('/Users/bmuskalla/.sdkman/candidates/java/13.0.2.hs-adpt')
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

    def "SPIKE 2: compiles a class using different JDK"() {
        given:
        buildFile << """
            java {
                javaInstallations {
                   create("jdk13") {
                      path.set "/Users/bmuskalla/.sdkman/candidates/java/13.0.2.hs-adpt"
                   }
                }
            }

            tasks.withType(AbstractCompile) {
                installation = java.javaInstallations.getByName("jdk13");
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
}
