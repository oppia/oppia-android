package org.oppia.android.scripts.lint

import com.android.SdkConstants
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipException
import java.util.zip.ZipFile

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
class CacheManager {
  companion object {
    // Dependencies need to be fresh enough to catch recent changes
    // Long enough to avoid excessive Bazel queries during rapid successive lint runs
    private const val DEPENDENCIES_TTL = 300L // 5 minutes

    // Paths are more stable than dependencies
    // External dependency paths don't change unless the dependency version changes
    private const val PATH_RESOLUTION_TTL = 600L // 10 minutes

    // Once extracted, the contents remain valid until the AAR version changes
    // AAR files don't change unless dependency versions are updated therefore stable
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

/**
 * Generates lint project description XML files for Android projects.
 *
 * @param repoRoot The root directory of the repository
 * @param workingDirectory The working directory where files will be generated
 * @param commandExecutor executes the specified command in the specified working directory
 */
class LintProjectDescription(
  private val repoRoot: File,
  private val workingDirectory: File,
  commandExecutor: CommandExecutor,
  private val changedFiles: Set<String>? = null
) {

  private val bazelClient = BazelClient(repoRoot, commandExecutor)
  companion object {
    private const val LINT_PROJECT_DESCRIPTION_FILE_NAME = "lint-project-description.xml"
    private const val LINT_CACHE_DIRECTORY_NAME = "lint-cache-directory"
    private const val EXTRACTED_AARS_DIRECTORY_NAME = "extracted-aars"
    private const val LINT_MODELS_DIRECTORY = "models-directory"
    private const val PARTIAL_RESULTS_DIRECTORY = "partial-results-directory"

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
  private val logger = LintLogger(workingDirectory)

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
    val partialResultsDirectory = ensureDirectoryExists(
      File(workingDirectory, PARTIAL_RESULTS_DIRECTORY)
    )

    val layerConfigBuilder = LayerConfigurationBuilder(
      repoRoot, bazelClient, extractedAarsDirectory,
      modelsDirectory, partialResultsDirectory,
      cacheManager,
      logger,
      changedFiles
    )
    val initialLayerConfigs = layerConfigBuilder.buildAllLayerConfigurations()
    val layerConfigs = layerConfigBuilder.buildModelDirectory(initialLayerConfigs)

    val xmlContent = generateProjectXmlContent(cacheDirectory, layerConfigs)

    return writeProjectDescriptionFile(projectDescriptionFile, xmlContent)
  }

  private fun generateProjectXmlContent(
    cacheDirectory: File,
    layerConfigs: List<LayerConfig>
  ): String = buildString {
    appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
    appendLine("<project>")
    appendLine("  <root dir='${repoRoot.absolutePath}'/>")
    appendLine("  <cache dir='${cacheDirectory.absolutePath}'/>")
    appendLine()

    layerConfigs.forEach { config ->
      append(generateModuleXml(config))
      appendLine()
    }

    appendLine("</project>")
  }

  private fun generateModuleXml(config: LayerConfig): String = buildString {
    appendLine("  <module")
    appendLine("""    name="${config.name}"""")
    appendLine("""    android="${config.isAndroid}"""")
    appendLine("""    library="${config.isLibrary}"""")
    appendLine("""    test="${config.isTest}"""")

    appendLine("""    model="${config.lintModelDir?.absolutePath}"""")

    appendLine("""    partial-results="${config.partialResultsDir.absolutePath}"""")

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

    config.proGuardFiles.forEach { proGuardFile ->
      appendLine("""    <proguard file="$proGuardFile"/>""")
    }

    config.jarFiles.forEach { jarFile ->
      appendLine("""    <classpath jar="$jarFile"/>""")
    }

    config.lintCheckJars.forEach { lintCheckJar ->
      appendLine("""    <lint-checks jar="$lintCheckJar"/>""")
    }

    config.annotationZips.forEach { annotationZip ->
      appendLine("""    <annotations file="$annotationZip"/>""")
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

/** Builds layer configurations for all layers in the project. */
private class LayerConfigurationBuilder(
  private val repoRoot: File,
  bazelClient: BazelClient,
  extractedAarsDirectory: File,
  private val modelsDirectory: File,
  private val partialResultsDirectory: File,
  cacheManager: CacheManager,
  private val logger: LintLogger,
  private val changedFiles: Set<String>?
) {

  companion object {
    // These dependencies are defined at a project level for approximate correspondence.
    private val LAYER_DEPENDENCIES = mapOf(
      LayerName.APP to LayerName.LIBRARY_LAYERS,
      LayerName.TESTING to listOf(LayerName.UTILITY, LayerName.DOMAIN),
      LayerName.DOMAIN to listOf(LayerName.UTILITY),
      LayerName.DATA to listOf(LayerName.UTILITY)
    )
    private const val ANDROID_MANIFEST_PATH = "src/main/${SdkConstants.FN_ANDROID_MANIFEST_XML}"
  }

  private val dependencyResolver = DependencyResolver(
    bazelClient, repoRoot, extractedAarsDirectory,
    cacheManager,
    logger
  )
  private val bazelInfo = bazelClient.retrieveBazelInfo()

  /**
   * Builds configurations for all layers in the project.
   *
   * Before any layer is configured, this verifies that every AAR/JAR artifact Bazel declares for
   * the project's layers physically exists (see [DependencyResolver.verifyDeclaredDependencies]),
   * so that lint is never run against a silently incomplete classpath.
   *
   * @throws IllegalStateException if any declared AAR/JAR dependency is missing from disk
   */
  fun buildAllLayerConfigurations(): List<LayerConfig> {
    dependencyResolver.verifyDeclaredDependencies(
      listOf(LayerName.APPLICATION_LAYER) + LayerName.LIBRARY_LAYERS
    )
    return buildList {
      add(buildLayerConfiguration(LayerName.APPLICATION_LAYER, isLibrary = false))

      LayerName.LIBRARY_LAYERS.forEach { layer ->
        add(buildLayerConfiguration(layer, isLibrary = true))
      }
    }
  }

  /** Builds configuration for a single code layer. */
  private fun buildLayerConfiguration(layer: LayerName, isLibrary: Boolean): LayerConfig {
    val sourceCollector = SourceFileCollector(repoRoot, layer, changedFiles)
    val (testFiles, srcFiles) = sourceCollector.collectSourceFiles()
      .partition { path ->
        path.contains("/test/") ||
          path.contains("/sharedTest/")
      }
    val partialResultDir = File(
      partialResultsDirectory, "${layer.layerName}-partial-results"
    ).apply {
      if (!exists() && !mkdirs()) {
        throw IllegalStateException("Failed to create partial results directory: $absolutePath")
      }
    }
    val annotationZips = try {
      dependencyResolver.extractAnnotationZips(
        dependencyResolver.resolveAarFiles(layer)
      )
    } catch (e: Exception) {
      logger.logError("Failed to extract annotation zips: ${e.message}")
      emptyList()
    }
    return LayerConfig(
      name = layer.layerName,
      isAndroid = true,
      isLibrary = isLibrary,
      isTest = layer == LayerName.TESTING,
      srcFiles = srcFiles,
      testFiles = testFiles,
      resourceDirs = sourceCollector.collectResourceDirectories(),
      manifestFile = findManifestFile(layer),
      dependencies = LAYER_DEPENDENCIES[layer]?.map { it.layerName }.orEmpty(),
      aarFiles = dependencyResolver.resolveAarFiles(layer),
      jarFiles = dependencyResolver.resolveJarFiles(layer),
      lintCheckJars = dependencyResolver.extractLintCheckJars(
        dependencyResolver.resolveAarFiles(layer)
      ),
      partialResultsDir = partialResultDir,
      annotationZips = annotationZips,
      proGuardFiles = sourceCollector.collectProGuardFiles(layer.layerName)
    )
  }

  /** Builds the model directory for each layer configuration. */
  fun buildModelDirectory(layerConfigs: List<LayerConfig>): List<LayerConfig> {
    return layerConfigs.map { layerConfig ->
      val modelDirectory = File(modelsDirectory, layerConfig.name)

      if (!modelDirectory.exists() && !modelDirectory.mkdirs()) {
        throw IllegalStateException(
          "Failed to create model directory: ${modelDirectory.absolutePath}"
        )
      }

      val modelCreator = LintModelCreator(modelDirectory, repoRoot, bazelInfo)
      val generatedModelDir = modelCreator.generateModelFiles(layerConfig)

      layerConfig.copy(lintModelDir = generatedModelDir)
    }
  }

  private fun findManifestFile(layer: LayerName): String {
    val manifestPath = File(repoRoot, "${layer.layerName}/$ANDROID_MANIFEST_PATH")
    require(manifestPath.exists()) {
      "Manifest file not found for layer: ${layer.layerName} at ${manifestPath.absolutePath}"
    }
    return manifestPath.absolutePath
  }
}

/** Helper class for collecting source files and resources for a layer. */
private class SourceFileCollector(
  private val repoRoot: File,
  layer: LayerName,
  private val changedFiles: Set<String>?
) {
  companion object {
    private val SOURCE_EXTENSIONS = setOf("kt", "java")
    private const val PROGUARD_CONFIG_PATH = "config/proguard"

    // This file is Bazel-specific and used solely for running tests.
    // Lint reports it as being in an incorrect project location as it's not part of the standard source set.
    // Since the lint tool does not analyze this file, we explicitly exclude it
    // similar to how Gradle source sets exclude this file.
    private const val EXCLUDED_SOURCE_FILE = "DataBinderMapperImpl.java"
  }

  private val layerName = layer.layerName
  private val sourceDir = File(repoRoot, "$layerName/${SdkConstants.FD_SOURCES}")

  /**
   * Collects the source files for the layer.
   *
   * When [changedFiles] is null, all source files are collected (full mode).
   * When [changedFiles] is an empty set, no source files are collected (--no-java-sources mode).
   * When [changedFiles] has entries, only files whose repo-relative paths match are collected
   * (incremental mode).
   */
  fun collectSourceFiles(): List<String> {
    return when {
      changedFiles == null -> collectFilesFromDirectory(sourceDir)
      changedFiles.isEmpty() -> emptyList()
      else -> collectFilesFromDirectory(sourceDir).filter { absolutePath ->
        File(absolutePath).relativeTo(repoRoot).path in changedFiles
      }
    }
  }

  /** Collects the resource directories for the layer. */
  fun collectResourceDirectories(): List<String> = buildList {
    if (sourceDir.exists()) {
      sourceDir.walkTopDown()
        .filter { it.isDirectory && it.name == SdkConstants.FD_RES }
        .forEach { add(it.path) }
    }
  }

  /** Collects the proguard files for the layer. */
  fun collectProGuardFiles(layerName: String): List<String> {
    if (layerName != LayerName.APP.layerName) return emptyList()

    val proguardDir = File(repoRoot, PROGUARD_CONFIG_PATH)

    return proguardDir
      .takeIf { it.isDirectory && it.exists() }
      ?.listFiles { file -> file.name.endsWith(".pro") }
      ?.map { it.absolutePath }
      ?: emptyList()
  }

  private fun collectFilesFromDirectory(directory: File): List<String> {
    require(directory.exists() && directory.isDirectory) {
      throw IllegalStateException("Source directory does not exist at: $directory")
    }

    return directory.walkTopDown()
      .filter {
        it.isFile && it.extension in SOURCE_EXTENSIONS &&
          it.name != EXCLUDED_SOURCE_FILE
      }
      .map { it.absolutePath }
      .toList()
  }
}

/** Helper class for resolving layer dependencies. */
private class DependencyResolver(
  private val bazelClient: BazelClient,
  repoRoot: File,
  private val extractedAarsDirectory: File,
  private val cacheManager: CacheManager,
  logger: LintLogger
) {
  companion object {
    /** The maximum number of missing artifacts itemized in the missing-dependency error. */
    private const val MAX_REPORTED_MISSING_ARTIFACTS = 25

    /**
     * Bazel build-rule tool packages whose outputs are never part of the Lint classpath.
     * These are build-system internals (annotation processors, Kotlin compiler, JVM external
     * resolution tools) that cquery declares but that Lint never needs to resolve symbols.
     */
    private val BUILD_TOOL_PACKAGE_FRAGMENTS = listOf(
      "rules_jvm_external/",
      "rules_java/",
      "io_bazel_rules_kotlin/",
      "remote_java_tools/"
    )

    /**
     * JAR name suffixes that identify compile-avoidance and annotation-processing intermediate
     * artifacts. These are produced by the Bazel Kotlin/Java rules as build-system optimisations
     * and are never placed on the Lint classpath.
     *
     * Concretely:
     *  - `-hjar.jar`, `-java-hjar.jar`: header (ABI) JARs for incremental compilation
     *  - `.abi.jar`: ABI stubs used by the Kotlin compiler for interface checking
     *  - `-native-header.jar`: C/C++ interop header outputs
     *  - `-kapt-generated-stub.jar`: annotation-processor stubs before full KAPT round
     *  - `-gensrc.jar`, `-kapt-gensrc.jar`, `-java-gensrc.jar`, `.srcjar`, `-src.jar`: generated
     *    source JARs (contain `.java`/`.kt` files, not `.class` files)
     *  - `_kt-kt.jar`, `_kt-java.jar`: compilation slices merged into the final `_kt.jar`
     */
    private val COMPILE_AVOIDANCE_JAR_SUFFIXES = listOf(
      "-hjar.jar",
      "-java-hjar.jar",
      ".abi.jar",
      "-native-header.jar",
      "-kapt-generated-stub.jar",
      "-gensrc.jar",
      "-kapt-gensrc.jar",
      "-java-gensrc.jar",
      ".srcjar",
      "-src.jar",
      "_kt-kt.jar",
      "_kt-java.jar"
    )

    /**
     * JAR name suffixes that identify terminal internal test runner binary outputs (e.g.
     * `ActivityTest_test.jar`, `ActivityTest_deploy.jar`). These are only excluded for internal
     * workspace outputs so that external prebuilt deploy JARs (such as
     * `external/android_tools/all_android_tools_deploy.jar`, which supplies Lint-compatible
     * Kotlin stdlib metadata for Android Lint 30.3.1 when the project compiles with Kotlin 1.9+)
     * remain available on the Lint classpath.
     */
    private val INTERNAL_TEST_RUNNER_JAR_SUFFIXES = listOf(
      "_test.jar",
      "_deploy.jar"
    )
  }

  /**
   * Checks if [declaredPath] represents an internal module AAR artifact.
   *
   * In Bazel, `kt_android_library` rules declare a default `.aar` output under their package
   * directory (e.g. `bazel-out/.../bin/app/app.aar`). These internal AAR outputs are never
   * generated during standard application compilation and are not consumed by Lint because Lint
   * models internal layers via `<dep module="..."/>` with their source files and compiled class
   * JARs. Only external AAR dependencies (such as Maven AARs) are consumed as `<aar .../>`.
   */
  private fun isInternalModuleAar(declaredPath: String): Boolean {
    if (!declaredPath.endsWith(".aar")) return false
    val isExternal = declaredPath.startsWith("external/") || declaredPath.contains("/external/")
    if (isExternal) return false
    for (layer in LayerName.values()) {
      val name = layer.layerName
      val binPrefix = "/bin/" + name + "/"
      val startPrefix = name + "/"
      val midPrefix = "/" + name + "/"
      if (declaredPath.contains(binPrefix) ||
        declaredPath.startsWith(startPrefix) ||
        declaredPath.contains(midPrefix)
      ) {
        return true
      }
    }
    return false
  }

  /**
   * Checks if [declaredPath] represents an extracted intermediate JAR from an AAR.
   *
   * When Bazel imports an external Maven AAR via `aar_import`, it generates an intermediate
   * `_aar/.../classes_and_libs_merged.jar` or `_aar/.../classes.jar`. Because Lint receives
   * the original `.aar` file as `<aar file="..." extracted="..."/>` and extracts both
   * `classes.jar` and nested JARs in libs/ directly, including the intermediate merged JAR
   * on the classpath is redundant with the retained AAR.
   */
  private fun isIntermediateAarJar(declaredPath: String): Boolean {
    val isExtractedAar = declaredPath.contains("_aar/")
    val isMergedOrClassesJar = declaredPath.endsWith("classes_and_libs_merged.jar") ||
      declaredPath.endsWith("classes.jar")
    return isExtractedAar && isMergedOrClassesJar
  }

  /**
   * Checks if [declaredPath] represents an external AAR resource symbols JAR.
   *
   * Bazel's AAR import rules produce synthetic `*_resources.jar` files for external AARs.
   * Lint reads resources directly from the extracted AAR `res/` directory and does not need
   * these synthetic symbols on the classpath.
   */
  private fun isExternalAarResourcesJar(declaredPath: String): Boolean {
    val isExternalMaven = declaredPath.startsWith("external/maven/") ||
      declaredPath.contains("/external/maven/")
    return isExternalMaven && declaredPath.endsWith("_resources.jar")
  }

  /**
   * Checks if [declaredPath] is a file in a fetched external repository (e.g. a Maven AAR/JAR under
   * 'external/...').
   *
   * Unlike 'bazel-out/...' outputs, these are materialized by Bazel's repository fetch rather than
   * by building a particular target, so they exist regardless of which targets were built. Every
   * 'bazel-out/...' output (internal libraries, test targets, and generated copies of external
   * artifacts) only exists once its specific producing target has been built, and `cquery
   * deps(//layer:*)` declares such outputs for every test in the layer. Requiring those would mean
   * building the entire test suite before Lint, so they can't be a precondition for running Lint.
   *
   * Note that 'external/androidsdk/dummy.jar' is excluded: it's a placeholder output declared by the
   * Android SDK repository rule that is intentionally never created.
   */
  private fun isFetchedExternalRepoFile(declaredPath: String): Boolean =
    declaredPath.startsWith("external/") && declaredPath != "external/androidsdk/dummy.jar"

  /**
   * Returns true when [declaredPath] is a genuine JAR or AAR artifact that belongs on the Lint
   * classpath or AAR dependency list and therefore must exist on disk before Lint can run.
   *
   * Artifacts that are excluded:
   * - Internal module AARs (see [isInternalModuleAar]).
   * - Intermediate AAR extracted JARs (see [isIntermediateAarJar]).
   * - External AAR resource JARs (see [isExternalAarResourcesJar]).
   * - Compile-avoidance / annotation-processing intermediate JARs (see
   *   [COMPILE_AVOIDANCE_JAR_SUFFIXES]) and internal test runner binary outputs (see
   *   [INTERNAL_TEST_RUNNER_JAR_SUFFIXES]).
   * - Bazel build-rule tool packages (see [BUILD_TOOL_PACKAGE_FRAGMENTS]) — these are
   *   build-system internals, not application code.
   * - Outputs built in a Bazel *exec* configuration (paths containing `k8-opt-exec-` or
   *   `-exec-`), which are build-time tools rather than runtime classpath entries.
   */
  private fun isLintClasspathArtifact(declaredPath: String): Boolean {
    // Internal module AARs are not external AAR dependencies.
    if (isInternalModuleAar(declaredPath)) return false

    // Intermediate AAR extracted JARs are redundant with the retained AAR.
    if (isIntermediateAarJar(declaredPath)) return false

    // External AAR resource JARs are not needed since Lint extracts the AAR's res/ directory.
    if (isExternalAarResourcesJar(declaredPath)) return false

    // Compile-avoidance, annotation-processing stubs, and source JARs.
    if (COMPILE_AVOIDANCE_JAR_SUFFIXES.any { declaredPath.endsWith(it) }) return false

    // Internal test runner binary outputs (external prebuilt *_deploy.jar tools are retained).
    val isExternal = declaredPath.startsWith("external/") || declaredPath.contains("/external/")
    if (!isExternal && INTERNAL_TEST_RUNNER_JAR_SUFFIXES.any { declaredPath.endsWith(it) }) {
      return false
    }

    // Bazel build-rule tool packages (rules_jvm_external, rules_java, etc.).
    if (BUILD_TOOL_PACKAGE_FRAGMENTS.any { declaredPath.contains(it) }) return false

    // Exec-configuration build tools (never on the runtime/lint classpath).
    if (declaredPath.contains("k8-opt-exec-") || declaredPath.contains("-exec-/")) return false

    return true
  }

  private val pathResolver = PathResolver(repoRoot, bazelClient, cacheManager, logger)
  private val aarExtractor = AarExtractor(cacheManager)

  /**
   * Verifies that every Lint-classpath AAR and JAR artifact declared by Bazel for each of the
   * provided [layers] exists on disk.
   *
   * `bazel cquery --output=files` reports the *declared* outputs of each target, including many
   * build-system intermediate artifacts (compile-avoidance JARs, build-tool JARs, exec-config
   * outputs) that are never part of the Lint classpath. Only the artifacts identified as
   * Lint-classpath entries by [isLintClasspathArtifact] that are also fetched external repository
   * files (see [isFetchedExternalRepoFile]) are checked; all others are ignored here (and, if they
   * are missing, are skipped with a logged error when the classpath is resolved).
   *
   * Without this check, missing artifacts would be silently dropped from the lint classpath,
   * making lint results depend on whatever happened to be built beforehand (e.g. clean vs.
   * previously built workspaces).
   *
   * This intentionally never triggers a Bazel build; it only reports what is missing.
   *
   * @throws IllegalStateException if any Lint-classpath AAR/JAR is missing from disk. The message
   *     lists the missing artifacts and the layers that depend on them.
   */
  fun verifyDeclaredDependencies(layers: List<LayerName>) {
    // Maps each missing declared path to its resolved location & the layers that declare it.
    val missingArtifacts = linkedMapOf<String, Pair<String, MutableList<String>>>()
    val artifactExtensions = listOf(".${SdkConstants.EXT_AAR}", ".${SdkConstants.EXT_JAR}")
    layers.forEach { layer ->
      getDependenciesWithCache(layer.layerName)
        .filter { declaredPath ->
          artifactExtensions.any { declaredPath.endsWith(it) } &&
            isFetchedExternalRepoFile(declaredPath) && isLintClasspathArtifact(declaredPath)
        }
        .forEach { declaredPath ->
          val resolvedPath = pathResolver.computeAbsolutePath(declaredPath)
          if (!File(resolvedPath).exists()) {
            missingArtifacts.getOrPut(declaredPath) { resolvedPath to mutableListOf<String>() }
              .second.add(layer.layerName)
          }
        }
    }
    if (missingArtifacts.isEmpty()) return

    val listedArtifacts = missingArtifacts.entries.take(MAX_REPORTED_MISSING_ARTIFACTS)
      .joinToString(separator = "\n") { (declaredPath, resolvedAndLayers) ->
        val (resolvedPath, declaringLayers) = resolvedAndLayers
        "  - $declaredPath (expected at $resolvedPath; needed by layer(s): " +
          "${declaringLayers.joinToString(", ")})"
      }
    val omittedCount = missingArtifacts.size - MAX_REPORTED_MISSING_ARTIFACTS
    val omittedNote = if (omittedCount > 0) "\n  ... and $omittedCount more." else ""
    val affectedLayers = missingArtifacts.values.flatMap { it.second }.distinct().joinToString(", ")
    throw IllegalStateException(
      "Android Lint cannot run: ${missingArtifacts.size} AAR/JAR artifact(s) declared by Bazel " +
        "for the lint classpath do not exist on disk.\n" +
        "Bazel only declares these outputs; they are created once the targets that produce them " +
        "have been built, so they are missing in a clean or partially built workspace. Running " +
        "lint without them would silently produce an incomplete classpath and inconsistent " +
        "results.\n" +
        "Missing artifacts:\n$listedArtifacts$omittedNote\n" +
        "Fetch/build the Bazel dependencies that produce these artifacts (e.g. 'bazel build " +
        "//scripts:android_lint_check'; affected layers: $affectedLayers) and re-run lint. See " +
        "https://github.com/oppia/oppia-android/wiki/Android-Lint-Check for details."
    )
  }

  /** Resolves the AAR files for the given layer. */
  fun resolveAarFiles(layer: LayerName): List<AarFileInfo> {
    val allDependencies = getDependenciesWithCache(layer.layerName)
    val aarFiles = allDependencies.filter {
      it.endsWith(".${SdkConstants.EXT_AAR}") && isLintClasspathArtifact(it)
    }

    if (aarFiles.isEmpty()) {
      return emptyList()
    }

    val layerAarsDirectory = ensureDirectoryExists(File(extractedAarsDirectory, layer.layerName))

    return aarFiles.mapNotNull { aarFile ->
      processAarFile(aarFile, layerAarsDirectory)
    }
  }

  /** Resolves the JAR files for the given layer. */
  fun resolveJarFiles(layer: LayerName): List<String> {
    val allDependencies = getDependenciesWithCache(layer.layerName)
    return allDependencies
      .filter { it.endsWith(".${SdkConstants.EXT_JAR}") && isLintClasspathArtifact(it) }
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

  /** Extracts annotation zip files from the given list of AAR files. */
  fun extractAnnotationZips(aarFiles: List<AarFileInfo>): List<String> =
    aarFiles.mapNotNull { aarInfo ->
      val extractedDir = File(aarInfo.extractedPath)
      if (!extractedDir.exists() || !extractedDir.isDirectory) {
        throw IllegalArgumentException(
          "AAR extracted path does not exist or " +
            "is not a directory: ${aarInfo.extractedPath}"
        )
      }

      val annotationZip = File(extractedDir, "annotations.zip")
      if (annotationZip.exists()) annotationZip.absolutePath else null
    }

  private fun getDependenciesWithCache(layerName: String): List<String> =
    cacheManager.getDependencies(layerName) {
      bazelClient.retrieveTargetModuleDependencies("//$layerName:*")
    }

  private fun processAarFile(aarFile: String, layerAarsDirectory: File): AarFileInfo? {
    val resolvedAarPath = pathResolver.resolveBazelPath(aarFile)
      ?: return null

    val aarFileObj = File(resolvedAarPath)
    require(aarFileObj.exists()) {
      "AAR file does not exist: $resolvedAarPath"
    }

    return try {
      val extractedPath = aarExtractor.extractAar(resolvedAarPath, layerAarsDirectory)
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
  private val logger: LintLogger
) {
  companion object {
    private const val BAZEL_OUTPUT_BASE_KEY = "output_base"
  }

  /**
   * Computes the absolute file system location where Bazel declares [path] to be, without checking
   * whether the file exists.
   */
  fun computeAbsolutePath(path: String): String =
    when {
      File(path).isAbsolute -> path
      path.startsWith("external/") -> resolveExternalPath(path)
      else -> File(repoRoot, path).absolutePath
    }

  /** Resolves a Bazel path to an absolute file system path. */
  fun resolveBazelPath(path: String): String? =
    cacheManager.getPathResolution(path) {
      val resolvedPath = computeAbsolutePath(path)

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
  fun extractAar(aarFilePath: String, layerAarsDirectory: File): String =
    cacheManager.getAarExtraction(aarFilePath) {
      performAarExtraction(aarFilePath, layerAarsDirectory)
    }

  private fun performAarExtraction(aarFilePath: String, layerAarsDirectory: File): String {
    val aarFile = File(aarFilePath)
    require(aarFile.exists()) {
      "AAR file does not exist: $aarFilePath"
    }

    val safeName = createSafeDirectoryName(aarFile.nameWithoutExtension)
    val extractedDir = File(layerAarsDirectory, safeName)

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
