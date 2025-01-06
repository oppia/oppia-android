package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File
import java.lang.AssertionError
import java.lang.IllegalStateException

/**
 * Tests for [TestBazelWorkspace].
 *
 * Note also that this suite isn't really sufficient for ensuring that the generated Bazel files are
 * actually correct or well formatted. The utility depends on tests that utilize Bazel on the
 * filesystem to ensure correctness. This is because otherwise there's a circular logic dependency:
 * Bazel code is needed to verify the test Bazel utility, but the test Bazel utility is needed to
 * arrange the environment for ensuring that the Bazel code is correct. Rather than utilizing even
 * simple queries to ensure correctness, we leverage both specific expectations tested below & other
 * tests to ensure the utility is operating correctly.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class TestBazelWorkspaceTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  @Test
  fun testCreateTestUtility_doesNotImmediatelyCreateAnyFiles() {
    TestBazelWorkspace(tempFolder)

    // Simply creating the utility should not create any files. This ensures later tests are
    // beginning in a sane state.
    assertThat(tempFolder.root.listFiles()).isEmpty()
  }

  @Test
  fun testInitEmptyWorkspace_emptyDirectory_createsEmptyWorkspaceFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.initEmptyWorkspace()

    // The WORKSPACE file should now exist, but it won't have any content yet.
    val workspaceFile = File(tempFolder.root, "WORKSPACE")
    assertThat(workspaceFile.exists()).isTrue()
    assertThat(workspaceFile.readLines()).isEmpty()
  }

  @Test
  fun testInitEmptyWorkspace_emptyDirectory_createsBazelVersionFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.initEmptyWorkspace()

    // A .bazelversion file should now exist with the correct version.
    val bazelVersionFile = File(tempFolder.root, ".bazelversion")
    assertThat(bazelVersionFile.exists()).isTrue()
    assertThat(bazelVersionFile.readText().trim()).isEqualTo("6.5.0")
  }

  @Test
  fun testInitEmptyWorkspace_emptyDirectory_createsBazelRcFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.initEmptyWorkspace()

    // A .bazelversion file should now exist with the correct flags.
    val bazelRcFile = File(tempFolder.root, ".bazelrc")
    assertThat(bazelRcFile.exists()).isTrue()
    assertThat(bazelRcFile.readText().trim()).isEqualTo(
      """
        --noenable_bzlmod
        build --java_runtime_version=remotejdk_11 --tool_java_runtime_version=remotejdk_11
      """.trimIndent()
    )
  }

  @Test
  fun testInitEmptyWorkspace_fileCreationFails_throwsAssertionError() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    // Delete the WORKSPACE after initializing it--this is what puts the workspace in a bad place.
    // Theoretically, the new folder creation could also fail & the underlying assertion check would
    // catch this case and fail.
    testBazelWorkspace.initEmptyWorkspace()
    File(tempFolder.root, "WORKSPACE").delete()

    // Verify that when initializing an empty workspace fails, an AssertionError is thrown (which
    // would fail for calling tests).
    assertThrows<AssertionError>() { testBazelWorkspace.initEmptyWorkspace() }
  }

  @Test
  fun testWorkspaceFileProperty_retrieve_createsWorkspaceFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val workspaceFile = testBazelWorkspace.workspaceFile

    // Verify that WORKSPACE file is a top-level file that exists within the root, but is empty.
    assertThat(workspaceFile.exists()).isTrue()
    assertThat(workspaceFile.name).isEqualTo("WORKSPACE")
    assertThat(workspaceFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(workspaceFile.toRelativeString(tempFolder.root)).isEqualTo("WORKSPACE")
    assertThat(workspaceFile.readLines()).isEmpty()
  }

  @Test
  fun testSetUpWorkspaceForRulesJvmExternal_withOneDep_containsCorrectList() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("com.android.support:support-annotations:28.0.0")
    )

    val workspaceFile = testBazelWorkspace.workspaceFile
    val workspaceContent = workspaceFile.readText()
    assertThat(workspaceContent).contains("com.android.support:support-annotations:28.0.0")
  }

  @Test
  fun testSetUpWorkspaceForRulesJvmExternal_withOneDep_setsUpBazelVersion() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("com.android.support:support-annotations:28.0.0")
    )

    val bazelVersionContent = tempFolder.getBazelVersionFile().readText().trim()
    assertThat(bazelVersionContent).isEqualTo("6.5.0")
  }

  @Test
  fun testSetUpWorkspaceForRulesJvmExternal_withOneDep_setsUpBazelRc() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("com.android.support:support-annotations:28.0.0")
    )

    val bazelRcContent = tempFolder.getBazelRcFile().readText().trim()
    assertThat(bazelRcContent).isEqualTo(
      """
        --noenable_bzlmod
        build --java_runtime_version=remotejdk_11 --tool_java_runtime_version=remotejdk_11
      """.trimIndent()
    )
  }

  @Test
  fun testSetUpWorkspaceForRulesJvmExternal_withTwoDeps_containsCorrectList() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf(
        "com.android.support:support-annotations:28.0.0",
        "io.fabric.sdk.android:fabric:1.4.7"
      )
    )

    val workspaceFile = testBazelWorkspace.workspaceFile
    val workspaceContent = workspaceFile.readText()

    assertThat(workspaceContent).contains("com.android.support:support-annotations:28.0.0")
    assertThat(workspaceContent).contains("io.fabric.sdk.android:fabric:1.4.7")
  }

  @Test
  fun testSetUpWorkspaceForRulesJvmExternal_withMultipleDeps_containsCorrectList() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf(
        "com.android.support:support-annotations:28.0.0",
        "io.fabric.sdk.android:fabric:1.4.7",
        "androidx.databinding:databinding-adapters:3.4.2",
        "com.google.protobuf:protobuf-lite:3.0.0"
      )
    )

    val workspaceFile = testBazelWorkspace.workspaceFile
    val workspaceContent = workspaceFile.readText()

    assertThat(workspaceContent).contains("com.android.support:support-annotations:28.0.0")
    assertThat(workspaceContent).contains("io.fabric.sdk.android:fabric:1.4.7")
    assertThat(workspaceContent).contains("androidx.databinding:databinding-adapters:3.4.2")
    assertThat(workspaceContent).contains("com.google.protobuf:protobuf-lite:3.0.0")
  }

  @Test
  fun testSetUpWorkspaceForRulesJvmExternal_multipleCalls_containsOnlyFirstTimeContent() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("com.android.support:support-annotations:28.0.0")
    )

    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("io.fabric.sdk.android:fabric:1.4.7")
    )

    val workspaceFile = testBazelWorkspace.workspaceFile
    val workspaceContent = workspaceFile.readText()

    assertThat(workspaceContent).contains("com.android.support:support-annotations:28.0.0")
    assertThat(workspaceContent).doesNotContain("io.fabric.sdk.android:fabric:1.4.7")
  }

  @Test
  fun testSetUpWorkspaceForRulesJvmExternal_addsMavenInstall() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("com.android.support:support-annotations:28.0.0")
    )

    val workspaceFile = testBazelWorkspace.workspaceFile
    val workspaceContent = workspaceFile.readText()

    assertThat(workspaceContent).contains(
      """
      maven_install(
          artifacts = artifactsList,
          repositories = [
              "https://maven.google.com",
              "https://repo1.maven.org/maven2",
          ],
      )
      """.trimIndent()
    )
  }

  @Test
  fun testRootBuildFileProperty_retrieve_createsBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val buildFile = testBazelWorkspace.rootBuildFile

    // Verify that the BUILD file is a top-level file that exists within the root, but is empty.
    assertThat(buildFile.exists()).isTrue()
    assertThat(buildFile.name).isEqualTo("BUILD.bazel")
    assertThat(buildFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(buildFile.toRelativeString(tempFolder.root)).isEqualTo("BUILD.bazel")
    assertThat(buildFile.readLines()).isEmpty()
  }

  @Test
  fun testRootBuildFileProperty_retrieve_afterCreateTest_isChanged() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val originalLength = testBazelWorkspace.rootBuildFile.length()
    testBazelWorkspace.createTest(testName = "ExampleTest")

    val buildFile = testBazelWorkspace.rootBuildFile

    assertThat(buildFile.length()).isNotEqualTo(originalLength)
  }

  @Test
  fun testRootBuildFileProperty_retrieve_afterCreateLibrary_isChanged() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val originalLength = testBazelWorkspace.rootBuildFile.length()
    testBazelWorkspace.createLibrary(dependencyName = "ExampleLib")

    val buildFile = testBazelWorkspace.rootBuildFile

    assertThat(buildFile.length()).isNotEqualTo(originalLength)
  }

  @Test
  fun testAddSourceAndTestFileWithContent_createsSourceAndTestFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val sourceContent =
      """
      fun main() {
              println("Hello, World!")
          }
      """

    val testContent =
      """
      import org.junit.Test
      import kotlin.test.assertEquals
      
      class MainTest {
          
          @Test
          fun testMain() {
              assertEquals(1, 1)
          }
      }
      """

    testBazelWorkspace.addSourceAndTestFileWithContent(
      "Main",
      "MainTest",
      sourceContent,
      testContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val sourceFile = File(tempFolder.root, "coverage/main/java/com/example/Main.kt")
    val testFile = File(tempFolder.root, "coverage/test/java/com/example/MainTest.kt")

    assertThat(sourceFile.exists()).isTrue()
    assertThat(sourceFile.readText()).isEqualTo(sourceContent)

    assertThat(testFile.exists()).isTrue()
    assertThat(testFile.readText()).isEqualTo(testContent)
  }

  @Test
  fun testAddSourceAndTestFileWithContent_updatesBuildFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val sourceContent = "fun main() { println(\"Hello, World!\") }"
    val testContent =
      """
        import org.junit.Test
        import kotlin.test.assertEquals

        class MainTest {
            @Test
            fun testMain() {
                assertEquals(1, 1)
            }
        }
      """.trimIndent()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      "Main",
      "MainTest",
      sourceContent,
      testContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val sourceBuildFile = File(tempFolder.root, "coverage/main/java/com/example/BUILD.bazel")
    val testBuildFile = File(tempFolder.root, "coverage/test/java/com/example/BUILD.bazel")

    assertThat(sourceBuildFile.exists()).isTrue()
    assertThat(sourceBuildFile.readText()).contains(
      """
        kt_jvm_library(
            name = "main",
            srcs = ["Main.kt"],
            visibility = ["//visibility:public"]
        )
      """.trimIndent()
    )

    assertThat(testBuildFile.exists()).isTrue()
    assertThat(testBuildFile.readText()).contains(
      """
        load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")
        kt_jvm_test(
            name = "MainTest",
            srcs = ["MainTest.kt"],
            deps = [
                "//coverage/main/java/com/example:main",
                "@maven//:junit_junit",
            ],
            visibility = ["//visibility:public"],
            test_class = "com.example.MainTest",
        )
      """.trimIndent()
    )
  }

  @Test
  fun testAddMultiLevelSourceAndTestFileWithContent_createsSourceAndTestFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val sourceContent =
      """
      fun main() {
              println("Hello, World!")
          }
      """

    val testContentShared =
      """
      import org.junit.Test
      import kotlin.test.assertEquals
      
      class MainTest {
          
          @Test
          fun testMain() {
              assertEquals(1, 1)
          }
      }
      """

    val testContentLocal =
      """
      import org.junit.Test
      import kotlin.test.assertEquals
      
      class MainTestLocal {
          
          @Test
          fun testMain() {
              assertEquals(1, 2)
          }
      }
      """

    testBazelWorkspace.addMultiLevelSourceAndTestFileWithContent(
      filename = "Main",
      sourceContent = sourceContent,
      testContentShared = testContentShared,
      testContentLocal = testContentLocal,
      subpackage = "coverage"
    )

    val sourceFile = File(tempFolder.root, "coverage/main/java/com/example/Main.kt")
    val testFileShared = File(tempFolder.root, "coverage/sharedTest/java/com/example/MainTest.kt")
    val testFileLocal = File(tempFolder.root, "coverage/test/java/com/example/MainLocalTest.kt")

    assertThat(sourceFile.exists()).isTrue()
    assertThat(sourceFile.readText()).isEqualTo(sourceContent)

    assertThat(testFileShared.exists()).isTrue()
    assertThat(testFileShared.readText()).isEqualTo(testContentShared)

    assertThat(testFileLocal.exists()).isTrue()
    assertThat(testFileLocal.readText()).isEqualTo(testContentLocal)
  }

  @Test
  fun testAddMultiLevelSourceAndTestFileWithContent_updatesBuildFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val sourceContent =
      """
      fun main() {
              println("Hello, World!")
          }
      """

    val testContentShared =
      """
      import org.junit.Test
      import kotlin.test.assertEquals
      
      class MainTest {
          
          @Test
          fun testMain() {
              assertEquals(1, 1)
          }
      }
      """

    val testContentLocal =
      """
      import org.junit.Test
      import kotlin.test.assertEquals
      
      class MainTestLocal {
          
          @Test
          fun testMain() {
              assertEquals(1, 2)
          }
      }
      """

    testBazelWorkspace.addMultiLevelSourceAndTestFileWithContent(
      filename = "Main",
      sourceContent = sourceContent,
      testContentShared = testContentShared,
      testContentLocal = testContentLocal,
      subpackage = "coverage"
    )

    val sourceBuildFile = File(
      tempFolder.root, "coverage/main/java/com/example/BUILD.bazel"
    )
    val testBuildFileShared = File(
      tempFolder.root, "coverage/sharedTest/java/com/example/BUILD.bazel"
    )
    val testBuildFileLocal = File(
      tempFolder.root, "coverage/test/java/com/example/BUILD.bazel"
    )

    assertThat(sourceBuildFile.exists()).isTrue()
    assertThat(sourceBuildFile.readText()).contains(
      """
        kt_jvm_library(
            name = "main",
            srcs = ["Main.kt"],
            visibility = ["//visibility:public"]
        )
      """.trimIndent()
    )

    assertThat(testBuildFileShared.exists()).isTrue()
    assertThat(testBuildFileShared.readText()).contains(
      """
        load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")
        kt_jvm_test(
            name = "MainTest",
            srcs = ["MainTest.kt"],
            deps = [
                "//coverage/main/java/com/example:main",
                "@maven//:junit_junit",
            ],
            visibility = ["//visibility:public"],
            test_class = "com.example.MainTest",
        )
      """.trimIndent()
    )

    assertThat(testBuildFileLocal.exists()).isTrue()
    assertThat(testBuildFileLocal.readText()).contains(
      """
        load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")
        kt_jvm_test(
            name = "MainLocalTest",
            srcs = ["MainLocalTest.kt"],
            deps = [
                "//coverage/main/java/com/example:main",
                "@maven//:junit_junit",
            ],
            visibility = ["//visibility:public"],
            test_class = "com.example.MainLocalTest",
        )
      """.trimIndent()
    )
  }

  @Test
  fun testAddSourceContentAndBuildFile_createsSourceFileAndBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val sourceContent = "fun main() { println(\"Hello, World!\") }"

    testBazelWorkspace.addSourceContentAndBuildFile(
      "Main",
      sourceContent,
      "coverage/main/java/com/example"
    )

    val sourceFile = File(tempFolder.root, "coverage/main/java/com/example/Main.kt")
    val buildFile = File(tempFolder.root, "coverage/main/java/com/example/BUILD.bazel")

    assertThat(sourceFile.exists()).isTrue()
    assertThat(sourceFile.readText()).isEqualTo(sourceContent.trimIndent())

    assertThat(buildFile.exists()).isTrue()
    assertThat(buildFile.readText()).contains(
      """
        kt_jvm_library(
            name = "main",
            srcs = ["Main.kt"],
            visibility = ["//visibility:public"]
        )
      """.trimIndent()
    )
  }

  @Test
  fun testAddTestContentAndBuildFile_createsTestFileAndBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val testContent = "import org.junit.Test" +
      "\nimport kotlin.test.assertEquals\n\nclass MainTest {" +
      "\n@Test\nfun testMain() {\nassertEquals(1, 1)\n}\n}"

    testBazelWorkspace.addTestContentAndBuildFile(
      "Main",
      "MainTest",
      testContent,
      "coverage/main/java/com/example",
      "coverage/test/java/com/example"
    )

    val testFile = File(tempFolder.root, "coverage/test/java/com/example/MainTest.kt")
    val buildFile = File(tempFolder.root, "coverage/test/java/com/example/BUILD.bazel")

    assertThat(testFile.exists()).isTrue()
    assertThat(testFile.readText()).isEqualTo(testContent.trimIndent())

    assertThat(buildFile.exists()).isTrue()
    assertThat(buildFile.readText()).contains(
      """
        load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")
        kt_jvm_test(
            name = "MainTest",
            srcs = ["MainTest.kt"],
            deps = [
                "//coverage/main/java/com/example:main",
                "@maven//:junit_junit",
            ],
            visibility = ["//visibility:public"],
            test_class = "com.example.MainTest",
        )
      """.trimIndent()
    )
  }

  @Test
  fun testAddTestToBuildFile_reusedTestName_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "FirstTest")

    val exception = assertThrows<IllegalStateException>() {
      testBazelWorkspace.addTestToBuildFile(
        testName = "FirstTest",
        testFile = tempFolder.newFile("FirstTestOther.kt")
      )
    }

    assertThat(exception).hasMessageThat().contains("Test 'FirstTest' already set up")
  }

  @Test
  fun testAddTestToBuildFile_firstTest_setsUpWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    val workspaceContent = tempFolder.getWorkspaceFile().readText()
    assertThat(workspaceContent).contains("kt_register_toolchains()")
  }

  @Test
  fun testAddTestToBuildFile_firstTest_setsUpBazelVersion() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    val bazelVersionContent = tempFolder.getBazelVersionFile().readText().trim()
    assertThat(bazelVersionContent).isEqualTo("6.5.0")
  }

  @Test
  fun testAddTestToBuildFile_firstTest_setsUpBazelRc() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    val bazelRcContent = tempFolder.getBazelRcFile().readText().trim()
    assertThat(bazelRcContent).isEqualTo(
      """
        --noenable_bzlmod
        build --java_runtime_version=remotejdk_11 --tool_java_runtime_version=remotejdk_11
      """.trimIndent()
    )
  }

  @Test
  fun testAddTestToBuildFile_firstTest_returnsTestBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    assertThat(files.getFileNames()).containsExactly("WORKSPACE", "BUILD.bazel", "FirstTest.kt")
  }

  @Test
  fun testAddTestToBuildFile_secondTest_doesNotChangeWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )
    val workspaceSize = tempFolder.getWorkspaceFile().length()

    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "SecondTest",
      testFile = tempFolder.newFile("SecondTest.kt")
    )

    // WORKSPACE not included since it doesn't need to be reinitialized.
    assertThat(files.getFileNames()).containsExactly("BUILD.bazel", "SecondTest.kt")
    assertThat(workspaceSize).isEqualTo(tempFolder.getWorkspaceFile().length())
  }

  @Test
  fun testAddTestToBuildFile_firstTest_initializesBuildFileOnlyForTests() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_test")).isEqualTo(1)
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(0)
  }

  @Test
  fun testAddTestToBuildFile_secondTest_doesNotReinitializeBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    testBazelWorkspace.addTestToBuildFile(
      testName = "SecondTest",
      testFile = tempFolder.newFile("SecondTest.kt")
    )

    // The load line should only exist once in the file.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_test")).isEqualTo(1)
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_appendsBasicTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    // There should be 1 test in the file with empty deps and correct source.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = []")
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withGeneratedDep_configuresBuildFileForLibraries() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withGeneratedDependency = true
    )

    // The build file should now be initialized for libraries.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(1)
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withGeneratedDep_appendsLibraryAndTestWithDep() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withGeneratedDependency = true
    )

    // Ensure the test is arranged correctly.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = [\"//:FirstTestDependency_lib\",]")
    // And the generated library.
    assertThat(buildContent.countMatches("kt_jvm_library\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTestDependency.kt\"]")
  }

  @Test
  fun testAddTestToBuildFile_firstTest_withGeneratedDep_returnsTestDepBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withGeneratedDependency = true
    )

    assertThat(files.getFileNames())
      .containsExactly("WORKSPACE", "BUILD.bazel", "FirstTest.kt", "FirstTestDependency.kt")
  }

  @Test
  fun testAddTestToBuildFile_secondTest_withGeneratedDep_returnsTestDepBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "SecondTest",
      testFile = tempFolder.newFile("SecondTest.kt"),
      withGeneratedDependency = true
    )

    assertThat(files.getFileNames())
      .containsExactly("BUILD.bazel", "SecondTest.kt", "SecondTestDependency.kt")
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withExtraDep_appendsTestWithDep() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withExtraDependency = "//:ExtraDep"
    )

    // Ensure the test is arranged correctly.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = [\"//:ExtraDep\",]")
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withSubpackage_appendsToSubpackageBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    tempFolder.newFolder("subpackage")
    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("subpackage/FirstTest.kt"),
      subpackage = "subpackage"
    )

    // The root build file shouldn't be changed, but there should be new files in the subpackage
    // directory.
    val subpackageDirectory = File(tempFolder.root, "subpackage")
    assertThat(testBazelWorkspace.rootBuildFile.readLines()).isEmpty()
    assertThat(subpackageDirectory.exists()).isTrue()
    assertThat(subpackageDirectory.isDirectory).isTrue()
    assertThat(File(subpackageDirectory, "BUILD.bazel").exists()).isTrue()
    assertThat(File(subpackageDirectory, "FirstTest.kt").exists()).isTrue()
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withSubpackage_returnsNewBuildAndTestFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    tempFolder.newFolder("subpackage")
    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("subpackage/FirstTest.kt"),
      subpackage = "subpackage"
    )

    assertThat(files.getRelativeFileNames(tempFolder.root))
      .containsExactly("WORKSPACE", "subpackage/BUILD.bazel", "subpackage/FirstTest.kt")
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withMultipleSubpackages_returnsNewBuildAndTestFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val subpackage = "subpackage.first.second"
    tempFolder.newFolder(*(subpackage.split(".")).toTypedArray())
    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("subpackage/first/second/FirstTest.kt"),
      subpackage = subpackage
    )

    assertThat(files.getRelativeFileNames(tempFolder.root))
      .containsExactly(
        "WORKSPACE", "subpackage/first/second/BUILD.bazel", "subpackage/first/second/FirstTest.kt"
      )
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withGeneratedAndExtraDeps_includesBothInTestDeps() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withGeneratedDependency = true,
      withExtraDependency = "//:ExtraDep"
    )

    // Both dependencies should be included in the test's deps.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("deps = [\"//:FirstTestDependency_lib\",\"//:ExtraDep\",]")
  }

  @Test
  fun testCreateTest_reusedTestName_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "FirstTest")

    val exception = assertThrows<IllegalStateException>() {
      testBazelWorkspace.createTest(testName = "FirstTest")
    }

    assertThat(exception).hasMessageThat().contains("Test 'FirstTest' already exists")
  }

  @Test
  fun testCreateTest_firstTest_setsUpWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(testName = "FirstTest")

    val workspaceContent = tempFolder.getWorkspaceFile().readText()
    assertThat(workspaceContent).contains("kt_register_toolchains()")
  }

  @Test
  fun testCreateTest_firstTest_setsUpBazelVersion() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(testName = "FirstTest")

    val bazelVersionContent = tempFolder.getBazelVersionFile().readText().trim()
    assertThat(bazelVersionContent).isEqualTo("6.5.0")
  }

  @Test
  fun testCreateTest_firstTest_setsUpBazelRc() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(testName = "FirstTest")

    val bazelRcContent = tempFolder.getBazelRcFile().readText().trim()
    assertThat(bazelRcContent).isEqualTo(
      """
        --noenable_bzlmod
        build --java_runtime_version=remotejdk_11 --tool_java_runtime_version=remotejdk_11
      """.trimIndent()
    )
  }

  @Test
  fun testCreateTest_firstTest_returnsTestBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val files = testBazelWorkspace.createTest(testName = "FirstTest")

    assertThat(files.getFileNames()).containsExactly("WORKSPACE", "BUILD.bazel", "FirstTest.kt")
  }

  @Test
  fun testCreateTest_secondTest_doesNotChangeWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "FirstTest")
    val workspaceSize = tempFolder.getWorkspaceFile().length()

    val files = testBazelWorkspace.createTest(testName = "SecondTest")

    // WORKSPACE not included since it doesn't need to be reinitialized.
    assertThat(files.getFileNames()).containsExactly("BUILD.bazel", "SecondTest.kt")
    assertThat(workspaceSize).isEqualTo(tempFolder.getWorkspaceFile().length())
  }

  @Test
  fun testCreateTest_firstTest_initializesBuildFileOnlyForTests() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(testName = "FirstTest")

    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_test")).isEqualTo(1)
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(0)
  }

  @Test
  fun testCreateTest_secondTest_doesNotReinitializeBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "FirstTest")

    testBazelWorkspace.createTest(testName = "SecondTest")

    // The load line should only exist once in the file.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_test")).isEqualTo(1)
  }

  @Test
  fun testCreateTest_unusedTestName_appendsBasicTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(testName = "FirstTest")

    // There should be 1 test in the file with empty deps and correct source.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = []")
  }

  @Test
  fun testCreateTest_unusedTestName_withGeneratedDep_configuresBuildFileForLibraries() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(
      testName = "FirstTest",
      withGeneratedDependency = true
    )

    // The build file should now be initialized for libraries.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(1)
  }

  @Test
  fun testCreateTest_unusedTestName_withGeneratedDep_appendsLibraryAndTestWithDep() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(
      testName = "FirstTest",
      withGeneratedDependency = true
    )

    // Ensure the test is arranged correctly.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = [\"//:FirstTestDependency_lib\",]")
    // And the generated library.
    assertThat(buildContent.countMatches("kt_jvm_library\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTestDependency.kt\"]")
  }

  @Test
  fun testCreateTest_firstTest_withGeneratedDep_returnsTestDepBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val files = testBazelWorkspace.createTest(
      testName = "FirstTest",
      withGeneratedDependency = true
    )

    assertThat(files.getFileNames())
      .containsExactly("WORKSPACE", "BUILD.bazel", "FirstTest.kt", "FirstTestDependency.kt")
  }

  @Test
  fun testCreateTest_secondTest_withGeneratedDep_returnsTestDepBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "FirstTest")

    val files = testBazelWorkspace.createTest(
      testName = "SecondTest",
      withGeneratedDependency = true
    )

    assertThat(files.getFileNames())
      .containsExactly("BUILD.bazel", "SecondTest.kt", "SecondTestDependency.kt")
  }

  @Test
  fun testCreateTest_unusedTestName_withExtraDep_appendsTestWithDep() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(testName = "FirstTest", withExtraDependency = "//:ExtraDep")

    // Ensure the test is arranged correctly.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = [\"//:ExtraDep\",]")
  }

  @Test
  fun testCreateTest_unusedTestName_withSubpackage_appendsToSubpackageBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(testName = "FirstTest", subpackage = "subpackage")

    // The root build file shouldn't be changed, but there should be new files in the subpackage
    // directory.
    val subpackageDirectory = File(tempFolder.root, "subpackage")
    assertThat(testBazelWorkspace.rootBuildFile.readLines()).isEmpty()
    assertThat(subpackageDirectory.exists()).isTrue()
    assertThat(subpackageDirectory.isDirectory).isTrue()
    assertThat(File(subpackageDirectory, "BUILD.bazel").exists()).isTrue()
    assertThat(File(subpackageDirectory, "FirstTest.kt").exists()).isTrue()
  }

  @Test
  fun testCreateTest_unusedTestName_withSubpackage_returnsNewBuildAndTestFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val files = testBazelWorkspace.createTest(testName = "FirstTest", subpackage = "subpackage")

    assertThat(files.getRelativeFileNames(tempFolder.root))
      .containsExactly("WORKSPACE", "subpackage/BUILD.bazel", "subpackage/FirstTest.kt")
  }

  @Test
  fun testCreateTest_unusedTestName_withMultipleSubpackages_returnsNewBuildAndTestFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val files = testBazelWorkspace.createTest(
      testName = "FirstTest",
      subpackage = "subpackage.first.second"
    )

    assertThat(files.getRelativeFileNames(tempFolder.root))
      .containsExactly(
        "WORKSPACE", "subpackage/first/second/BUILD.bazel", "subpackage/first/second/FirstTest.kt"
      )
  }

  @Test
  fun testCreateTest_unusedTestName_withGeneratedAndExtraDeps_includesBothInTestDeps() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createTest(
      testName = "FirstTest",
      withGeneratedDependency = true,
      withExtraDependency = "//:ExtraDep"
    )

    // Both dependencies should be included in the test's deps.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("deps = [\"//:FirstTestDependency_lib\",\"//:ExtraDep\",]")
  }

  @Test
  fun testCreateLibrary_firstLib_unusedName_configuresWorkspaceAndBuild() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createLibrary(dependencyName = "ExampleDep")

    val workspaceContent = tempFolder.getWorkspaceFile().readText()
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(workspaceContent).contains("kt_register_toolchains()")
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(1)
  }

  @Test
  fun testCreateLibrary_firstLib_unusedName_setsUpBazelVersion() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createLibrary(dependencyName = "ExampleDep")

    val bazelVersionContent = tempFolder.getBazelVersionFile().readText().trim()
    assertThat(bazelVersionContent).isEqualTo("6.5.0")
  }

  @Test
  fun testCreateLibrary_firstLib_unusedName_setsUpBazelRc() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createLibrary(dependencyName = "ExampleDep")

    val bazelRcContent = tempFolder.getBazelRcFile().readText().trim()
    assertThat(bazelRcContent).isEqualTo(
      """
        --noenable_bzlmod
        build --java_runtime_version=remotejdk_11 --tool_java_runtime_version=remotejdk_11
      """.trimIndent()
    )
  }

  @Test
  fun testCreateLibrary_firstLib_unusedName_appendsJvmLibraryDeclaration() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.createLibrary(dependencyName = "ExampleDep")

    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("kt_jvm_library\\(")).isEqualTo(1)
    assertThat(buildContent).contains("name = \"ExampleDep_lib\"")
    assertThat(buildContent).contains("srcs = [\"ExampleDep.kt\"]")
  }

  @Test
  fun testCreateLibrary_firstLib_unusedName_returnsBuildLibAndWorkspaceFilesWithTargetName() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val (targetName, files) = testBazelWorkspace.createLibrary(dependencyName = "ExampleDep")

    assertThat(targetName).isEqualTo("//:ExampleDep_lib")
    assertThat(files.getFileNames()).containsExactly("WORKSPACE", "BUILD.bazel", "ExampleDep.kt")
  }

  @Test
  fun testCreateLibrary_secondLib_unusedName_doesNotChangeWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")
    val workspaceSize = tempFolder.getWorkspaceFile().length()

    val (_, files) = testBazelWorkspace.createLibrary(dependencyName = "SecondLib")

    assertThat(files.getFileNames()).doesNotContain("WORKSPACE")
    assertThat(workspaceSize).isEqualTo(tempFolder.getWorkspaceFile().length())
  }

  @Test
  fun testCreateLibrary_secondLib_unusedName_appendsJvmLibraryDeclaration() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")

    testBazelWorkspace.createLibrary(dependencyName = "SecondLib")

    // The kt_jvm_library declaration should only exist once, and both libraries should exist.
    val buildContent = testBazelWorkspace.rootBuildFile.readText()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(1)
    assertThat(buildContent.countMatches("kt_jvm_library\\(")).isEqualTo(2)
    assertThat(buildContent).contains("name = \"FirstLib_lib\"")
    assertThat(buildContent).contains("srcs = [\"FirstLib.kt\"]")
    assertThat(buildContent).contains("name = \"SecondLib_lib\"")
    assertThat(buildContent).contains("srcs = [\"SecondLib.kt\"]")
  }

  @Test
  fun testCreateLibrary_secondLib_reusedName_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")

    val exception = assertThrows<IllegalStateException>() {
      testBazelWorkspace.createLibrary(dependencyName = "FirstLib")
    }

    assertThat(exception).hasMessageThat().contains("Library 'FirstLib' already exists")
  }

  @Test
  fun testWorkspaceInitialization_createLibrary_thenTest_workspaceInitedForKotlinForLibrary() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")

    testBazelWorkspace.createTest(testName = "FirstTest")

    // The workspace should only be configured once (due to the library initialization).
    val workspaceContent = tempFolder.getWorkspaceFile().readText()
    assertThat(workspaceContent.countMatches("http_archive\\(")).isEqualTo(1)
  }

  @Test
  fun testWorkspaceInitialization_createTest_thenLibrary_workspaceInitedForKotlinForTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "FirstTest")

    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")

    // The workspace should only be configured once (due to the test initialization).
    val workspaceContent = tempFolder.getWorkspaceFile().readText()
    assertThat(workspaceContent.countMatches("http_archive\\(")).isEqualTo(1)
  }

  @Test
  fun testRetrieveTestFile_noTest_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    // A non-existent test file cannot be retrieved.
    assertThrows<NoSuchElementException>() {
      testBazelWorkspace.retrieveTestFile(testName = "Invalid")
    }
  }

  @Test
  fun testRetrieveTestFile_forRealTest_returnsFileForTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "ExampleTest")

    val testFile = testBazelWorkspace.retrieveTestFile(testName = "ExampleTest")

    assertThat(testFile.exists()).isTrue()
    assertThat(testFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(testFile.name).isEqualTo("ExampleTest.kt")
  }

  @Test
  fun testRetrieveLibraryFile_noLibrary_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    // A non-existent library file cannot be retrieved.
    assertThrows<NoSuchElementException>() {
      testBazelWorkspace.retrieveLibraryFile(dependencyName = "Invalid")
    }
  }

  @Test
  fun testRetrieveLibraryFile_forRealLibrary_returnsFileForLibrary() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createLibrary(dependencyName = "ExampleLib")

    val libFile = testBazelWorkspace.retrieveLibraryFile(dependencyName = "//:ExampleLib")

    assertThat(libFile.exists()).isTrue()
    assertThat(libFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(libFile.name).isEqualTo("ExampleLib.kt")
  }

  @Test
  fun testRetrieveTestDependencyFile_noTest_throwsExceptionWithHelpfulMessage() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val exception = assertThrows<IllegalStateException>() {
      testBazelWorkspace.retrieveTestDependencyFile(testName = "Invalid")
    }

    assertThat(exception).hasMessageThat().contains("No entry for 'Invalid'.")
    assertThat(exception).hasMessageThat().contains("Was the test created without dependencies?")
  }

  @Test
  fun testRetrieveTestDependencyFile_testWithoutGeneratedDep_throwsExceptionWithHelpfulMessage() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest("ValidWithoutDep")

    val exception = assertThrows<IllegalStateException>() {
      testBazelWorkspace.retrieveTestDependencyFile(testName = "ValidWithoutDep")
    }

    // Since the test does not have a generated dependency, there is no entry to retrieve.
    assertThat(exception).hasMessageThat().contains("No entry for 'ValidWithoutDep'.")
    assertThat(exception).hasMessageThat().contains("Was the test created without dependencies?")
  }

  @Test
  fun testRetrieveTestDependencyFile_testWithGeneratedDep_returnsFileForTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest("ValidWithDep", withGeneratedDependency = true)

    val libFile = testBazelWorkspace.retrieveTestDependencyFile(testName = "ValidWithDep")

    assertThat(libFile.exists()).isTrue()
    assertThat(libFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(libFile.name).isEqualTo("ValidWithDepDependency.kt")
  }

  private fun TemporaryFolder.getWorkspaceFile(): File = File(root, "WORKSPACE")

  private fun TemporaryFolder.getBazelVersionFile(): File = File(root, ".bazelversion")

  private fun TemporaryFolder.getBazelRcFile(): File = File(root, ".bazelrc")

  private fun File.isRelativeTo(base: File): Boolean = relativeToOrNull(base) != null

  private fun Iterable<File>.getFileNames(): List<String> = map { it.name }

  private fun Iterable<File>.getRelativeFileNames(root: File): List<String> = map {
    it.toRelativeString(root)
  }

  private fun String.countMatches(regex: String): Int = Regex(regex).findAll(this).toList().size
}
