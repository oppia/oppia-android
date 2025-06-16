package org.oppia.android.scripts.lint

import com.android.SdkConstants
import org.oppia.android.scripts.common.BazelClient
import java.io.File
import java.io.IOException
import java.lang.Module
import java.lang.ModuleLayer
import java.util.zip.ZipFile

/**
 * Represents module configuration for lint project description.
 */
data class ModuleConfig(
  val name: String,
  val isAndroid: Boolean,
  val isLibrary: Boolean,
  val compileSdkVersion: String,
  val kotlinLanguageVersion: String,
  val srcDirs: List<String>,
  val testDirs: List<String>,
  val resourceDirs: List<String>,
  val manifestFile: String? = null,
  val dependencies: List<String>,
  val aarFiles: List<AarFileInfo>,
  val jarFiles: List<String>,
  val lintModelDir: File? = null
)

/**
 * Information about an AAR file and its extraction location.
 */
data class AarFileInfo(
  val originalPath: String,
  val extractedPath: String
)

/**
 * Generates lint project description XML files for Android projects.
 *
 * @param repoRoot The root directory of the repository
 * @param workingDirectory The working directory where files will be generated
 */
class LintProjectDescription(
  private val repoRoot: File,
  private val workingDirectory: File,
  private val bazelClient: BazelClient
) {

  companion object {
    private const val LINT_PROJECT_DESCRIPTION_FILE_NAME = "lint-project-description.xml"
    private const val LINT_CACHE_DIRECTORY_FILE_NAME = "lint-cache-directory"
    private const val LINT_JDK_DIRECTORY_NAME = "jdk-home"
    private const val EXTRACTED_AARS_DIRECTORY_NAME = "extracted-aars"
    private const val COMPILE_SDK_VERSION = "34"
    private const val KOTLIN_LANGUAGE_VERSION = "1.6"
    private const val APPLICATION_MODULE = "app"
    private val LIBRARY_MODULES = listOf("domain", "testing", "utility", "data")
    private val MODULES_WITH_MAIN_RES = setOf("app", "utility")
    private val MODULES_WITH_TEST_RES = setOf("utility")
  }

  /**
   * Generates the lint project description XML file.
   *
   * @return The generated project description file
   * @throws IOException if file operations fail
   * @throws IllegalStateException if ANDROID_HOME is not set
   */
  fun generateProjectDescriptionXml(): File {
    val projectDescriptionFile = File(workingDirectory, LINT_PROJECT_DESCRIPTION_FILE_NAME)
    val cacheDirectory =
      ensureDirectoryExists(File(workingDirectory, LINT_CACHE_DIRECTORY_FILE_NAME))
    val sdkPath = getAndroidSdkPath()
    val jdkPath = File(workingDirectory, LINT_JDK_DIRECTORY_NAME)
    val extractedAarsDirectory =
      ensureDirectoryExists(File(workingDirectory, EXTRACTED_AARS_DIRECTORY_NAME))

    prepareJdk(jdkPath)

    val moduleConfigs = createModuleConfigs(extractedAarsDirectory)
    val content = generateXmlContent(cacheDirectory, jdkPath.absolutePath, sdkPath, moduleConfigs)

    return writeProjectDescriptionFile(projectDescriptionFile, content)
  }

  private fun createModuleConfigs(extractedAarsDirectory: File): List<ModuleConfig> {
    val modules = mutableListOf<ModuleConfig>()

    modules.add(createModuleConfig(APPLICATION_MODULE, false, extractedAarsDirectory))

    LIBRARY_MODULES.forEach { module ->
      modules.add(createModuleConfig(module, true, extractedAarsDirectory))
    }

    return modules
  }

  private fun createModuleConfig(
    moduleName: String,
    isLibrary: Boolean,
    extractedAarsDirectory: File
  ): ModuleConfig {
    val srcDirs = mutableListOf<String>()
    val testDirs = mutableListOf<String>()
    val resourceDirs = mutableListOf<String>()

    addDirectoryIfExists(File(repoRoot, "$moduleName/src/main/java"), srcDirs)
    addDirectoryIfExists(File(repoRoot, "$moduleName/src/test/java"), testDirs)

    if (moduleName == APPLICATION_MODULE) {
      addDirectoryIfExists(File(repoRoot, "$moduleName/src/sharedTest/java"), testDirs)
    }

    if (MODULES_WITH_MAIN_RES.contains(moduleName)) {
      addDirectoryIfExists(File(repoRoot, "$moduleName/src/main/res"), resourceDirs)
    }

    if (MODULES_WITH_TEST_RES.contains(moduleName)) {
      addDirectoryIfExists(File(repoRoot, "$moduleName/src/test/res"), resourceDirs)
    }

    val manifestFile = findManifestFile(moduleName)

    val dependencies = if (moduleName == APPLICATION_MODULE) LIBRARY_MODULES else emptyList()

    val aarFiles = getAarFilesForModule(moduleName, extractedAarsDirectory)
    val jarFiles = getJarFilesForModule(moduleName)

    return ModuleConfig(
      name = moduleName,
      isAndroid = true,
      isLibrary = isLibrary,
      compileSdkVersion = COMPILE_SDK_VERSION,
      kotlinLanguageVersion = KOTLIN_LANGUAGE_VERSION,
      srcDirs = srcDirs,
      testDirs = testDirs,
      resourceDirs = resourceDirs,
      manifestFile = manifestFile,
      dependencies = dependencies,
      aarFiles = aarFiles,
      jarFiles = jarFiles
    )
  }

  private fun addDirectoryIfExists(directory: File, targetList: MutableList<String>) {
    if (directory.exists() && directory.isDirectory) {
      targetList.add(directory.absolutePath)
    } else {
      throw IllegalStateException("Directory does not exist: ${directory.absolutePath}")
    }
  }

  private fun findManifestFile(moduleName: String): String? {
    val manifestPath = File(repoRoot, "$moduleName/src/main/AndroidManifest.xml")
    return if (manifestPath.exists()) {
      manifestPath.absolutePath
    } else {
      null
    }
  }

  private fun getAarFilesForModule(
    moduleName: String,
    extractedAarsDirectory: File
  ): List<AarFileInfo> {

    if (moduleName == "data") {
      return emptyList()
    }

    val allDependencies = bazelClient.retrieveTargetModuleDependencies(moduleName)
    val aarFiles = allDependencies.filter { it.endsWith(".${SdkConstants.EXT_AAR}") }

    if (aarFiles.isEmpty()) {
      return emptyList()
    }

    val moduleAarsDirectory = ensureDirectoryExists(File(extractedAarsDirectory, moduleName))
    val processedAars = mutableListOf<AarFileInfo>()

    aarFiles.forEach { aarFile ->
      val resolvedAarPath = resolveBazelPath(aarFile)

      if (resolvedAarPath != null && File(resolvedAarPath).exists()) {
        try {
          val extractedPath = extractAar(resolvedAarPath, moduleAarsDirectory)
          if (extractedPath != null) {
            processedAars.add(AarFileInfo(resolvedAarPath, extractedPath))
          }
        } catch (e: Exception) {
          // Continue processing other AARs
        }
      }
    }

    return processedAars
  }

  private fun getJarFilesForModule(moduleName: String): List<String> {
    if (moduleName == "data") {
      return emptyList()
    }

    val allDependencies = bazelClient.retrieveTargetModuleDependencies(moduleName)
    val jarFiles = allDependencies.filter { it.endsWith(".${SdkConstants.EXT_JAR}") }

    val validJarFiles = jarFiles.mapNotNull { jarFile ->
      val resolvedJarPath = resolveBazelPath(jarFile)

      if (resolvedJarPath != null && File(resolvedJarPath).exists()) {
        resolvedJarPath
      } else {
        null
      }
    }

    return validJarFiles
  }

  /**
   * Resolves a Bazel path to its actual file system location.
   * Handles external dependencies and workspace-relative paths.
   */
  private fun resolveBazelPath(path: String): String? {
    return when {
      File(path).isAbsolute -> path

      path.startsWith("external/") -> {
        val bazelInfo = bazelClient.getBazelInfo()
        val outputBase = bazelInfo["output_base"]
        val execRoot = bazelInfo["execution_root"]

        val possiblePaths = listOfNotNull(
          outputBase?.let { File(it, path).absolutePath },
          execRoot?.let { File(it, path).absolutePath },
          outputBase?.let {
            File(it, "external").resolve(path.removePrefix("external/")).absolutePath
          }
        )

        possiblePaths.firstOrNull { File(it).exists() }
      }

      else -> File(repoRoot, path).absolutePath
    }
  }

  private fun extractAar(aarFilePath: String, moduleAarsDirectory: File): String? {
    val aarFile = File(aarFilePath)
    if (!aarFile.exists()) {
      return null
    }

    val safeName = aarFile.nameWithoutExtension.replace(
      Regex("[^a-zA-Z0-9._-]"), "_"
    )
    val extractedDir = File(moduleAarsDirectory, safeName)

    if (extractedDir.exists()) {
      return extractedDir.absolutePath
    }

    return try {
      ensureDirectoryExists(extractedDir)

      var extractedFileCount = 0
      ZipFile(aarFile).use { zipFile ->
        zipFile.entries().asSequence().forEach { entry ->
          val entryFile = File(extractedDir, entry.name)

          if (entry.isDirectory) {
            entryFile.mkdirs()
          } else {
            entryFile.parentFile?.mkdirs()

            zipFile.getInputStream(entry).use { input ->
              entryFile.outputStream().use { output ->
                input.copyTo(output)
              }
            }
            extractedFileCount++
          }
        }
      }

      if (extractedFileCount == 0) {
        return null
      }

      extractedDir.absolutePath
    } catch (e: Exception) {
      if (extractedDir.exists()) {
        try {
          extractedDir.deleteRecursively()
        } catch (e: Exception) {
          throw IllegalStateException(
            "Failed to delete extracted directory: ${extractedDir.absolutePath}", e
          )
        }
      }
      null
    }
  }

  private fun getAndroidSdkPath(): String {
    return System.getenv(SdkConstants.ANDROID_HOME_ENV)
      ?: throw IllegalStateException(
        "ANDROID_HOME environment variable is not set. " +
          "Please set it to the path of your Android SDK."
      )
  }

  private fun generateXmlContent(
    cacheDirectory: File,
    jdkPath: String,
    sdkPath: String,
    moduleConfigs: List<ModuleConfig>
  ): String {
    return buildString {
      appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
      appendLine("<project>")
      appendLine("  <root dir='${repoRoot.absolutePath}'/>")
      appendLine("  <sdk dir='$sdkPath'/>")
      appendLine("  <jdk dir='$jdkPath'/>")
      appendLine("  <cache dir='${cacheDirectory.absolutePath}'/>")
      appendLine()

      moduleConfigs.forEach { config ->
        append(generateModuleXml(config))
        appendLine()
      }

      appendLine("</project>")
    }
  }

  private fun ensureDirectoryExists(directory: File): File {
    if (!directory.exists() && !directory.mkdirs()) {
      throw IOException("Failed to create directory: ${directory.path}")
    }
    return directory
  }

  private fun writeProjectDescriptionFile(file: File, content: String): File {
    return file.apply {
      try {
        bufferedWriter().use { writer ->
          writer.write(content)
        }
      } catch (e: Exception) {
        throw IllegalStateException("Failed to write project description file: ${file.path}", e)
      }
    }
  }

  /**
   * Prepares JDK information for lint by creating a release file.
   * Lint uses $JAVA_HOME/release which is not provided by Bazel's JavaRuntimeInfo.
   */
  private fun prepareJdk(jdkHome: File) {
    ensureDirectoryExists(jdkHome)
    val releaseFile = File(jdkHome, "release")

    try {
      val modulesString = generateModulesString()
      releaseFile.writeText(modulesString)
    } catch (e: Exception) {
      throw IllegalStateException("Failed to prepare JDK release file: ${releaseFile.path}", e)
    }
  }

  private fun generateModulesString(): String {
    return try {
      ModuleLayer.boot()
        .modules()
        .joinToString(
          separator = " ",
          prefix = "MODULES=\"",
          postfix = "\"",
          transform = Module::getName
        )
    } catch (e: Exception) {
      throw IllegalStateException("Failed to generate modules string from boot layer", e)
    }
  }

  /**
   * Generates XML configuration for a module.
   */
  private fun generateModuleXml(config: ModuleConfig): String {
    return buildString {
      appendLine("  <module")
      appendLine("""    name="${config.name}"""")
      appendLine("""    android="${config.isAndroid}"""")
      appendLine("""    library="${config.isLibrary}"""")
      appendLine("""    compile-sdk-version="${config.compileSdkVersion}"""")
      appendLine("""    kotlinLanguage="${config.kotlinLanguageVersion}"""")

      config.lintModelDir?.let { modelDir ->
        appendLine("""    model="${modelDir.absolutePath}"""")
      }

      appendLine("""    desugar="full">""")

      config.manifestFile?.let { manifestFile ->
        appendLine("""    <manifest file="$manifestFile"/>""")
      }

      config.srcDirs.forEach { srcDir ->
        appendLine("""    <src dir="$srcDir"/>""")
      }

      config.testDirs.forEach { testDir ->
        appendLine("""    <src dir="$testDir" test="true"/>""")
      }

      config.resourceDirs.forEach { resourceDir ->
        appendLine("""    <resource dir="$resourceDir"/>""")
      }

      config.dependencies.forEach { dependency ->
        appendLine("""    <dep module="$dependency"/>""")
      }

      config.aarFiles.forEach { aarInfo ->
        appendLine(
          """    <aar file="${aarInfo.originalPath}" extracted="${aarInfo.extractedPath}"/>"""
        )
      }

      config.jarFiles.forEach { jarFile ->
        appendLine("""    <classpath jar="$jarFile"/>""")
      }

      appendLine("  </module>")
    }
  }
}
