import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.androidTest) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.composeJb) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.publish)
    alias(libs.plugins.dokka)
    id("build-logic") apply false
}

allprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = Versions.Java.jvmTarget
        }
    }

    apply(plugin = "com.diffplug.spotless")
    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/")
            ktlint(libs.versions.ktlint.get().toString())
        }
        kotlinGradle {
            target("**/*.gradle.kts")
            targetExclude("**/build/")
            ktlint(libs.versions.ktlint.get().toString())
        }
    }

    group = "io.github.qdsfdhvh"
    version = Versions.Project.version

    plugins.withId("com.vanniktech.maven.publish.base") {
        mavenPublishing {
            publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
            signAllPublications()
            @Suppress("UnstableApiUsage")
            pom {
                name.set("compose-imageLoader")
                description.set("Compose ImageLoader.")
                url.set("https://github.com/qdsfdhvh/compose-imageLoader")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("Seiko")
                        name.set("SeikoDes")
                        email.set("seiko_des@outlook.com")
                    }
                }
                scm {
                    url.set("https://github.com/qdsfdhvh/compose-imageLoader")
                    connection.set("scm:git:git://github.com/qdsfdhvh/compose-imageLoader.git")
                    developerConnection.set("scm:git:git://github.com/qdsfdhvh/compose-imageLoader.git")
                }
            }
        }
    }
}

tasks.dokkaHtmlMultiModule {
    moduleVersion.set(Versions.Project.version)
    outputDirectory.set(rootDir.resolve("docs/static/api"))
}

gradle.taskGraph.whenReady {
    if (project.hasProperty("noAppApple")) {
        allTasks.asSequence()
            .filter {
                it.path.startsWith(":app:ios-combine") ||
                    it.path.startsWith(":app:macos") ||
                    it.path.startsWith(":app:web")
            }
            .forEach {
                it.enabled = false
            }
        // TODO remove when this fix https://github.com/JetBrains/compose-multiplatform/issues/3135
        allTasks.asSequence()
            .filter {
                it.path in listOf(
                    ":image-loader:linkDebugTestIosSimulatorArm64",
                    ":image-loader:linkDebugTestIosArm64",
                    ":image-loader:linkDebugTestIosX64",
                    ":image-loader:linkDebugTestMacosArm64",
                    ":image-loader:linkDebugTestMacosX64",
                )
            }.forEach {
                it.enabled = false
            }
    }
}
