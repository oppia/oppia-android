package org.oppia.android.scripts.maven

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [GenerateMavenDependenciesList]. */
class GenerateMavenDependenciesListTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }
  // 1. Some License requires manual work, script fails.
  // 2. Some Dependency requires manual work, script fails.
  // 3. Some License and dependency both requires manual work, script fails.
  // 4. Textproto is complete, script passes.
  // 5. Some dependency contains empty license list, script fails.
  // 6. Incomplte manual work, script fails.
  // 7. Dependencies contain invalid links, script fails.

  @Test
  fun dummy_test() {
    val num = 4
    assertThat(num).isEqualTo(4)
  }

  private class DependencyListsProviderInterceptor(

  ): DependencyListsProvider {

  }
}
