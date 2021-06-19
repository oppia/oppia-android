package test

import java.io.File
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.junit.rules.ExpectedException

class MavenScriptTest {

  @Test
  fun checkTestRunning() {
    val calculation = 4
    val expectedResult = 4
    assertEquals(expectedResult, calculation)
  }

}
