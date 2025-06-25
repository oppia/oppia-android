package org.oppia.android.scripts.lint

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.absolute
import com.android.tools.lint.model.LintModelMavenName
import com.android.tools.lint.model.LintModelModuleType.APP
import com.android.tools.lint.model.LintModelModuleType.LIBRARY
import org.oppia.android.scripts.common.AndroidBuildSdkProperties

class LintModelCreator {

  fun generateModelFiles(
    modelsDir: File,
    repoRoot: File,
    sdkProperties: AndroidBuildSdkProperties,
    moduleConfig: ModuleConfig
  ): File {
    val modelPath = modelsDir.toPath().createDirectories()
    val lintModelXml = modelPath.resolve("module.xml")
    val buildDir = modelPath.resolve("build").createDirectories()

    // Calculate relative path from models dir to repo root
    val relativeProjectPath = modelPath.absolute().relativize(repoRoot.toPath().absolute())

    // Generate module.xml
    generateModuleXml(lintModelXml.toFile(), moduleConfig, relativeProjectPath, buildDir,sdkProperties.buildToolsVersion)

    // Generate variant XML (main.xml)
    val variantFile = modelsDir.resolve("main.xml")
    generateVariantXml(variantFile, moduleConfig, buildDir)

    // Generate artifact libraries XML
    val artifactLibrariesFile = modelsDir.resolve("main-artifact-libraries.xml")
    generateArtifactLibrariesXml(artifactLibrariesFile)

    // Generate dependencies XML
    val dependenciesFile = modelsDir.resolve("main-artifact-dependencies.xml")
    generateDependenciesXml(dependenciesFile)

    return modelsDir
  }

  private fun generateModuleXml(
    moduleFile: File,
    moduleConfig: ModuleConfig,
    relativeProjectPath: Path,
    buildDir: Path,
    buildToolsVersion:String
  ) {
    val lintModuleType = when {
      library -> LIBRARY
      else -> APP
    }
    val content = StringBuilder().apply {
      appendLine("<lint-module")
      appendLine("   dir=\"$relativeProjectPath\"")
      appendLine("   name=\"${moduleConfig.name}\"")
      appendLine("   type=\"${lintModuleType.name}\"")
      appendLine("   maven=\"${LintModelMavenName.NON_MAVEN}\"")
      appendLine("   buildFolder=\"${buildDir.toFile()}\"")
      appendLine("   compileTarget=\"${buildToolsVersion}\"")
      appendLine("   neverShrinking=\"true\">")
      appendLine("   <lintOptions />")
      appendLine("   <variant name=\"main\"/>")
      appendLine("</lint-module>")
    }.toString()

    moduleFile.writeText(content)
  }

  private fun generateVariantXml(
    variantFile: File,
    moduleConfig: ModuleConfig,
    buildDir: Path
  ) {
    val content = StringBuilder().apply {
      appendLine("<variant")
      appendLine("    name=\"main\"")

      appendLine("    minSdkVersion=\"21\"")
      appendLine("    targetSdkVersion=\"34\"")


      appendLine("    debuggable=\"true\"")
      if (moduleConfig.isAndroid) {
        appendLine("    useSupportLibraryVectorDrawables=\"true\"")
      }

      // Add package name if available (derive from manifest or use default)
      val packageName = extractPackageNameFromManifest(moduleConfig.manifestFile)
        ?: "org.oppia.android.${moduleConfig.name}"
      appendLine("    package=\"$packageName\"")
      appendLine(">")
      appendLine("    <buildFeatures />")
      appendLine("    <sourceProviders>")

      // Add source provider
      generateSourceProvider(this, moduleConfig)

      appendLine("    </sourceProviders>")
      appendLine("    <artifact")
      appendLine("      type=\"MAIN\"")
      appendLine("      classOutputs=\"${buildDir.resolve("classes").createDirectories()}\"")
      appendLine("      applicationId=\"$packageName\">")
      appendLine("    </artifact>")
      appendLine("</variant>")
    }.toString()

    variantFile.writeText(content)
  }

  private fun generateSourceProvider(
    builder: StringBuilder,
    moduleConfig: ModuleConfig
  ) {
    builder.apply {
      appendLine("        <sourceProvider")

      if (moduleConfig.manifestFile.isNotEmpty()) {
        appendLine("            manifest=\"${moduleConfig.manifestFile}\"")
      }

      val javaDirs = getCommonParentDirs(moduleConfig.srcFiles)
      if (javaDirs.isNotEmpty()) {
        appendLine("            javaDirectories=\"${javaDirs.joinToString(",")}\"")
      }

      val testDirs = getCommonParentDirs(moduleConfig.testFiles)
      if (testDirs.isNotEmpty()) {
        appendLine("            testDirectories=\"${testDirs.joinToString(",")}\"")
      }

      val resDirs = moduleConfig.resourceDirs
      if (resDirs.isNotEmpty()) {
        appendLine("            resDirectories=\"${resDirs.joinToString(",")}\"")
      }

      appendLine("        />")
    }
  }

  private fun generateArtifactLibrariesXml(
    librariesFile: File
  ) {
    val content = StringBuilder().apply {
      appendLine("<libraries>")
      appendLine("</libraries>")
    }.toString()

    librariesFile.writeText(content)
  }

  private fun generateDependenciesXml(
    dependenciesFile: File
  ) {
    val content = StringBuilder().apply {
      appendLine("<dependencies>")
      appendLine("</dependencies>")
    }.toString()

    dependenciesFile.writeText(content)
  }

  private fun getCommonParentDirs(files: List<String>): List<String> {
    if (files.isEmpty()) return emptyList()

    return files.map { filePath ->
      val path = Paths.get(filePath)
      path.parent?.toString() ?: ""
    }.distinct().filter { it.isNotEmpty() }
  }

  private fun extractPackageNameFromManifest(manifestPath: String): String? {
    if (manifestPath.isEmpty()) return null

    return try {
      val manifestFile = File(manifestPath)
      if (!manifestFile.exists()) return null

      val content = manifestFile.readText()
      val packageRegex = Regex("""package\s*=\s*["']([^"']+)["']""")
      packageRegex.find(content)?.groupValues?.get(1)
    } catch (e: Exception) {
      null
    }
  }
}
