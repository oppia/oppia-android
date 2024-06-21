package org.oppia.android.scripts.license

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [MavenCoordinate]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class MavenCoordinateTest {
  @Test
  fun testParseFrom_emptyString_throwsError() {
    val error = assertThrows<IllegalStateException> {
      MavenCoordinate.parseFrom(coordinateString = "")
    }

    assertThat(error).hasMessageThat().contains("Invalid Maven coordinate string")
  }

  @Test
  fun testParseFrom_oneComponentString_throwsError() {
    val error = assertThrows<IllegalStateException> {
      MavenCoordinate.parseFrom("org.oppia.fake-group")
    }

    assertThat(error).hasMessageThat().contains("Invalid Maven coordinate string")
  }

  @Test
  fun testParseFrom_twoComponentString_throwsError() {
    val error = assertThrows<IllegalStateException> {
      MavenCoordinate.parseFrom("org.oppia.fake-group:and.fake-art")
    }

    assertThat(error).hasMessageThat().contains("Invalid Maven coordinate string")
  }

  @Test
  fun testParseFrom_sixComponentString_throwsError() {
    val error = assertThrows<IllegalStateException> {
      MavenCoordinate.parseFrom("one:two:three:four:five:six")
    }

    assertThat(error).hasMessageThat().contains("Invalid Maven coordinate string")
  }

  @Test
  fun testParseFrom_threeComponentString_returnsCoordinateWithoutClassifierAndExtension() {
    val coord = MavenCoordinate.parseFrom("org.oppia.fake-group:and.fake-art:1.2.3")

    assertThat(coord.groupId).isEqualTo("org.oppia.fake-group")
    assertThat(coord.artifactId).isEqualTo("and.fake-art")
    assertThat(coord.version).isEqualTo("1.2.3")
    assertThat(coord.classifier).isNull()
    assertThat(coord.extension).isNull()
  }

  @Test
  fun testParseFrom_fourComponentString_returnsCoordinateWithoutClassifier() {
    val coord = MavenCoordinate.parseFrom("org.oppia.fake-group:and.fake-art:aar:1.2.3")

    assertThat(coord.groupId).isEqualTo("org.oppia.fake-group")
    assertThat(coord.artifactId).isEqualTo("and.fake-art")
    assertThat(coord.version).isEqualTo("1.2.3")
    assertThat(coord.classifier).isNull()
    assertThat(coord.extension).isEqualTo("aar")
  }

  @Test
  fun testParseFrom_fiveComponentString_returnsCoordinateWithAllProperties() {
    val coord = MavenCoordinate.parseFrom("org.oppia.fake-group:and.fake-art:aar:sources:1.2.3")

    assertThat(coord.groupId).isEqualTo("org.oppia.fake-group")
    assertThat(coord.artifactId).isEqualTo("and.fake-art")
    assertThat(coord.version).isEqualTo("1.2.3")
    assertThat(coord.classifier).isEqualTo("sources")
    assertThat(coord.extension).isEqualTo("aar")
  }

  @Test
  fun testReducedCoordinateString_noClassifierOrExtension_returnsReducedString() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group", artifactId = "and.fake-art", version = "1.2.3"
      )

    val reducedCoordString = coord.reducedCoordinateString

    assertThat(reducedCoordString).isEqualTo("org.oppia.fake-group:and.fake-art:1.2.3")
  }

  @Test
  fun testReducedCoordinateString_withClassifier_returnsReducedString() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        classifier = "sources"
      )

    val reducedCoordString = coord.reducedCoordinateString

    assertThat(reducedCoordString).isEqualTo("org.oppia.fake-group:and.fake-art:1.2.3")
  }

  @Test
  fun testReducedCoordinateString_withExtension_returnsReducedString() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar"
      )

    val reducedCoordString = coord.reducedCoordinateString

    assertThat(reducedCoordString).isEqualTo("org.oppia.fake-group:and.fake-art:1.2.3")
  }

  @Test
  fun testReducedCoordinateString_withClassifierAndExtension_returnsReducedString() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar",
        classifier = "sources"
      )

    val reducedCoordString = coord.reducedCoordinateString

    assertThat(reducedCoordString).isEqualTo("org.oppia.fake-group:and.fake-art:1.2.3")
  }

  @Test
  fun testReducedCoordinateString_noClassifierOrExtension_thenParse_returnsEquivalentCoordinate() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group", artifactId = "and.fake-art", version = "1.2.3"
      )

    val parsedReducedCoord = MavenCoordinate.parseFrom(coord.reducedCoordinateString)

    assertThat(parsedReducedCoord).isEqualTo(coord)
  }

  @Test
  fun testReducedCoordinateString_withClassifier_thenParse_returnsEquivalentCoordNoClassifier() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        classifier = "sources"
      )

    val parsedReducedCoord = MavenCoordinate.parseFrom(coord.reducedCoordinateString)

    val expected = coord.copy(classifier = null)
    assertThat(parsedReducedCoord).isEqualTo(expected)
  }

  @Test
  fun testReducedCoordinateString_withExtension_thenParse_returnsEquivalentCoordinateNoExt() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar"
      )

    val parsedReducedCoord = MavenCoordinate.parseFrom(coord.reducedCoordinateString)

    val expected = coord.copy(extension = null)
    assertThat(parsedReducedCoord).isEqualTo(expected)
  }

  @Test
  fun testReducedCoordinateString_withClassifierAndExt_thenParse_retsEquivCoordNoClassifierOrExt() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar",
        classifier = "sources"
      )

    val parsedReducedCoord = MavenCoordinate.parseFrom(coord.reducedCoordinateString)

    val expected = coord.copy(extension = null, classifier = null)
    assertThat(parsedReducedCoord).isEqualTo(expected)
  }

  @Test
  fun testReducedCoordinateStringWithoutVersion_noClassifierOrExtension_returnsReducedString() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group", artifactId = "and.fake-art", version = "1.2.3"
      )

    val reducedCoordString = coord.reducedCoordinateStringWithoutVersion

    assertThat(reducedCoordString).isEqualTo("org.oppia.fake-group:and.fake-art")
  }

  @Test
  fun testReducedCoordinateStringWithoutVersion_withClassifier_returnsReducedString() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        classifier = "sources"
      )

    val reducedCoordString = coord.reducedCoordinateStringWithoutVersion

    assertThat(reducedCoordString).isEqualTo("org.oppia.fake-group:and.fake-art")
  }

  @Test
  fun testReducedCoordinateStringWithoutVersion_withExtension_returnsReducedString() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar"
      )

    val reducedCoordString = coord.reducedCoordinateStringWithoutVersion

    assertThat(reducedCoordString).isEqualTo("org.oppia.fake-group:and.fake-art")
  }

  @Test
  fun testReducedCoordinateStringWithoutVersion_withClassifierAndExtension_returnsReducedString() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar",
        classifier = "sources"
      )

    val reducedCoordString = coord.reducedCoordinateStringWithoutVersion

    assertThat(reducedCoordString).isEqualTo("org.oppia.fake-group:and.fake-art")
  }

  @Test
  fun testBazelTarget_noClassifierOrExtension_returnsStringWithDotsAndDashesReplaced() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group", artifactId = "and.fake-art", version = "1.2.3"
      )

    val bazelTarget = coord.bazelTarget

    assertThat(bazelTarget).isEqualTo("org_oppia_fake_group_and_fake_art")
  }

  @Test
  fun testBazelTarget_withClassifier_returnsStringWithDotsAndDashesReplaced() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        classifier = "sources"
      )

    val bazelTarget = coord.bazelTarget

    assertThat(bazelTarget).isEqualTo("org_oppia_fake_group_and_fake_art")
  }

  @Test
  fun testBazelTarget_withExtension_returnsStringWithDotsAndDashesReplaced() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar"
      )

    val bazelTarget = coord.bazelTarget

    assertThat(bazelTarget).isEqualTo("org_oppia_fake_group_and_fake_art")
  }

  @Test
  fun testBazelTarget_withClassifierAndExtension_returnsStringWithDotsAndDashesReplaced() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar",
        classifier = "sources"
      )

    val bazelTarget = coord.bazelTarget

    assertThat(bazelTarget).isEqualTo("org_oppia_fake_group_and_fake_art")
  }

  @Test
  fun testComputeArtifactUrl_noClassifierOrExtension_returnsArtifactUrlWithJar() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group", artifactId = "and.fake-art", version = "1.2.3"
      )

    val artifactUrl = coord.computeArtifactUrl(baseRepoUrl = "https://example.com")

    // The artifact's extension defaults to '.jar'.
    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3.jar"
      )
  }

  @Test
  fun testComputeArtifactUrl_noClassifierOrExtension_urlWithSuffix_returnsArtifactUrlWithJar() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group", artifactId = "and.fake-art", version = "1.2.3"
      )

    val artifactUrl = coord.computeArtifactUrl(baseRepoUrl = "https://example.com/")

    // The extra '/' from the base URL should be ignored.
    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3.jar"
      )
  }

  @Test
  fun testComputeArtifactUrl_withClassifier_returnsArtifactUrlWithJar() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        classifier = "sources"
      )

    val artifactUrl = coord.computeArtifactUrl(baseRepoUrl = "https://example.com")

    // The artifact's extension defaults to '.jar'.
    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3-sources.jar"
      )
  }

  @Test
  fun testComputeArtifactUrl_withExtension_returnsArtifactUrlWithExtension() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar"
      )

    val artifactUrl = coord.computeArtifactUrl(baseRepoUrl = "https://example.com")

    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3.aar"
      )
  }

  @Test
  fun testComputeArtifactUrl_withClassifierAndExtension_returnsArtifactUrlWithExtension() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar",
        classifier = "sources"
      )

    val artifactUrl = coord.computeArtifactUrl(baseRepoUrl = "https://example.com")

    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3-sources.aar"
      )
  }

  @Test
  fun testComputePomUrl_noClassifierOrExtension_returnsPomUrl() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group", artifactId = "and.fake-art", version = "1.2.3"
      )

    val artifactUrl = coord.computePomUrl(baseRepoUrl = "https://example.com")

    // Pom URLs always end in '.pom'.
    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3.pom"
      )
  }

  @Test
  fun testComputePomUrl_noClassifierOrExtension_urlWithSuffix_returnsPomUrl() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group", artifactId = "and.fake-art", version = "1.2.3"
      )

    val artifactUrl = coord.computePomUrl(baseRepoUrl = "https://example.com/")

    // The extra '/' from the base URL should be ignored.
    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3.pom"
      )
  }

  @Test
  fun testComputePomUrl_withClassifier_returnsPomUrl() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        classifier = "sources"
      )

    val artifactUrl = coord.computePomUrl(baseRepoUrl = "https://example.com")

    // Pom URLs always end in '.pom'.
    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3-sources.pom"
      )
  }

  @Test
  fun testComputePomUrl_withExtension_returnsPomUrl() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar"
      )

    val artifactUrl = coord.computePomUrl(baseRepoUrl = "https://example.com")

    // Pom URLs always end in '.pom'.
    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3.pom"
      )
  }

  @Test
  fun testComputePomUrl_withClassifierAndExtension_returnsPomUrl() {
    val coord =
      MavenCoordinate(
        groupId = "org.oppia.fake-group",
        artifactId = "and.fake-art",
        version = "1.2.3",
        extension = "aar",
        classifier = "sources"
      )

    val artifactUrl = coord.computePomUrl(baseRepoUrl = "https://example.com")

    // Pom URLs always end in '.pom'.
    assertThat(artifactUrl)
      .isEqualTo(
        "https://example.com/org/oppia/fake-group/and.fake-art/1.2.3/and.fake-art-1.2.3-sources.pom"
      )
  }
}
