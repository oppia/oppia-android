package org.oppia.android.scripts.lint

import com.android.SdkConstants
import com.android.tools.lint.model.LintModelModuleType.APP
import com.android.tools.lint.model.LintModelModuleType.LIBRARY
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipException
import java.util.zip.ZipFile
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories

/**
 * Enum representing module names in the project.
 *
 * @property moduleName The name of the module as a string.
 */
enum class ModuleName(val moduleName: String) {
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
data class ModuleConfig(
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
data class AarFileInfo(
  val originalPath: String,
  val extractedPath: String
)

/** Cached entry with TTL support. */
private data class CachedEntry<T>(
  val value: T,
  val timestamp: Instant,
  val sizeBytes: Long = 0L
) {
  /** Checks if the cache entry is expired. */
  fun isExpired(ttlSeconds: Long): Boolean =
    Instant.now().epochSecond - timestamp.epochSecond > ttlSeconds
}

/** Cache manager with TTL and memory-based cleanup. */
private class CacheManager {
  companion object {

    private const val DEPENDENCIES_TTL = 300L // 5 minutes
    private const val PATH_RESOLUTION_TTL = 600L // 10 minutes
    private const val AAR_EXTRACTION_TTL = 1800L // 30 minutes

    private const val MAX_CACHE_SIZE_BYTES = 100 * 1024 * 1024L // 100MB
    private const val CLEANUP_THRESHOLD = 0.8 // Cleanup when 80% full
  }

  private val dependencyCache = ConcurrentHashMap<String, CachedEntry<List<String>>>()
  private val pathCache = ConcurrentHashMap<String, CachedEntry<String?>>()
  private val aarExtractionCache = ConcurrentHashMap<String, CachedEntry<String>>()

  private var totalCacheSize = 0L

  /** Gets dependencies with TTL caching. */
  fun getDependencies(key: String, provider: () -> List<String>): List<String> {
    cleanupExpiredEntries()
    checkMemoryLimitAndCleanup()

    val cached = dependencyCache[key]
    if (cached != null && !cached.isExpired(DEPENDENCIES_TTL)) {
      return cached.value
    }

    val value = provider()
    val sizeBytes = estimateListSize(value)
    dependencyCache[key] = CachedEntry(value, Instant.now(), sizeBytes)
    updateCacheSize(sizeBytes)

    return value
  }

  /** Gets path resolution with TTL caching. */
  fun getPathResolution(key: String, provider: () -> String?): String? {
    cleanupExpiredEntries()
    checkMemoryLimitAndCleanup()

    val cached = pathCache[key]
    if (cached != null && !cached.isExpired(PATH_RESOLUTION_TTL)) {
      return cached.value
    }

    val value = provider()
    val sizeBytes = estimateStringSize(value)
    pathCache[key] = CachedEntry(value, Instant.now(), sizeBytes)
    updateCacheSize(sizeBytes)

    return value
  }

  /** Gets AAR extraction with TTL caching. */
  fun getAarExtraction(key: String, provider: () -> String): String {
    cleanupExpiredEntries()
    checkMemoryLimitAndCleanup()

    val cached = aarExtractionCache[key]
    if (cached != null && !cached.isExpired(AAR_EXTRACTION_TTL)) {
      return cached.value
    }

    val value = provider()
    val sizeBytes = estimateStringSize(value)
    aarExtractionCache[key] = CachedEntry(value, Instant.now(), sizeBytes)
    updateCacheSize(sizeBytes)

    return value
  }

  /** Cleans up expired entries from all caches. */
  private fun cleanupExpiredEntries() {

    // Cleanup dependencies
    val expiredDeps = dependencyCache.filterValues {
      it.isExpired(DEPENDENCIES_TTL)
    }.keys
    expiredDeps.forEach { key ->
      dependencyCache.remove(key)?.let { entry ->
        totalCacheSize -= entry.sizeBytes
      }
    }

    // Cleanup path resolutions
    val expiredPaths = pathCache.filterValues {
      it.isExpired(PATH_RESOLUTION_TTL)
    }.keys
    expiredPaths.forEach { key ->
      pathCache.remove(key)?.let { entry ->
        totalCacheSize -= entry.sizeBytes
      }
    }

    // Cleanup AAR extractions
    val expiredAars = aarExtractionCache.filterValues {
      it.isExpired(AAR_EXTRACTION_TTL)
    }.keys
    expiredAars.forEach { key ->
      aarExtractionCache.remove(key)?.let { entry ->
        totalCacheSize -= entry.sizeBytes
      }
    }
  }

  /** Performs aggressive cleanup when memory limit is reached. */
  private fun checkMemoryLimitAndCleanup() {
    if (totalCacheSize > MAX_CACHE_SIZE_BYTES * CLEANUP_THRESHOLD) {
      performAggressiveCleanup()
    }
  }

  /** Aggressively removes oldest entries to free up memory. */
  private fun performAggressiveCleanup() {
    val targetSize = MAX_CACHE_SIZE_BYTES / 2 // Clean to 50% capacity

    // Collect all entries with their timestamps
    val allEntries = mutableListOf<Pair<String, CachedEntry<*>>>()

    dependencyCache.forEach { (key, entry) ->
      allEntries.add("dep:$key" to entry)
    }
    pathCache.forEach { (key, entry) ->
      allEntries.add("path:$key" to entry)
    }
    aarExtractionCache.forEach { (key, entry) ->
      allEntries.add("aar:$key" to entry)
    }

    // Sort by timestamp (oldest first)
    allEntries.sortBy { it.second.timestamp }

    // Remove oldest entries until we're under target size
    var currentSize = totalCacheSize
    for ((key, entry) in allEntries) {
      if (currentSize <= targetSize) break

      when {
        key.startsWith("dep:") -> {
          val realKey = key.substring(4)
          dependencyCache.remove(realKey)
        }
        key.startsWith("path:") -> {
          val realKey = key.substring(5)
          pathCache.remove(realKey)
        }
        key.startsWith("aar:") -> {
          val realKey = key.substring(4)
          aarExtractionCache.remove(realKey)
        }
      }

      currentSize -= entry.sizeBytes
    }

    totalCacheSize = currentSize
  }

  /** Updates the total cache size. */
  private fun updateCacheSize(additionalSize: Long) {
    totalCacheSize += additionalSize
  }

  /** Estimates the memory size of a string list. */
  private fun estimateListSize(list: List<String>): Long {
    return list.sumOf { it.length * 2L } + (list.size * 8L)
  }

  /** Estimates the memory size of a string. */
  private fun estimateStringSize(str: String?): Long {
    return (str?.length?.times(2L) ?: 0L) + 8L
  }
}

/** logger for error messages. */
private class Logger(workingDirectory: File) {
  private val logFile = File(workingDirectory, "error-logs")

  /** Logs messages with timestamp. */
  fun logError(message: String) {
    try {
      logFile.appendText("[${Instant.now()}] $message\n")
    } catch (e: Exception) {
      System.err.println("Failed to write to log: ${e.message}")
    }
  }
}

/**
 * Generates lint project description XML files for Android projects.
 *
 * @param repoRoot the root directory of the repository
 * @param workingDirectory the working directory where files will be generated
 * @param commandExecutor executes the specified command in the specified working directory
 */
class LintProjectDescription(
  private val repoRoot: File,
  private val workingDirectory: File,
  commandExecutor: CommandExecutor
) {

  private val bazelClient = BazelClient(repoRoot, commandExecutor)
  companion object {
    private const val LINT_PROJECT_DESCRIPTION_FILE_NAME = "lint-project-description.xml"
    private const val LINT_CACHE_DIRECTORY_NAME = "lint-cache-directory"
    private const val EXTRACTED_AARS_DIRECTORY_NAME = "extracted-aars"
    private const val LINT_MODELS_DIRECTORY = "models-directory"

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

  private val cacheManager = CacheManager()
  private val logger = Logger(workingDirectory)

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
    val modelsDirectory = ensureDirectoryExists(File(workingDirectory, LINT_MODELS_DIRECTORY))

    val moduleConfigBuilder = ModuleConfigurationBuilder(
      repoRoot, bazelClient, extractedAarsDirectory,
      modelsDirectory,
      cacheManager,
      logger
    )
    val initialModuleConfigs = moduleConfigBuilder.buildAllModuleConfigurations()
    val moduleConfigs = moduleConfigBuilder.buildModelDirectory(initialModuleConfigs)
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
    appendLine("""    model="${config.lintModelDir?.absolutePath}"""")
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
  extractedAarsDirectory: File,
  private val modelsDirectory: File,
  cacheManager: CacheManager,
  logger: Logger,
) {

  companion object {
    // These dependencies are referenced from Gradle build files
    // replicating the module-module dependencies
    private val MODULE_DEPENDENCIES = mapOf(
      ModuleName.APP to ModuleName.LIBRARY_MODULES,
      ModuleName.TESTING to listOf(ModuleName.UTILITY, ModuleName.DOMAIN),
      ModuleName.DOMAIN to listOf(ModuleName.UTILITY),
      ModuleName.DATA to listOf(ModuleName.UTILITY)
    )
    private const val ANDROID_MANIFEST_PATH = "src/main/${SdkConstants.FN_ANDROID_MANIFEST_XML}"
  }

  private val dependencyResolver = DependencyResolver(
    bazelClient, repoRoot, extractedAarsDirectory,
    cacheManager,
    logger
  )

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
      ),
    )
  }

  fun buildModelDirectory(moduleConfigs: List<ModuleConfig>): List<ModuleConfig> {
    return moduleConfigs.map { moduleConfig ->
      val modelDirectory = File(modelsDirectory, moduleConfig.name)

      if (!modelDirectory.exists() && !modelDirectory.mkdirs()) {
        throw IllegalStateException(
          "Failed to create model directory: ${modelDirectory.absolutePath}"
        )
      }

      val modelCreator = LintModelCreator(modelDirectory, repoRoot)
      val generatedModelDir = modelCreator.generateModelFiles(moduleConfig)

      moduleConfig.copy(lintModelDir = generatedModelDir)
    }
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
  private val extractedAarsDirectory: File,
  private val cacheManager: CacheManager,
  logger: Logger
) {
  private val pathResolver = PathResolver(repoRoot, bazelClient, cacheManager, logger)
  private val aarExtractor = AarExtractor(cacheManager)

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
    cacheManager.getDependencies(moduleName) {
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
      throw ZipException("Invalid AAR file format: $aarFile")
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
  private val bazelClient: BazelClient,
  private val cacheManager: CacheManager,
  private val logger: Logger
) {
  companion object {
    private const val BAZEL_OUTPUT_BASE_KEY = "output_base"
  }

  /** Resolves a Bazel path to an absolute file system path. */
  fun resolveBazelPath(path: String): String? =
    cacheManager.getPathResolution(path) {
      val resolvedPath = when {
        File(path).isAbsolute -> path
        path.startsWith("external/") -> resolveExternalPath(path)
        else -> File(repoRoot, path).absolutePath
      }

      if (File(resolvedPath).exists()) {
        resolvedPath
      } else {
        val errorMessage = "Path cannot be resolved: $path -> $resolvedPath"
        logger.logError(errorMessage)
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

/** Handles extraction of AAR files. */
private class AarExtractor(private val cacheManager: CacheManager) {
  companion object {
    private val INVALID_DIRECTORY_CHARS = Regex("[^a-zA-Z0-9._-]")

    /** Creates a safe directory name by replacing invalid characters. */
    private fun createSafeDirectoryName(name: String): String =
      name.replace(INVALID_DIRECTORY_CHARS, "_")
  }

  /** Extracts the contents of an AAR file to a specified directory. */
  fun extractAar(aarFilePath: String, moduleAarsDirectory: File): String =
    cacheManager.getAarExtraction(aarFilePath) {
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

/**
 * Creates lint model files for Android modules to enable lint checking.
 *
 * @param modelDir Directory where the generated lint model files will be stored
 * @param repoRoot Root directory of the repository containing the Android modules
 */
class LintModelCreator(
  private val modelDir: File,
  private val repoRoot: File
) {
  companion object {
    private const val MODULE_XML_FILE = "module.xml"
    private const val VARIANT_XML_FILE = "main.xml"
    private const val ARTIFACT_LIBRARIES_XML_FILE = "main-mainArtifact-libraries.xml"
    private const val DEPENDENCIES_XML_FILE = "main-mainArtifact-dependencies.xml"

    private const val BUILD_DIR_NAME = "build"
    private const val CLASSES_DIR_NAME = "classes"

    private const val PACKAGE_PREFIX = "org.oppia.android"
    private const val MIN_SDK_VERSION = "21"
    private const val TARGET_SDK_VERSION = "34"
    private const val PROGUARD_CONFIG_PATH = "config/proguard"
  }

  /**
   * Generates all required lint model files for the specified module configuration.
   *
   * @param moduleConfig Configuration object containing module-specific settings
   * @return The directory containing the generated model files
   */
  fun generateModelFiles(moduleConfig: ModuleConfig): File {
    val modelPath = modelDir.toPath().createDirectories()
    val buildDir = modelPath.resolve(BUILD_DIR_NAME).createDirectories()
    val relativeProjectPath = modelPath.absolute().relativize(repoRoot.toPath().absolute())

    generateModuleXml(
      modelPath.resolve(MODULE_XML_FILE).toFile(),
      moduleConfig,
      relativeProjectPath,
      buildDir
    )

    generateVariantXml(
      modelDir.resolve(VARIANT_XML_FILE),
      moduleConfig,
      buildDir
    )

    generateArtifactLibrariesXml(modelDir.resolve(ARTIFACT_LIBRARIES_XML_FILE))
    generateDependenciesXml(modelDir.resolve(DEPENDENCIES_XML_FILE))

    return modelDir
  }

  private fun generateModuleXml(
    moduleFile: File,
    moduleConfig: ModuleConfig,
    relativeProjectPath: Path,
    buildDir: Path
  ) {
    val moduleType = if (moduleConfig.isLibrary) LIBRARY else APP
    val buildToolsVersion = AndroidBuildSdkProperties().buildToolsVersion

    val content =
      """
      <lint-module
          dir="$relativeProjectPath"
          name="${moduleConfig.name}"
          type="${moduleType.name}"
          maven="__non_maven__"
          buildFolder="${buildDir.toFile().absolutePath}"
          javaSourceLevel="1.8"
          compileTarget="$buildToolsVersion"
          neverShrinking="true">
          <lintOptions />
          <variant name="main"/>
      </lint-module>
      """.trimIndent()

    moduleFile.writeText(content)
  }

  private fun generateVariantXml(
    variantFile: File,
    moduleConfig: ModuleConfig,
    buildDir: Path
  ) {
    val packageName = extractPackageFromManifest(moduleConfig.manifestFile)
      ?: "$PACKAGE_PREFIX.${moduleConfig.name}"
    val proguardAttribute = createProguardAttribute(moduleConfig.name)
    val classOutputPath =
      buildDir.resolve(CLASSES_DIR_NAME).createDirectories().toFile().absolutePath

    val content =
      """
      <variant
          name="main"
          minSdkVersion="$MIN_SDK_VERSION"
          targetSdkVersion="$TARGET_SDK_VERSION"
          debuggable="true"
          useSupportLibraryVectorDrawables="true"
          package="$packageName"
          $proguardAttribute>
          <buildFeatures
              coreLibraryDesugaring="true" 
              viewBinding="true" />
          <sourceProviders>
              ${generateMainSourceProvider(moduleConfig)}
          </sourceProviders>
          <testSourceProviders>
              ${generateTestSourceProvider(moduleConfig)}
          </testSourceProviders>
          <mainArtifact
              classOutputs="$classOutputPath"
              applicationId="$packageName">
          </mainArtifact>
      </variant>
      """.trimIndent()

    variantFile.writeText(content)
  }

  private fun generateMainSourceProvider(moduleConfig: ModuleConfig): String {
    val attributes = buildList {
      add("""manifest="${File(moduleConfig.manifestFile).absolutePath}"""")

      val javaDir = File(repoRoot, "${moduleConfig.name}/src/main/java").absolutePath
      add("""javaDirectories="$javaDir"""")

      val mainResDirs = moduleConfig.resourceDirs
        .map { File(it).absolutePath }
        .filter { it.contains("/src/main/") }
      if (mainResDirs.isNotEmpty()) {
        add("""resDirectories="${mainResDirs.joinToString(",")}"""")
      }

      val assetsDir = File(repoRoot, "${moduleConfig.name}/src/main/assets")
      if (assetsDir.exists()) {
        add("""assetsDirectories="${assetsDir.absolutePath}"""")
      }
    }

    return createSourceProviderXml(attributes)
  }

  private fun generateTestSourceProvider(moduleConfig: ModuleConfig): String {
    val attributes = buildList {
      val testManifest = File(repoRoot, "${moduleConfig.name}/src/test/AndroidManifest.xml")
      if (testManifest.exists()) {
        add("""manifest="${testManifest.absolutePath}"""")
      }

      val testJavaDirs = listOf(
        File(repoRoot, "${moduleConfig.name}/src/test/java").absolutePath,
        File(repoRoot, "${moduleConfig.name}/src/sharedTest/java").absolutePath
      ).filter { File(it).exists() }

      if (testJavaDirs.isNotEmpty()) {
        add("""javaDirectories="${testJavaDirs.joinToString(",")}"""")
      }

      val testResDirs = moduleConfig.resourceDirs
        .map { File(it).absolutePath }
        .filter { it.contains("/src/test/") }
      if (testResDirs.isNotEmpty()) {
        add("""resDirectories="${testResDirs.joinToString(",")}"""")
      }

      val testAssetsDir = File(repoRoot, "${moduleConfig.name}/src/test/assets")
      if (testAssetsDir.exists()) {
        add("""assetsDirectories="${testAssetsDir.absolutePath}"""")
      }
    }

    return if (attributes.isEmpty()) {
      "<sourceProvider />"
    } else {
      createSourceProviderXml(attributes)
    }
  }

  private fun createSourceProviderXml(attributes: List<String>): String {
    val attributesString = attributes.joinToString("\n              ")
    return """
      <sourceProvider
              $attributesString
          />
    """.trimIndent()
  }

  private fun createProguardAttribute(moduleName: String): String {
    if (moduleName != ModuleName.APP.moduleName) return ""

    val proguardDir = File(repoRoot, PROGUARD_CONFIG_PATH)
    val proguardFiles = proguardDir
      .takeIf { it.isDirectory }
      ?.listFiles { file -> file.name.endsWith(".pro") }
      ?.map { it.absolutePath }
      ?: emptyList()

    return if (proguardFiles.isNotEmpty()) {
      """proguardFiles="${proguardFiles.joinToString(",")}" """
    } else {
      ""
    }.trimEnd()
  }

  private fun generateArtifactLibrariesXml(librariesFile: File) {
    librariesFile.writeText("<libraries>\n</libraries>")
  }

  private fun generateDependenciesXml(dependenciesFile: File) {
    dependenciesFile.writeText("<dependencies>\n</dependencies>")
  }

  private fun extractPackageFromManifest(manifestPath: String): String? {
    return try {
      val manifestFile = File(manifestPath)
      val content = manifestFile.readText()
      val packageRegex = Regex("""package\s*=\s*["']([^"']+)["']""")
      packageRegex.find(content)?.groupValues?.get(1)
    } catch (e: Exception) {
      null
    }
  }
}
