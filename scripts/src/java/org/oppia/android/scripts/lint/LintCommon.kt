package org.oppia.android.scripts.lint

import java.io.File
import java.time.Instant

/**
 * Enum representing layer names in the project.
 *
 * @property layerName The name of the layer as a string.
 */
enum class LayerName(val layerName: String) {
  /** Represents the app layer. */
  APP("app"),

  /** Represents the domain layer. */
  DOMAIN("domain"),

  /** Represents the testing layer. */
  TESTING("testing"),

  /** Represents the utility layer. */
  UTILITY("utility"),

  /** Represents the data layer. */
  DATA("data");

  companion object {
    /** The application layer instance. */
    val APPLICATION_LAYER = APP

    /** list of library layers in the project. */
    val LIBRARY_LAYERS = listOf(DOMAIN, TESTING, UTILITY, DATA)
  }
}

/** Represents layer configuration for lint project description. */
data class LayerConfig(
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
  val annotationZips: List<String>,
  val lintCheckJars: List<String>,
  val lintModelDir: File? = null,
  val partialResultsDir: File,
  val proGuardFiles: List<String>
) {
  init {
    require(name.isNotBlank()) { "Layer name cannot be blank" }

    require(partialResultsDir.canWrite() || partialResultsDir.mkdirs()) {
      "Cannot create or write to partialResultsDir: ${partialResultsDir.absolutePath}"
    }

    val missingProguardFiles = proGuardFiles.filterNot { File(it).exists() }
    if (missingProguardFiles.isNotEmpty()) {
      throw IllegalArgumentException(
        "ProGuard files do not exist: ${missingProguardFiles.joinToString(", ")}"
      )
    }

    val missingAarFiles = aarFiles.filterNot { File(it.originalPath).exists() }
    if (missingAarFiles.isNotEmpty()) {
      throw IllegalArgumentException(
        "AAR files do not exist: ${missingAarFiles.joinToString(", ") { it.originalPath }}"
      )
    }

    val missingResourceDirs = resourceDirs.filterNot { File(it).exists() && File(it).isDirectory }
    if (missingResourceDirs.isNotEmpty()) {
      throw IllegalArgumentException(
        "Missing or invalid resource directories: ${missingResourceDirs.joinToString(", ")}"
      )
    }

    val missingSrcFiles = srcFiles.filterNot { File(it).exists() }
    if (missingSrcFiles.isNotEmpty()) {
      throw IllegalArgumentException(
        "Missing source files: ${missingSrcFiles.joinToString(", ")}"
      )
    }

    val missingTestFiles = testFiles.filterNot { File(it).exists() }
    if (missingTestFiles.isNotEmpty()) {
      throw IllegalArgumentException(
        "Missing test files: ${missingTestFiles.joinToString(", ")}"
      )
    }

    val missingJarFiles = jarFiles.filterNot { File(it).exists() }
    if (missingJarFiles.isNotEmpty()) {
      throw IllegalArgumentException(
        "Missing JAR files: ${missingJarFiles.joinToString(", ")}"
      )
    }
  }
}

/** Information about an AAR file and its extraction location. */
data class AarFileInfo(
  val originalPath: String,
  val extractedPath: String
)

/** Logger for error messages during lint analysis. */
class LintLogger(workingDirectory: File) {
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

/** Java configuration class. */
class JavaConfiguration(bazelInfo: Map<String, String>) {
  private val jdkHome: File
  private val version: String
  companion object {
    private const val JAVA_HOME_KEY = "java-home"
    private const val JAVA_RUNTIME_KEY = "java-runtime"
  }
  init {
    jdkHome = File(
      bazelInfo[JAVA_HOME_KEY] ?: error("$JAVA_HOME_KEY not found in bazel info output")
    )

    val javaRuntime = bazelInfo[JAVA_RUNTIME_KEY]
      ?: error("$JAVA_RUNTIME_KEY not found in bazel info output")

    val versionRegex = Regex("""build (\d+\.\d+\.\d+)""")
    version = versionRegex.find(javaRuntime)
      ?.groupValues?.get(1)
      ?: error("Could not extract Java version from: $javaRuntime")
  }

  /** Retrieves the JDK home directory. */
  fun getJdkHome(): File = jdkHome

  /** Retrieves the Java version. */
  fun getVersion(): String = version
}
