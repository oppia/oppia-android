package org.oppia.android.scripts.license

/**
 * Represents the coordinate to a unique Maven artifact, as defined by:
 * https://maven.apache.org/repositories/artifacts.html.
 *
 * @property groupId the artifact's group (which is often the artifact author or maintainer)
 * @property artifactId the unique ID for the artifact within its group
 * @property version the artifact's version (which often uses SemVer)
 * @property classifier the unique classifier of the coordinate, e.g. "sources", or null if none
 * @property extension the optional extension of the artifact, or null if not specified
 */
data class MavenCoordinate(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val classifier: String? = null,
  val extension: String? = null
) {
  /**
   * A reduced string representation of this coordinate that ignores any specified [classifier] or
   * [extension].
   */
  val reducedCoordinateString: String get() = "$groupId:$artifactId:$version"

  /** A variation of [reducedCoordinateString] which also omits this coordinate's [version]. */
  val reducedCoordinateStringWithoutVersion: String get() = "$groupId:$artifactId"

  /**
   * A base computation of what the suffix of the Bazel target would look like for the artifact
   * represented by this coordinate. Note that this is specifically the suffix of a Maven-imported
   * artifact target and not one produced by Oppia (so they may look a bit different).
   */
  val bazelTarget: String
    get() = "${groupId.bazelifyCoordFragment()}_${artifactId.bazelifyCoordFragment()}"

  private val delimitedClassifierUrlFragment get() = classifier?.let { "-$it" } ?: ""

  /**
   * Returns the downloadable URL for the main file of the artifact represented by this
   * coordinate.
   *
   * Note that per https://maven.apache.org/repositories/artifacts.html the extension will assumed
   * to be 'jar' if an [extension] hasn't been provided.
   */
  fun computeArtifactUrl(baseRepoUrl: String): String =
    computeArtifactFileUrl(baseRepoUrl, extension ?: "jar")

  /**
   * Returns the downloadable URL for the POM file corresponding to the artifact represented by
   * this coordinate.
   *
   * See https://maven.apache.org/guides/introduction/introduction-to-the-pom.html#what-is-a-pom
   * for more context on POM files.
   */
  fun computePomUrl(baseRepoUrl: String): String =
    computeArtifactFileUrl(baseRepoUrl, extension = "pom")

  private fun computeArtifactFileUrl(baseRepoUrl: String, extension: String): String {
    return "${baseRepoUrl.removeSuffix("/")}/${groupId.replace('.', '/')}/$artifactId/$version" +
      "/$artifactId-$version$delimitedClassifierUrlFragment.$extension"
  }

  companion object {
    /**
     * Returns a new [MavenCoordinate] derived from the specified [coordinateString] with
     * fragments defined by https://maven.apache.org/repositories/artifacts.html.
     *
     * Note that this function does not support 'baseVersion'-style versions and will treat such
     * fragments as a whole 'version' piece.
     */
    fun parseFrom(coordinateString: String): MavenCoordinate {
      val components = coordinateString.split(':')
      return when (components.size) {
        3 -> {
          val (groupId, artifactId, version) = components
          MavenCoordinate(groupId, artifactId, version)
        }
        4 -> {
          val (groupId, artifactId, extension, version) = components
          MavenCoordinate(groupId, artifactId, version, extension = extension)
        }
        5 -> {
          val (groupId, artifactId, extension, classifier, version) = components
          MavenCoordinate(groupId, artifactId, version, classifier, extension)
        }
        else -> error("Invalid Maven coordinate string: $coordinateString.")
      }
    }

    private fun String.bazelifyCoordFragment(): String = replace('.', '_').replace('-', '_')
  }
}
