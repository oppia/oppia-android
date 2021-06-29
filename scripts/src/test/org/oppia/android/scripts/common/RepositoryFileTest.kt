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
  fun setUpTests() {
    tempFolder.newFolder("testfiles")
  }

  @Test
  fun testRepoFile_fileInAppDirectory_fileShouldBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", "app")
    val file = tempFolder.newFile("testfiles/app/TestFile")

    val collectedFiles = collectFiles()

    assertThat(collectedFiles.contains(file)).isEqualTo(true)
  }

  @Test
  fun testRepoFile_fileInDotGitDirectory_fileShouldNotBePresentInCollectedFiles() {
    tempFolder.newFolder("testfiles", ".git")
    val file = tempFolder.newFile("testfiles/.git/TestFile")

    val collectedFiles = collectFiles()

    assertThat(collectedFiles.contains(file)).isEqualTo(false)
  }

  @Test
  fun testRepoFile_retrieveFilePath_filePathShouldBeRelativeToRoot() {
    tempFolder.newFolder("testfiles", "model", "src", "main", "proto")
    val file = tempFolder.newFile("testfiles/model/src/main/proto/test.proto")
    val obtainedPath = retrieveRelativeFilePath(file)

    assertThat(obtainedPath).isEqualTo("model/src/main/proto/test.proto")
  }

  /** Helper function which executes the file collection method of the RepoFile class. */
  private fun collectFiles(): List<File> {
    return RepositoryFile.collectSearchFiles(tempFolder.getRoot().toString() + "/testfiles/")
  }

  /** Helper function which executes retrieve file path method of the Repo class. */
  private fun retrieveRelativeFilePath(file: File): String {
    return RepositoryFile.retrieveRelativeFilePath(
      file,
      tempFolder.getRoot().toString() + "/testfiles/"
    )
  }
}
