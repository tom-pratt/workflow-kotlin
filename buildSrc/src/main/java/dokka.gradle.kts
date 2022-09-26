import com.squareup.workflow1.library
import com.squareup.workflow1.libsCatalog
import java.net.URL
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.dokka")
}

tasks.withType<AbstractDokkaLeafTask> {

  // This is the displayed name for the module, like in the Html sidebar.
  //   artifact id: workflow-internal-testing-utils
  //          path: internal-testing-utils
  moduleName.set(
    provider {
      findProperty("POM_ARTIFACT_ID") as? String
        ?: project.path.removePrefix(":")
    }
  )

  dokkaSourceSets.configureEach {

    val dokkaSourceSet = this

    reportUndocumented.set(false)
    skipDeprecated.set(true)

    if (file("src/${dokkaSourceSet.name}").exists()) {

      val readmeFile = file("$projectDir/README.md")
      // If the module has a README, add it to the the module's index
      if (readmeFile.exists()) {
        includes.from(readmeFile)
      }

      sourceLink {
        localDirectory.set(file("src/${dokkaSourceSet.name}"))

        val modulePath = projectDir.relativeTo(rootDir).path

        // URL showing where the source code can be accessed through the web browser
        remoteUrl.set(
          URL("https://github.com/square/workflow-kotlin/blob/main/$modulePath/src/${dokkaSourceSet.name}")
        )
        // Suffix which is used to append the line number to the URL. Use #L for GitHub
        remoteLineSuffix.set("#L")
      }
    }
    perPackageOption {
      // Will match all .internal packages and sub-packages, regardless of module.
      matchingRegex.set(""".*\.internal.*""")
      suppress.set(true)
    }
  }
}


dependencies {
  dokkaPlugin(libsCatalog.library("dokka-versioning"))
}

val versionWithoutSnapshot = (project.property("VERSION_NAME") as String)
  .removeSuffix("-SNAPSHOT")


tasks.withType<org.jetbrains.dokka.gradle.AbstractDokkaTask> {

  pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
    version = versionWithoutSnapshot
    olderVersionsDir = rootDir.resolve("old-dokka")
    renderVersionsNavigationOnAllPages = true
  }
}

if (project == rootProject) {

  val disableOldDokkaAutoFormat by tasks.registering(com.squareup.workflow1.buildsrc.dokka.DisableOldDokkaAutoFormatTask::class) {
    // only run if the `/old-dokka` directory exists with sub-folders. Each version of Dokka docs would
    // have its own folder like ["/old-dokka/0.10.0", "/old-dokka/0.10.1"].
    enabled = rootDir.resolve("old-dokka")
      .listFiles()
      ?.any { it.isDirectory } == true
    editorConfig.set(rootProject.file("old-dokka/.editorconfig"))
  }
  tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(disableOldDokkaAutoFormat)
  }
  val syncDokkaToOldDokka by tasks.registering(Sync::class) {
    group = "dokka versioning"
    description =
      "sync the Dokka output for the current version to /old-dokka/$versionWithoutSnapshot"
    from(rootProject.buildDir.resolve("dokka/htmlMultiModule"))
    into(rootProject.file("old-dokka/$versionWithoutSnapshot"))

    enabled = versionWithoutSnapshot == project.property("VERSION_NAME")

    dependsOn("dokkaHtmlMultiModule", disableOldDokkaAutoFormat)
  }

  tasks.named("dokkaHtmlMultiModule") {
    finalizedBy(syncDokkaToOldDokka)
  }
}
