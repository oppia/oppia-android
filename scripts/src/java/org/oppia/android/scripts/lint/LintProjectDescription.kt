package org.oppia.android.scripts.lint

import com.android.SdkConstants
import org.oppia.android.scripts.common.BazelClient
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

/** Enum representing module names in the project. */
private enum class ModuleName(val moduleName: String) {
  APP("app"),
  DOMAIN("domain"),
  TESTING("testing"),
  UTILITY("utility"),
  DATA("data");

  companion object {
    val APPLICATION_MODULE = APP
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

    private val SOURCE_EXTENSIONS = setOf("kt", "java")
    private val MODULES_WITH_MAIN_RES = setOf(ModuleName.APP, ModuleName.UTILITY)
    private val MODULES_WITH_TEST_RES = setOf(ModuleName.UTILITY)
    private val MODULE_DEPENDENCIES = mapOf(
      ModuleName.APP to ModuleName.LIBRARY_MODULES,
      ModuleName.TESTING to listOf(ModuleName.UTILITY, ModuleName.DOMAIN),
      ModuleName.DOMAIN to listOf(ModuleName.UTILITY),
      ModuleName.DATA to listOf(ModuleName.UTILITY)
    )

    /** Creates a safe directory name by replacing invalid characters. */
    private fun createSafeDirectoryName(name: String): String =
      name.replace(Regex("[^a-zA-Z0-9._-]"), "_")

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
    val projectDescriptionFile =
      File(workingDirectory, LINT_PROJECT_DESCRIPTION_FILE_NAME)
    val cacheDirectory =
      ensureDirectoryExists(File(workingDirectory, LINT_CACHE_DIRECTORY_NAME))
    val extractedAarsDirectory =
      ensureDirectoryExists(File(workingDirectory, EXTRACTED_AARS_DIRECTORY_NAME))

    val moduleConfigs = buildModuleConfigurations(extractedAarsDirectory)
    val xmlContent = generateProjectXmlContent(cacheDirectory, moduleConfigs)

    return writeProjectDescriptionFile(projectDescriptionFile, xmlContent)
  }

  /** Builds configurations for all modules in the project. */
  private fun buildModuleConfigurations(extractedAarsDirectory: File): List<ModuleConfig> =
    buildList {
      add(
        buildModuleConfiguration(
          ModuleName.APPLICATION_MODULE, isLibrary = false, extractedAarsDirectory
        )
      )

      ModuleName.LIBRARY_MODULES.forEach { module ->
        add(buildModuleConfiguration(module, isLibrary = true, extractedAarsDirectory))
      }
    }

  /** Builds configuration for a single module. */
  private fun buildModuleConfiguration(
    module: ModuleName,
    isLibrary: Boolean,
    extractedAarsDirectory: File
  ): ModuleConfig {
    val sourceCollector = SourceFileCollector(repoRoot, module)
    val dependencyResolver = DependencyResolver(bazelClient, extractedAarsDirectory)

    return ModuleConfig(
      name = module.moduleName,
      isAndroid = true,
      isLibrary = isLibrary,
      isTest = module == ModuleName.TESTING,
      srcFiles = sourceCollector.collectSourceFiles(),
      testFiles = sourceCollector.collectTestFiles(),
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

  /** Helper class for collecting source files and resources for a module. */
  private inner class SourceFileCollector(
    private val repoRoot: File,
    private val module: ModuleName
  ) {
    private val moduleName = module.moduleName

    fun collectSourceFiles(): List<String> =
      collectFilesFromDirectory(File(repoRoot, "$moduleName/src/main/java"))

    fun collectTestFiles(): List<String> = buildList {
      addAll(collectFilesFromDirectory(File(repoRoot, "$moduleName/src/test/java")))

      if (module == ModuleName.APP) {
        addAll(collectFilesFromDirectory(File(repoRoot, "$moduleName/src/sharedTest/java")))
      }
    }

    fun collectResourceDirectories(): List<String> = buildList {
      if (module in MODULES_WITH_MAIN_RES) {
        addDirectoryIfExists(File(repoRoot, "$moduleName/src/main/res"))
      }
      if (module in MODULES_WITH_TEST_RES) {
        addDirectoryIfExists(File(repoRoot, "$moduleName/src/test/res"))
      }
    }

    private fun collectFilesFromDirectory(directory: File): List<String> {
      if (!directory.exists() || !directory.isDirectory) {
        throw IllegalStateException("Directory does not exist: ${directory.absolutePath}")
      }

      return directory.walkTopDown()
        .filter { it.isFile && it.extension in SOURCE_EXTENSIONS }
        .map { it.absolutePath }
        .toList()
    }

    private fun MutableList<String>.addDirectoryIfExists(directory: File) {
      if (directory.exists() && directory.isDirectory) {
        add(directory.absolutePath)
      } else {
        throw IllegalStateException(
          "Required resource directory does not exist: ${directory.absolutePath}"
        )
      }
    }
  }

  /** Helper class for resolving module dependencies. */
  private inner class DependencyResolver(
    private val bazelClient: BazelClient,
    private val extractedAarsDirectory: File
  ) {
    fun resolveAarFiles(module: ModuleName): List<AarFileInfo> {
      if (module == ModuleName.DATA) {
        return emptyList()
      }

      val allDependencies = bazelClient.retrieveTargetModuleDependencies(module.moduleName)
      val aarFiles = allDependencies.filter { it.endsWith(".${SdkConstants.EXT_AAR}") }

      if (aarFiles.isEmpty()) {
        return emptyList()
      }

      val moduleAarsDirectory =
        ensureDirectoryExists(File(extractedAarsDirectory, module.moduleName))
      return aarFiles.mapNotNull { aarFile ->
        processAarFile(aarFile, moduleAarsDirectory)
      }
    }

    fun resolveJarFiles(module: ModuleName): List<String> {
      if (module == ModuleName.DATA) {
        return emptyList()
      }

      val allDependencies = bazelClient.retrieveTargetModuleDependencies(module.moduleName)
      return allDependencies
        .filter { it.endsWith(".${SdkConstants.EXT_JAR}") }
        .mapNotNull { jarFile ->
          PathResolver.resolveBazelPath(jarFile, repoRoot, bazelClient)
        }
    }

    fun extractLintCheckJars(aarFiles: List<AarFileInfo>): List<String> =
      aarFiles.mapNotNull { aarInfo ->
        val lintJar = File(aarInfo.extractedPath, "lint.jar")
        if (lintJar.exists()) lintJar.absolutePath else null
      }

    private fun processAarFile(aarFile: String, moduleAarsDirectory: File): AarFileInfo? {
      val resolvedAarPath = PathResolver.resolveBazelPath(aarFile, repoRoot, bazelClient)
        ?: return null

      require(File(resolvedAarPath).exists()) {
        "AAR file does not exist: $resolvedAarPath"
      }

      return try {
        val extractedPath = AarExtractor.extractAar(resolvedAarPath, moduleAarsDirectory)
        AarFileInfo(resolvedAarPath, extractedPath)
      } catch (e: Exception) {
        throw IllegalStateException("Failed to extract AAR file: $aarFile", e)
      }
    }
  }

  /** Object for resolving Bazel paths to actual file system locations. */
  private object PathResolver {

    fun resolveBazelPath(path: String, repoRoot: File, bazelClient: BazelClient): String? {
      val resolvedPath = when {
        File(path).isAbsolute -> path
        path.startsWith("external/") -> resolveExternalPath(path, bazelClient)
        else -> File(repoRoot, path).absolutePath
      }

      return if (File(resolvedPath).exists()) {
        resolvedPath
      } else {
        println("Path can not be resolved: $path")
        null
      }
    }

    private fun resolveExternalPath(path: String, bazelClient: BazelClient): String {
      val bazelInfo = bazelClient.retrieveBazelInfo()
      val outputBase = bazelInfo["output_base"]
        ?: throw IllegalStateException("Could not retrieve Bazel output_base for path: $path")

      return File(outputBase, path).absolutePath
    }
  }

  private object AarExtractor {

    fun extractAar(aarFilePath: String, moduleAarsDirectory: File): String {
      val aarFile = File(aarFilePath)
      if (!aarFile.exists()) {
        throw IllegalStateException("AAR file does not exist: $aarFilePath")
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

          if (extractedDir.exists()) {
            extractedDir.deleteRecursively()
          }
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
  }

  private fun findManifestFile(module: ModuleName): String {
    val manifestPath = File(repoRoot, "${module.moduleName}/src/main/AndroidManifest.xml")
    return if (manifestPath.exists()) {
      manifestPath.absolutePath
    } else {
      throw IllegalStateException(
        "Manifest file not found for module: ${module.moduleName} at ${manifestPath.absolutePath}"
      )
    }
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
