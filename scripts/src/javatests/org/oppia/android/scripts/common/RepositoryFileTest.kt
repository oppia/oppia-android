package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** Tests for [RepositoryFile]. */
class RepositoryFileTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
  }

  @Test
  fun testRepoFile_fileInAppDirectory_fileShouldBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "app")
    val file = tempFolder.newFile("testfiles/app/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).contains(file)
  }

  @Test
  fun testRepoFile_fileInDotGitDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", ".git")
    val file = tempFolder.newFile("testfiles/.git/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInDotGitSecretDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", ".gitsecret")
    val file = tempFolder.newFile("testfiles/.gitsecret/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInDotIdeaDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", ".idea")
    val file = tempFolder.newFile("testfiles/.idea/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInDotAswbDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", ".aswb")
    val file = tempFolder.newFile("testfiles/.aswb/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInBazelBinDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "bazel-bin")
    val file = tempFolder.newFile("testfiles/bazel-bin/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInBazelOppiaAndroidDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "bazel-oppia-android")
    val file = tempFolder.newFile("testfiles/bazel-oppia-android/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInBazelOutDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "bazel-out")
    val file = tempFolder.newFile("testfiles/bazel-out/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInBazelTestLogsDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "bazel-testlogs")
    val file = tempFolder.newFile("testfiles/bazel-testlogs/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInAppBuildDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "app", "build")
    val file = tempFolder.newFile("testfiles/app/build/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInDataBuildDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "data", "build")
    val file = tempFolder.newFile("testfiles/data/build/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInDomainBuildDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "domain", "build", "generated")
    val file = tempFolder.newFile("testfiles/domain/build/generated/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInModelBuildDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "model", "build", "libs")
    val file = tempFolder.newFile("testfiles/model/build/libs/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInTestingBuildDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "testing", "build")
    val file = tempFolder.newFile("testfiles/testing/build/TestFile.kt")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInUtilityBuildDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "utility", "build")
    val file = tempFolder.newFile("testfiles/utility/build/TestFile.kt")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).doesNotContain(file)
  }

  @Test
  fun testRepoFile_fileInDotGitHubDirectory_fileShouldBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", ".github")
    val file = tempFolder.newFile("testfiles/.github/TestFile")

    val collectedFiles = RepositoryFile.collectSearchFiles("${tempFolder.root}/testfiles/")

    assertThat(collectedFiles).contains(file)
  }

  @Test
  fun testRepoFile_dotKtExpectedExtension_onlyKtFilesShouldBePresentInCollectedFiles() {
    val xmlFile = tempFolder.newFile("testfiles/TestFile.xml")
    val kotlinFile = tempFolder.newFile("testfiles/TestFile.kt")

    val collectedFiles = RepositoryFile.collectSearchFiles(
      repoPath = "${tempFolder.root}/testfiles/",
      expectedExtension = ".kt"
    )

    assertThat(collectedFiles).contains(kotlinFile)
    assertThat(collectedFiles).doesNotContain(xmlFile)
  }

  @Test
  fun testRepoFile_specifyExemptionList_filesInExemptionListShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "app")
    val testFile1 = tempFolder.newFile("testfiles/TestFile1.kt")
    val testFile2 = tempFolder.newFile("testfiles/app/TestFile2.kt")
    val testFile3 = tempFolder.newFile("testfiles/app/TestFile3.kt")
    val testFile4 = tempFolder.newFile("testfiles/TestFile4.kt")
    val exemptionList = listOf<String>(
      "app/TestFile3.kt",
      "TestFile4.kt"
    )

    val collectedFiles = RepositoryFile.collectSearchFiles(
      repoPath = "${tempFolder.root}/testfiles/",
      exemptionsList = exemptionList
    )

    assertThat(collectedFiles).contains(testFile1)
    assertThat(collectedFiles).contains(testFile2)
    assertThat(collectedFiles).doesNotContain(testFile3)
    assertThat(collectedFiles).doesNotContain(testFile4)
  }

  @Test
  fun testRepoFile_specifyMultipleParams_collectedFilesComplyWithAllSpecs() {
    tempFolder.newFolder("testfiles", "app")
    tempFolder.newFolder("testfiles", ".git")
    val testFile1 = tempFolder.newFile("testfiles/TestFile1.kt")
    val testFile2 = tempFolder.newFile("testfiles/app/TestFile2.kt")
    val testFile3 = tempFolder.newFile("testfiles/app/TestFile3.kt")
    val testFile4 = tempFolder.newFile("testfiles/TestFile4.kt")
    val testFile5 = tempFolder.newFile("testfiles/TestFile5.xml")
    val testFile6 = tempFolder.newFile("testfiles/.git/TestFile6.kt")
    val exemptionList = listOf<String>(
      "app/TestFile3.kt",
      "TestFile4.kt"
    )

    val collectedFiles = RepositoryFile.collectSearchFiles(
      repoPath = "${tempFolder.root}/testfiles/",
      expectedExtension = ".kt",
      exemptionsList = exemptionList
    )

    assertThat(collectedFiles).contains(testFile1)
    assertThat(collectedFiles).contains(testFile2)
    assertThat(collectedFiles).doesNotContain(testFile3)
    assertThat(collectedFiles).doesNotContain(testFile4)
    assertThat(collectedFiles).doesNotContain(testFile5)
    assertThat(collectedFiles).doesNotContain(testFile6)
  }

  @Test
  fun testRepoFile_retrieveFilePath_filePathShouldBeRelativeToRoot() {
    tempFolder.newFolder("testfiles", "model", "src", "main", "proto")
    val file = tempFolder.newFile("testfiles/model/src/main/proto/test.proto")
    val obtainedPath = retrieveRelativeFilePath(file)

    assertThat(obtainedPath).isEqualTo("model/src/main/proto/test.proto")
  }

  @Test
  fun testRepoFile_fileNotHavingCommonDirectoryWithRoot_pathObtainedIsNotRelativeToRoot() {
    val file = tempFolder.newFile("TestFile.kt")
    val obtainedPath = retrieveRelativeFilePath(file)

    assertThat(obtainedPath).contains("TestFile.kt")
    assertThat(obtainedPath).isNotEqualTo("TestFile.kt")
  }

  /**
   * Calls [RepositoryFile.retrieveRelativeFilePath] for a file and returns the relative
   * path to the test directory.
   */
  private fun retrieveRelativeFilePath(file: File): String {
    return RepositoryFile.retrieveRelativeFilePath(
      file,
      "${tempFolder.root}/testfiles/"
    )
  }
}
