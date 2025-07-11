package org.oppia.android.scripts.lint

import com.android.tools.lint.model.LintModelModuleType.APP
import com.android.tools.lint.model.LintModelModuleType.LIBRARY
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import java.io.File
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories

/**
 * This class generates the XML configuration files that Android Lint requires
 * to analyze Bazel-based Android projects. The generated files include:
 * - module.xml: Module configuration and metadata
 * - main.xml: Variant configuration with source providers
 * - dependencies.xml: Module dependencies (stub file)
 * - libraries.xml: External libraries (stub file)
 *
 * @param modelDir Directory where the generated lint model files will be stored
 * @param repoRoot Root directory of the repository containing the Android modules
 * @param bazelInfo Map containing Bazel workspace related information
 */
class LintModelCreator(
  private val modelDir: File,
  private val repoRoot: File,
  private val bazelInfo: Map<String, String>
) {
  companion object {
    // File names for the generated lint model files
    private const val MODULE_XML_FILE = "module.xml"
    private const val VARIANT_XML_FILE = "main.xml"
    private const val ARTIFACT_LIBRARIES_XML_FILE = "main-mainArtifact-libraries.xml"
    private const val DEPENDENCIES_XML_FILE = "main-mainArtifact-dependencies.xml"
    private const val CACHE_METADATA_FILE = ".lint-model-cache"

    // Directory names for the generated model files
    private const val BUILD_DIR_NAME = "build"
    private const val CLASSES_DIR_NAME = "classes"

    // Build configurations for Android module configuration
    private const val PACKAGE_PREFIX = "org.oppia.android"
    private const val MIN_SDK_VERSION = "21"
    private const val TARGET_SDK_VERSION = "34"

    // Model directories are stable and won't change frequently therefore a longer TTL
    private const val MODEL_CACHE_TTL_HOURS = 24L // 24 hours

    private const val FILE_SEPARATOR = ","

    private const val PROGUARD_CONFIG_PATH = "config/proguard"
  }

  private val logger = LintLogger(workingDirectory = modelDir)
  private val sdkProperties = AndroidBuildSdkProperties()

  /**
   * Generates all required lint model files for the specified module configuration.
   * Uses caching to avoid regeneration when inputs haven't changed.
   *
   * @param moduleConfig Configuration object containing module-specific settings
   * @return The directory containing the generated model files
   */
  fun generateModelFiles(moduleConfig: ModuleConfig): File {
    val bazelInputsHash = computeBazelInputsHash(moduleConfig)

    if (isCacheValid(bazelInputsHash)) {
      return modelDir
    }
    val modelPath = modelDir.toPath().createDirectories()
    val buildDir = modelPath.resolve(BUILD_DIR_NAME).createDirectories()
    val modulePath = File(repoRoot, moduleConfig.name).toPath().absolute()

    generateModuleXml(
      modelPath.resolve(MODULE_XML_FILE).toFile(),
      moduleConfig,
      modulePath,
      buildDir
    )

    generateVariantXml(
      modelDir.resolve(VARIANT_XML_FILE),
      moduleConfig,
      buildDir
    )

    // Generate stub files since dependencies and libraries are already passed in project XML
    generateArtifactLibrariesXml(modelDir.resolve(ARTIFACT_LIBRARIES_XML_FILE))
    generateDependenciesXml(modelDir.resolve(DEPENDENCIES_XML_FILE))

    updateCacheMetadata(bazelInputsHash)

    return modelDir
  }

  /**
   * Computes a hash of all Bazel inputs that could affect lint model generation.
   * This includes:
   * - Module configuration (name, type, dependencies)
   * - Source files and their modification times
   * - Resource directories and their contents
   * - Manifest file content
   * - Bazel build configuration
   */
  private fun computeBazelInputsHash(moduleConfig: ModuleConfig): String {
    val digest = MessageDigest.getInstance("SHA-256")

    digest.update(moduleConfig.name.toByteArray())
    digest.update(moduleConfig.isLibrary.toString().toByteArray())
    digest.update(moduleConfig.isAndroid.toString().toByteArray())
    digest.update(moduleConfig.isTest.toString().toByteArray())

    moduleConfig.dependencies.sorted().forEach { dep ->
      digest.update(dep.toByteArray())
    }

    moduleConfig.srcFiles.sorted().forEach { srcFile ->
      digest.update(srcFile.toByteArray())
      val file = File(srcFile)
      if (file.exists()) {
        digest.update(file.lastModified().toString().toByteArray())
      }
    }

    moduleConfig.testFiles.sorted().forEach { testFile ->
      digest.update(testFile.toByteArray())
      val file = File(testFile)
      if (file.exists()) {
        digest.update(file.lastModified().toString().toByteArray())
      }
    }

    moduleConfig.resourceDirs.sorted().forEach { resDir ->
      digest.update(resDir.toByteArray())
      hashDirectoryContents(File(resDir), digest)
    }

    val manifestFile = File(moduleConfig.manifestFile)
    if (manifestFile.exists()) {
      digest.update(manifestFile.readText().toByteArray())
      digest.update(manifestFile.lastModified().toString().toByteArray())
    }

    moduleConfig.aarFiles.sortedBy { it.originalPath }.forEach { aarInfo ->
      digest.update(aarInfo.originalPath.toByteArray())
      digest.update(aarInfo.extractedPath.toByteArray())
    }

    moduleConfig.jarFiles.sorted().forEach { jarFile ->
      digest.update(jarFile.toByteArray())
      val file = File(jarFile)
      if (file.exists()) {
        digest.update(file.lastModified().toString().toByteArray())
      }
    }

    val relevantBazelKeys = listOf("build_target", "output_base", "workspace")
    relevantBazelKeys.forEach { key ->
      bazelInfo[key]?.let { value ->
        digest.update("$key=$value".toByteArray())
      }
    }

    digest.update(sdkProperties.buildToolsVersion.toByteArray())

    return digest.digest().joinToString("") { "%02x".format(it) }
  }

  /** Recursively hashes the contents of a directory, including file names and modification times. */
  private fun hashDirectoryContents(directory: File, digest: MessageDigest) {
    if (!directory.exists() || !directory.isDirectory) return

    directory.listFiles()?.sortedBy { it.name }?.forEach { file ->
      digest.update(file.name.toByteArray())
      digest.update(file.lastModified().toString().toByteArray())

      if (file.isDirectory) {
        hashDirectoryContents(file, digest)
      } else {
        digest.update(file.length().toString().toByteArray())
      }
    }
  }

  /** Checks if the cache is valid by comparing the stored hash with the current hash. */
  private fun isCacheValid(currentHash: String): Boolean {
    val cacheFile = File(modelDir, CACHE_METADATA_FILE)

    if (!cacheFile.exists()) return false

    // Check if required model files exist
    val requiredFiles = listOf(
      MODULE_XML_FILE,
      VARIANT_XML_FILE,
      ARTIFACT_LIBRARIES_XML_FILE,
      DEPENDENCIES_XML_FILE
    )

    if (requiredFiles.any { !File(modelDir, it).exists() }) {
      return false
    }

    return try {
      val cacheData = cacheFile.readText().trim().split("\n")
      if (cacheData.size != 2) return false

      val storedHash = cacheData[0]
      val timestamp = cacheData[1].toLong()

      // Check if cache is expired
      val now = System.currentTimeMillis()
      val cacheAge = (now - timestamp) / (1000 * 60 * 60)

      if (cacheAge > MODEL_CACHE_TTL_HOURS) {
        return false
      }

      val hashMatches = storedHash == currentHash

      hashMatches
    } catch (e: Exception) {
      logger.logError("Error reading cache metadata: ${e.message}")
      false
    }
  }

  /** Updates the cache metadata file with the new hash and timestamp. */
  private fun updateCacheMetadata(hash: String) {
    val cacheFile = File(modelDir, CACHE_METADATA_FILE)
    try {
      val timestamp = System.currentTimeMillis()
      cacheFile.writeText("$hash\n$timestamp")
    } catch (e: Exception) {
      logger.logError("Failed to update cache metadata: ${e.message}")
    }
  }

  private fun generateModuleXml(
    moduleFile: File,
    moduleConfig: ModuleConfig,
    modulePath: Path,
    buildDir: Path
  ) {
    val moduleType = if (moduleConfig.isLibrary) LIBRARY else APP
    val buildToolsVersion = sdkProperties.buildToolsVersion
    val javaSourceLevel = JavaConfiguration(bazelInfo = bazelInfo).getVersion()
    val buildFolder = escapeXmlAttribute(buildDir.createDirectories().toFile().absolutePath)
    val content =
      """
        <lint-module
            dir="${escapeXmlAttribute(modulePath.toString())}"
            name="${escapeXmlAttribute(moduleConfig.name)}"
            type="${moduleType.name}"
            buildFolder="$buildFolder"
            javaSourceLevel="$javaSourceLevel"
            compileTarget="$buildToolsVersion"
            partialResultsDir="${escapeXmlAttribute(moduleConfig.partialResultsDir.absolutePath)}"
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
    val rawPackageName = extractPackageFromManifest(moduleConfig.manifestFile)
      ?: "$PACKAGE_PREFIX.${moduleConfig.name}"

    val packageName = escapeXmlAttribute(rawPackageName)

    val proguardAttribute = createProguardAttribute(moduleConfig.name)

    val classOutputPath = escapeXmlAttribute(
      buildDir.resolve(CLASSES_DIR_NAME).createDirectories().toFile().absolutePath
    )

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
                viewBinding="true"
                namespacing="REQUIRED" />
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
      add("""manifest="${escapeXmlAttribute(File(moduleConfig.manifestFile).absolutePath)}"""")

      val javaDir = escapeXmlAttribute(
        File(
          repoRoot,
          "${moduleConfig.name}/src/main/java"
        ).absolutePath
      )
      add("""javaDirectories="$javaDir"""")

      val mainResDirs = moduleConfig.resourceDirs
        .map { escapeXmlAttribute(File(it).absolutePath) }
        .filter { it.contains("/src/main/") }
      if (mainResDirs.isNotEmpty()) {
        add("""resDirectories="${mainResDirs.joinToString(FILE_SEPARATOR)}"""")
      }

      val assetsDir = File(repoRoot, "${moduleConfig.name}/src/main/assets")
      if (assetsDir.exists()) {
        add("""assetsDirectories="${escapeXmlAttribute(assetsDir.absolutePath)}"""")
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
        add("""javaDirectories="${testJavaDirs.joinToString(FILE_SEPARATOR)}"""")
      }

      val testResDirs = moduleConfig.resourceDirs
        .map { File(it).absolutePath }
        .filter { it.contains("/src/test/") }
      if (testResDirs.isNotEmpty()) {
        add("""resDirectories="${testResDirs.joinToString(FILE_SEPARATOR)}"""")
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
    val manifestFile = File(manifestPath)

    if (!manifestFile.exists()) {
      logger.logError("Manifest file not found at path: $manifestPath")
      return null
    }

    val packageRegex = Regex("""package\s*=\s*["']([^"']+)["']""")

    return try {
      manifestFile.useLines { lines ->
        for (line in lines) {
          val match = packageRegex.find(line)
          if (match != null) return match.groupValues[1]
        }
      }
      logger.logError("Package attribute not found in manifest: $manifestPath")
      null
    } catch (e: Exception) {
      logger.logError("Error reading manifest: ${e.message}")
      null
    }
  }

  /**
   * Escapes special XML characters in attribute values to prevent XML injection
   * and ensure well-formed XML output.
   */
  private fun escapeXmlAttribute(value: String): String {
    return value.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;")
  }
}
