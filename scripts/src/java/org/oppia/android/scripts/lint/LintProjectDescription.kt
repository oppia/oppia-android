package org.oppia.android.scripts.lint

import com.android.SdkConstants
import org.oppia.android.scripts.common.BazelClient
import java.io.File
import java.io.IOException
import java.util.zip.ZipException
import java.util.zip.ZipFile
import kotlin.system.measureTimeMillis

/**
 * Enum representing module names in the project.
 *
 * @property moduleName The name of the module as a string.
 */
private enum class ModuleName(val moduleName: String) {
  /** Represents the application module. */
  APP("app"),

  /** Represents the domain module. */
  DOMAIN("domain"),

  /** Represents the testing module. */
  TESTING("testing"),

  /** Represents the utility module. */
  UTILITY("utility"),

  /** Represents the data module. */
  DATA("data");

  companion object {
    /** The application module instance. */
    val APPLICATION_MODULE = APP

    /** list of library modules in the project. */
    val LIBRARY_MODULES = listOf(DOMAIN, TESTING, UTILITY, DATA)
  }
}

/** Represents module configuration for lint project description. */
private data class ModuleConfig(
  val name: String,
  val isAndroid: Boolean,
  val isLibrary: Boolean,
  val isTest: Boolean,
  val srcFiles: List<String>,
  val testFiles: List<String>,
  val resourceDirs: List<String>,
  val manifestFile: String,
  val dependencies: List<String>,
  val aarFiles: List<AarFileInfo>,
  val jarFiles: List<String>,
  val lintCheckJars: List<String>,
  val lintModelDir: File? = null
)

/** Information about an AAR file and its extraction location. */
private data class AarFileInfo(
  val originalPath: String,
  val extractedPath: String
)

/**
 * Generates lint project description XML files for Android projects.
 *
 * @param repoRoot The root directory of the repository
 * @param workingDirectory The working directory where files will be generated
 * @param bazelClient The Bazel client for dependency resolution
 */
class LintProjectDescription(
  private val repoRoot: File,
  private val workingDirectory: File,
  private val bazelClient: BazelClient
) {

  companion object {
    private const val LINT_PROJECT_DESCRIPTION_FILE_NAME = "lint-project-description.xml"
    private const val LINT_CACHE_DIRECTORY_NAME = "lint-cache-directory"
    private const val EXTRACTED_AARS_DIRECTORY_NAME = "extracted-aars"

    /**
     * Ensures a directory exists, creating it if necessary.
     *
     * @throws IllegalStateException if directory creation fails
     */
    private fun ensureDirectoryExists(directory: File): File {
      if (!directory.exists() && !directory.mkdirs()) {
        throw IllegalStateException("Failed to create directory: ${directory.absolutePath}")
      }
      return directory
    }
  }

  /**
   * Generates the lint project description XML file.
   *
   * @return The generated project description file
   * @throws IOException if file operations fail
   * @throws IllegalStateException if required dependencies are not available
   */
  fun generateProjectDescriptionXml(): File {
    val projectDescriptionFile = File(workingDirectory, LINT_PROJECT_DESCRIPTION_FILE_NAME)
    val cacheDirectory = ensureDirectoryExists(File(workingDirectory, LINT_CACHE_DIRECTORY_NAME))
    val extractedAarsDirectory =
      ensureDirectoryExists(File(workingDirectory, EXTRACTED_AARS_DIRECTORY_NAME))

    val moduleConfigBuilder =
      ModuleConfigurationBuilder(repoRoot, bazelClient, extractedAarsDirectory)
    val moduleConfigs = moduleConfigBuilder.buildAllModuleConfigurations()
    val xmlContent = generateProjectXmlContent(cacheDirectory, moduleConfigs)

    return writeProjectDescriptionFile(projectDescriptionFile, xmlContent)
  }

  private fun generateProjectXmlContent(
    cacheDirectory: File,
    moduleConfigs: List<ModuleConfig>
  ): String = buildString {
    appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
    appendLine("<project>")
    appendLine("  <root dir='${repoRoot.absolutePath}'/>")
    appendLine("  <cache dir='${cacheDirectory.absolutePath}'/>")
    appendLine()

    moduleConfigs.forEach { config ->
      append(generateModuleXml(config))
      appendLine()
    }

    appendLine("</project>")
  }

  private fun generateModuleXml(config: ModuleConfig): String = buildString {
    appendLine("  <module")
    appendLine("""    name="${config.name}"""")
    appendLine("""    android="${config.isAndroid}"""")
    appendLine("""    library="${config.isLibrary}"""")
    appendLine("""    test="${config.isTest}"""")

    config.lintModelDir?.let { modelDir ->
      appendLine("""    model="${modelDir.absolutePath}"""")
    }

    appendLine("""    desugar="full">""")

    appendLine("""    <manifest file="${config.manifestFile}"/>""")

    config.srcFiles.forEach { srcFile ->
      appendLine("""    <src file="$srcFile"/>""")
    }

    config.testFiles.forEach { testFile ->
      appendLine("""    <src file="$testFile" test="true"/>""")
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

    config.lintCheckJars.forEach { lintCheckJar ->
      appendLine("""    <lint-checks jar="$lintCheckJar"/>""")
    }

    appendLine("  </module>")
  }

  private fun writeProjectDescriptionFile(file: File, content: String): File =
    file.apply {
      try {
        bufferedWriter().use { writer ->
          writer.write(content)
        }
      } catch (e: Exception) {
        throw IllegalStateException(
          "Failed to write project description file: ${file.absolutePath}"
        )
      }
    }
}

/** Builds module configurations for all modules in the project. */
private class ModuleConfigurationBuilder(
  private val repoRoot: File,
  bazelClient: BazelClient,
  extractedAarsDirectory: File
) {

  companion object {
    private val MODULE_DEPENDENCIES = mapOf(
      ModuleName.APP to ModuleName.LIBRARY_MODULES,
      ModuleName.TESTING to listOf(ModuleName.UTILITY, ModuleName.DOMAIN),
      ModuleName.DOMAIN to listOf(ModuleName.UTILITY),
      ModuleName.DATA to listOf(ModuleName.UTILITY)
    )
    private val ANDROID_MANIFEST_PATH = "src/main/${SdkConstants.FN_ANDROID_MANIFEST_XML}"
  }

  private val dependencyResolver = DependencyResolver(bazelClient, repoRoot, extractedAarsDirectory)

  /** Builds configurations for all modules in the project. */
  fun buildAllModuleConfigurations(): List<ModuleConfig> = buildList {
    add(buildModuleConfiguration(ModuleName.APPLICATION_MODULE, isLibrary = false))

    ModuleName.LIBRARY_MODULES.forEach { module ->
      add(buildModuleConfiguration(module, isLibrary = true))
    }
  }

  /** Builds configuration for a single module. */
  private fun buildModuleConfiguration(
    module: ModuleName,
    isLibrary: Boolean
  ): ModuleConfig {
    val sourceCollector = SourceFileCollector(repoRoot, module)
    val (testFiles, srcFiles) = sourceCollector.collectSourceFiles()
      .partition { path ->
        path.endsWith("Test.kt") ||
          path.contains("/test/") ||
          path.contains("/sharedTest/")
      }

    return ModuleConfig(
      name = module.moduleName,
      isAndroid = true,
      isLibrary = isLibrary,
      isTest = module == ModuleName.TESTING,
      srcFiles = srcFiles,
      testFiles = testFiles,
      resourceDirs = sourceCollector.collectResourceDirectories(),
      manifestFile = findManifestFile(module),
      dependencies = MODULE_DEPENDENCIES[module]?.map { it.moduleName }.orEmpty(),
      aarFiles = dependencyResolver.resolveAarFiles(module),
      jarFiles = dependencyResolver.resolveJarFiles(module),
      lintCheckJars = dependencyResolver.extractLintCheckJars(
        dependencyResolver.resolveAarFiles(module)
      )
    )
  }

  private fun findManifestFile(module: ModuleName): String {
    val manifestPath = File(repoRoot, "${module.moduleName}/$ANDROID_MANIFEST_PATH")
    require(manifestPath.exists()) {
      "Manifest file not found for module: ${module.moduleName} at ${manifestPath.absolutePath}"
    }
    return manifestPath.absolutePath
  }
}

/** Helper class for collecting source files and resources for a module. */
private class SourceFileCollector(
  repoRoot: File,
  module: ModuleName
) {
  companion object {
    private val SOURCE_EXTENSIONS = setOf("kt", "java")
  }

  private val moduleName = module.moduleName
  private val sourceDir = File(repoRoot, "$moduleName/${SdkConstants.FD_SOURCES}")

  /** Collects the source files for the module. */
  fun collectSourceFiles(): List<String> = collectFilesFromDirectory(sourceDir)

  /** Collects the resource directories for the module. */
  fun collectResourceDirectories(): List<String> = buildList {
    if (sourceDir.exists()) {
      sourceDir.walkTopDown()
        .filter { it.isDirectory && it.name == SdkConstants.FD_RES }
        .forEach { add(it.path) }
    }
  }

  private fun collectFilesFromDirectory(directory: File): List<String> {
    require(directory.exists() && directory.isDirectory) {
      throw IllegalStateException("Source directory does not exist at: $directory")
    }

    return directory.walkTopDown()
      .filter { it.isFile && it.extension in SOURCE_EXTENSIONS }
      .map { it.absolutePath }
      .toList()
  }
}

/** Helper class for resolving module dependencies. */
private class DependencyResolver(
  private val bazelClient: BazelClient,
  repoRoot: File,
  private val extractedAarsDirectory: File
) {
  private val dependencyCache = mutableMapOf<String, List<String>>()
  private val pathResolver = PathResolver(repoRoot, bazelClient)
  private val aarExtractor = AarExtractor()

  /** Resolves the AAR files for the given module. */
  fun resolveAarFiles(module: ModuleName): List<AarFileInfo> {
    val allDependencies = getDependenciesWithCache(module.moduleName)
    val aarFiles = allDependencies.filter { it.endsWith(".${SdkConstants.EXT_AAR}") }

    if (aarFiles.isEmpty()) {
      return emptyList()
    }

    val moduleAarsDirectory = ensureDirectoryExists(File(extractedAarsDirectory, module.moduleName))

    return aarFiles.mapNotNull { aarFile ->
      processAarFile(aarFile, moduleAarsDirectory)
    }
  }

  /** Resolves the JAR files for the given module. */
  fun resolveJarFiles(module: ModuleName): List<String> {
    val allDependencies = getDependenciesWithCache(module.moduleName)
    return allDependencies
      .filter { it.endsWith(".${SdkConstants.EXT_JAR}") }
      .mapNotNull { jarFile ->
        pathResolver.resolveBazelPath(jarFile)
      }
  }

  /** Extracts lint check JAR files from the given list of AAR files. */
  fun extractLintCheckJars(aarFiles: List<AarFileInfo>): List<String> =
    aarFiles.mapNotNull { aarInfo ->
      val lintJar = File(aarInfo.extractedPath, "lint.jar")
      if (lintJar.exists()) lintJar.absolutePath else null
    }

  private fun getDependenciesWithCache(moduleName: String): List<String> =
    dependencyCache.getOrPut(moduleName) {
      bazelClient.retrieveTargetModuleDependencies("//$moduleName:*")
    }

  private fun processAarFile(aarFile: String, moduleAarsDirectory: File): AarFileInfo? {
    val resolvedAarPath = pathResolver.resolveBazelPath(aarFile)
      ?: return null

    val aarFileObj = File(resolvedAarPath)
    require(aarFileObj.exists()) {
      "AAR file does not exist: $resolvedAarPath"
    }

    return try {
      val extractedPath = aarExtractor.extractAar(resolvedAarPath, moduleAarsDirectory)
      AarFileInfo(resolvedAarPath, extractedPath)
    } catch (e: ZipException) {
      throw IOException("Invalid AAR file format: $aarFile", e)
    } catch (e: IOException) {
      throw IOException("Failed to extract AAR file: $aarFile", e)
    } catch (e: Exception) {
      throw IllegalStateException("Failed to extract AAR file: $aarFile", e)
    }
  }

  private fun ensureDirectoryExists(directory: File): File {
    if (!directory.exists() && !directory.mkdirs()) {
      throw IllegalStateException("Failed to create directory: ${directory.absolutePath}")
    }
    return directory
  }
}

/** Object for resolving Bazel paths to actual file system locations. */
private class PathResolver(
  private val repoRoot: File,
  private val bazelClient: BazelClient
) {
  companion object {
    private const val BAZEL_OUTPUT_BASE_KEY = "output_base"
  }

  private val pathCache = mutableMapOf<String, String?>()

  /** Resolves a Bazel path to an absolute file system path. */
  fun resolveBazelPath(path: String): String? =
    pathCache.getOrPut(path) {
      val resolvedPath = when {
        File(path).isAbsolute -> path
        path.startsWith("external/") -> resolveExternalPath(path)
        else -> File(repoRoot, path).absolutePath
      }

      if (File(resolvedPath).exists()) {
        resolvedPath
      } else {
        println("Path cannot be resolved: $path")
        null
      }
    }

  private fun resolveExternalPath(path: String): String {
    val bazelInfo = bazelClient.retrieveBazelInfo()
    val outputBase = bazelInfo[BAZEL_OUTPUT_BASE_KEY]
      ?: throw IllegalStateException(
        "Could not retrieve Bazel $BAZEL_OUTPUT_BASE_KEY for path: $path"
      )

    return File(outputBase, path).absolutePath
  }
}

/**
 * Handles extraction of AAR files.
 */
private class AarExtractor {
  companion object {
    private val INVALID_DIRECTORY_CHARS = Regex("[^a-zA-Z0-9._-]")

    /** Creates a safe directory name by replacing invalid characters. */
    private fun createSafeDirectoryName(name: String): String =
      name.replace(INVALID_DIRECTORY_CHARS, "_")
  }

  private val extractionCache = mutableMapOf<String, String>()

  /** Extracts the contents of an AAR file to a specified directory. */
  fun extractAar(aarFilePath: String, moduleAarsDirectory: File): String =
    extractionCache.getOrPut(aarFilePath) {
      performAarExtraction(aarFilePath, moduleAarsDirectory)
    }

  private fun performAarExtraction(aarFilePath: String, moduleAarsDirectory: File): String {
    val aarFile = File(aarFilePath)
    require(aarFile.exists()) {
      "AAR file does not exist: $aarFilePath"
    }

    val safeName = createSafeDirectoryName(aarFile.nameWithoutExtension)
    val extractedDir = File(moduleAarsDirectory, safeName)

    if (extractedDir.exists()) {
      return extractedDir.absolutePath
    }

    return try {
      ensureDirectoryExists(extractedDir)
      val extractedCount = performExtraction(aarFile, extractedDir)

      if (extractedCount > 0) {
        extractedDir.absolutePath
      } else {
        extractedDir.deleteRecursively()
        throw IllegalStateException("No files were extracted from AAR: $aarFilePath")
      }
    } catch (e: Exception) {
      if (extractedDir.exists()) {
        extractedDir.deleteRecursively()
      }
      throw IllegalStateException("Failed to extract AAR file: $aarFilePath")
    }
  }

  private fun performExtraction(aarFile: File, extractedDir: File): Int {
    var extractedFileCount = 0

    try {
      ZipFile(aarFile).use { zipFile ->
        zipFile.entries().asSequence().forEach { entry ->
          val entryFile = File(extractedDir, entry.name)

          if (entry.isDirectory) {
            if (!entryFile.mkdirs() && !entryFile.exists()) {
              throw IOException("Failed to create directory: ${entryFile.absolutePath}")
            }
          } else {
            entryFile.parentFile?.let { parentDir ->
              if (!parentDir.mkdirs() && !parentDir.exists()) {
                throw IOException("Failed to create parent directory: ${parentDir.absolutePath}")
              }
            }

            zipFile.getInputStream(entry).use { input ->
              entryFile.outputStream().use { output ->
                input.copyTo(output)
              }
            }
            extractedFileCount++
          }
        }
      }
    } catch (e: IOException) {
      throw IOException("Failed to extract AAR file: ${aarFile.absolutePath}")
    }

    return extractedFileCount
  }

  private fun ensureDirectoryExists(directory: File): File {
    if (!directory.exists() && !directory.mkdirs()) {
      throw IllegalStateException("Failed to create directory: ${directory.absolutePath}")
    }
    return directory
  }
}
