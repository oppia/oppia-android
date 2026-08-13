package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.testing.assertThrows
import java.io.File

/**
 * Tests for [GenerateChangelogs.kt].
 *
 * Pure-logic functions ([parseVersionBzl], [parsePrEntries], [parseFixedIssueNumbers],
 * [buildPrompt], [buildChangelogContent], [buildPrBody], [invokeLlmWithFallback]) are tested
 * directly with no I/O. The integrated [generateChangelogs] function is tested via
 * [FakeCommandExecutor] and [FakeVertexAiClient].
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GenerateChangelogsTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var fakeExecutor: FakeCommandExecutor
  private lateinit var fakeVertexAiClient: FakeVertexAiClient

  @Before
  fun setUp() {
    fakeExecutor = FakeCommandExecutor()
    fakeVertexAiClient = FakeVertexAiClient()
    fakeExecutor.registerHandler("gh") { _, _, _, _ -> 0 }
  }

  // ---------------------------------------------------------------------------
  // main() -- argument validation
  // ---------------------------------------------------------------------------

  @Test
  fun testMain_noArguments_throwsWithUsageMessage() {
    val exception = assertThrows<IllegalArgumentException> { main(emptyArray()) }

    assertThat(exception).hasMessageThat().contains("Usage:")
    assertThat(exception).hasMessageThat().contains("generate_changelogs")
  }

  @Test
  fun testMain_fourArguments_throwsWithUsageMessage() {
    val exception =
      assertThrows<IllegalArgumentException> { main(arrayOf("a", "b", "c", "d")) }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_sevenArguments_throwsWithUsageMessage() {
    val exception =
      assertThrows<IllegalArgumentException> {
        main(arrayOf("a", "b", "c", "d", "e", "f", "g"))
      }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  // ---------------------------------------------------------------------------
  // parseVersionBzl()
  // ---------------------------------------------------------------------------

  @Test
  fun testParseVersionBzl_missingFile_throwsWithPath() {
    val exception =
      assertThrows<IllegalStateException> { parseVersionBzl(tempFolder.root) }

    assertThat(exception).hasMessageThat().contains("version.bzl not found")
    assertThat(exception).hasMessageThat().contains(tempFolder.root.absolutePath)
  }

  @Test
  fun testParseVersionBzl_validContent_returnsMajorAndMinor() {
    writeVersionBzl(major = 0, minor = 18)

    val (major, minor) = parseVersionBzl(tempFolder.root)

    assertThat(major).isEqualTo(0)
    assertThat(minor).isEqualTo(18)
  }

  @Test
  fun testParseVersionBzl_nonZeroMajor_returnsMajorAndMinor() {
    writeVersionBzl(major = 1, minor = 3)

    val (major, minor) = parseVersionBzl(tempFolder.root)

    assertThat(major).isEqualTo(1)
    assertThat(minor).isEqualTo(3)
  }

  @Test
  fun testParseVersionBzl_missingMajorVersion_throwsWithMessage() {
    tempFolder.newFile("version.bzl").writeText("MINOR_VERSION = 18\n")

    val exception =
      assertThrows<IllegalStateException> { parseVersionBzl(tempFolder.root) }

    assertThat(exception).hasMessageThat().contains("MAJOR_VERSION")
  }

  @Test
  fun testParseVersionBzl_missingMinorVersion_throwsWithMessage() {
    tempFolder.newFile("version.bzl").writeText("MAJOR_VERSION = 0\n")

    val exception =
      assertThrows<IllegalStateException> { parseVersionBzl(tempFolder.root) }

    assertThat(exception).hasMessageThat().contains("MINOR_VERSION")
  }

  @Test
  fun testParseVersionBzl_nonNumericMinorValue_throwsWithMessage() {
    tempFolder.newFile("version.bzl").writeText(
      "MAJOR_VERSION = 0\nMINOR_VERSION = \"eighteen\"\n"
    )

    val exception =
      assertThrows<IllegalStateException> { parseVersionBzl(tempFolder.root) }

    assertThat(exception).hasMessageThat().contains("MINOR_VERSION")
  }

  @Test
  fun testParseVersionBzl_extraWhitespaceAroundAssignment_parsesCorrectly() {
    tempFolder.newFile("version.bzl").writeText(
      "MAJOR_VERSION  =  0\nMINOR_VERSION  =  17\n"
    )

    val (major, minor) = parseVersionBzl(tempFolder.root)

    assertThat(major).isEqualTo(0)
    assertThat(minor).isEqualTo(17)
  }

  // ---------------------------------------------------------------------------
  // parsePrEntries()
  // ---------------------------------------------------------------------------

  @Test
  fun testParsePrEntries_emptyList_returnsEmpty() {
    val result = parsePrEntries(emptyList())

    assertThat(result).isEmpty()
  }

  @Test
  fun testParsePrEntries_standardSquashMergeLine_returnsPrEntry() {
    val result = parsePrEntries(listOf("abc1234 Fix flaky test in release flow (#6270)"))

    assertThat(result).hasSize(1)
    assertThat(result[0].number).isEqualTo(6270)
    assertThat(result[0].title).isEqualTo("Fix flaky test in release flow")
  }

  @Test
  fun testParsePrEntries_lineWithNoPrReference_isSkipped() {
    val result = parsePrEntries(listOf("abc1234 Update README with setup instructions"))

    assertThat(result).isEmpty()
  }

  @Test
  fun testParsePrEntries_multipleLines_returnsAllMatchingEntries() {
    val lines = listOf(
      "aaa1111 Add deploy workflow (#6200)",
      "bbb2222 No PR reference here -- skip me",
      "ccc3333 Fix crash on startup (#6215)"
    )

    val result = parsePrEntries(lines)

    assertThat(result).hasSize(2)
    assertThat(result[0].number).isEqualTo(6200)
    assertThat(result[1].number).isEqualTo(6215)
  }

  @Test
  fun testParsePrEntries_prReferenceInMiddleOfTitle_isNotMatched() {
    // PR reference must be at the end of the subject line to match.
    val result = parsePrEntries(listOf("abc1234 Fix (#6200) something after"))

    assertThat(result).isEmpty()
  }

  @Test
  fun testParsePrEntries_titlePreservesHashInBody_returnsCorrectTitle() {
    val result =
      parsePrEntries(listOf("abc1234 Fix part of #6106: Add rollout script (#6270)"))

    assertThat(result).hasSize(1)
    assertThat(result[0].title).isEqualTo("Fix part of #6106: Add rollout script")
    assertThat(result[0].number).isEqualTo(6270)
  }

  // ---------------------------------------------------------------------------
  // parseFixedIssueNumbers()
  // ---------------------------------------------------------------------------

  @Test
  fun testParseFixedIssueNumbers_emptyList_returnsEmpty() {
    val result = parseFixedIssueNumbers(emptyList())

    assertThat(result).isEmpty()
  }

  @Test
  fun testParseFixedIssueNumbers_fixesKeyword_returnsIssueNumber() {
    val result = parseFixedIssueNumbers(listOf("abc1234 Fixes #6100 in crash path (#6270)"))

    assertThat(result).containsExactly(6100)
  }

  @Test
  fun testParseFixedIssueNumbers_fixKeyword_returnsIssueNumber() {
    val result = parseFixedIssueNumbers(listOf("abc1234 Fix #5999 (#6200)"))

    assertThat(result).containsExactly(5999)
  }

  @Test
  fun testParseFixedIssueNumbers_caseInsensitiveFixesKeyword_returnsIssueNumber() {
    val result = parseFixedIssueNumbers(listOf("abc1234 FIXES #6100 (#6270)"))

    assertThat(result).containsExactly(6100)
  }

  @Test
  fun testParseFixedIssueNumbers_duplicateIssueAcrossCommits_returnsDeduplicated() {
    val lines = listOf(
      "aaa Fixes #6100 (#6270)",
      "bbb Fixes #6100 also (#6271)"
    )

    val result = parseFixedIssueNumbers(lines)

    assertThat(result).containsExactly(6100)
  }

  @Test
  fun testParseFixedIssueNumbers_multipleIssuesInOneCommit_returnsAllSorted() {
    val result = parseFixedIssueNumbers(
      listOf("abc Fixes #6200, Fixes #6100 (#6300)")
    )

    assertThat(result).containsExactly(6100, 6200).inOrder()
  }

  @Test
  fun testParseFixedIssueNumbers_noFixesPattern_returnsEmpty() {
    val result =
      parseFixedIssueNumbers(listOf("abc1234 Add changelog for version 0.17 (#6270)"))

    assertThat(result).isEmpty()
  }

  // ---------------------------------------------------------------------------
  // buildPrompt()
  // ---------------------------------------------------------------------------

  @Test
  fun testBuildPrompt_containsVersionInContent() {
    val prompt = buildPrompt("0.17", "- PR title (#100)", "(none)")

    assertThat(prompt).contains("0.17")
  }

  @Test
  fun testBuildPrompt_containsPrListText() {
    val prompt = buildPrompt("0.17", "- My feature PR (#6200)", "(none)")

    assertThat(prompt).contains("- My feature PR (#6200)")
  }

  @Test
  fun testBuildPrompt_containsIssueListText() {
    val prompt = buildPrompt("0.17", "- PR (#100)", "- #6100\n- #6200")

    assertThat(prompt).contains("- #6100")
    assertThat(prompt).contains("- #6200")
  }

  @Test
  fun testBuildPrompt_noIssues_containsNoneMarker() {
    val prompt = buildPrompt("0.17", "- PR (#100)", "(none)")

    assertThat(prompt).contains("(none)")
  }

  // ---------------------------------------------------------------------------
  // buildChangelogContent()
  // ---------------------------------------------------------------------------

  @Test
  fun testBuildChangelogContent_llmSucceeded_containsSummaryOnly() {
    val content = buildChangelogContent(
      summary = "Great release summary.",
      prEntries = listOf(PrEntry(6200, "My feature")),
      issueNumbers = listOf(6100),
      llmFailed = false
    )

    assertThat(content).contains("Great release summary.")
    assertThat(content).doesNotContain(LLM_FALLBACK_MARKER)
    assertThat(content).doesNotContain("Changes in this release")
  }

  @Test
  fun testBuildChangelogContent_llmFailed_containsFallbackMarker() {
    val content = buildChangelogContent(
      summary = LLM_FALLBACK_MARKER,
      prEntries = emptyList(),
      issueNumbers = emptyList(),
      llmFailed = true
    )

    assertThat(content).contains(LLM_FALLBACK_MARKER)
  }

  @Test
  fun testBuildChangelogContent_llmFailed_withPrEntries_containsPrList() {
    val content = buildChangelogContent(
      summary = LLM_FALLBACK_MARKER,
      prEntries = listOf(PrEntry(6200, "Add deploy workflow")),
      issueNumbers = emptyList(),
      llmFailed = true
    )

    assertThat(content).contains("Changes in this release")
    assertThat(content).contains("Add deploy workflow")
  }

  @Test
  fun testBuildChangelogContent_llmFailed_withIssueNumbers_containsIssueSection() {
    val content = buildChangelogContent(
      summary = LLM_FALLBACK_MARKER,
      prEntries = emptyList(),
      issueNumbers = listOf(6100, 6200),
      llmFailed = true
    )

    assertThat(content).contains("Issues addressed")
    assertThat(content).contains("#6100")
    assertThat(content).contains("#6200")
  }

  @Test
  fun testBuildChangelogContent_llmFailed_noPrsOrIssues_noExtraSections() {
    val content = buildChangelogContent(
      summary = LLM_FALLBACK_MARKER,
      prEntries = emptyList(),
      issueNumbers = emptyList(),
      llmFailed = true
    )

    assertThat(content).doesNotContain("Changes in this release")
    assertThat(content).doesNotContain("Issues addressed")
  }

  @Test
  fun testBuildChangelogContent_endsWithSingleNewline() {
    val content = buildChangelogContent(
      summary = "Summary.",
      prEntries = emptyList(),
      issueNumbers = emptyList(),
      llmFailed = false
    )

    assertThat(content).endsWith("\n")
    assertThat(content).doesNotMatch(".*\\n\\n$")
  }

  // ---------------------------------------------------------------------------
  // buildPrBody()
  // ---------------------------------------------------------------------------

  @Test
  fun testBuildPrBody_containsVersionInHeading() {
    val body = buildPrBody(
      version = "0.17", fromSha = "aaa1111", toSha = "bbb2222",
      prEntries = emptyList(), issueNumbers = emptyList(), llmFailed = false
    )

    assertThat(body).contains("0.17")
  }

  @Test
  fun testBuildPrBody_containsCommitRangeLink() {
    val body = buildPrBody(
      version = "0.17", fromSha = "aaa111122223333", toSha = "bbb444455556666",
      prEntries = emptyList(), issueNumbers = emptyList(), llmFailed = false
    )

    assertThat(body).contains("aaa1111")
    assertThat(body).contains("bbb4444")
    assertThat(body).contains("github.com/oppia/oppia-android/compare/")
  }

  @Test
  fun testBuildPrBody_withPrEntries_containsPrLinks() {
    val body = buildPrBody(
      version = "0.17", fromSha = "aaa", toSha = "bbb",
      prEntries = listOf(PrEntry(6200, "Add deploy workflow")),
      issueNumbers = emptyList(), llmFailed = false
    )

    assertThat(body).contains("#6200")
    assertThat(body).contains("Add deploy workflow")
    assertThat(body).contains("github.com/oppia/oppia-android/pull/6200")
  }

  @Test
  fun testBuildPrBody_withIssueNumbers_containsIssueLinks() {
    val body = buildPrBody(
      version = "0.17", fromSha = "aaa", toSha = "bbb",
      prEntries = emptyList(), issueNumbers = listOf(6100), llmFailed = false
    )

    assertThat(body).contains("#6100")
    assertThat(body).contains("github.com/oppia/oppia-android/issues/6100")
  }

  @Test
  fun testBuildPrBody_llmFailed_containsWarningBlock() {
    val body = buildPrBody(
      version = "0.17", fromSha = "aaa", toSha = "bbb",
      prEntries = emptyList(), issueNumbers = emptyList(), llmFailed = true
    )

    assertThat(body).contains("LLM generation failed")
    assertThat(body).contains(LLM_FALLBACK_MARKER)
  }

  @Test
  fun testBuildPrBody_llmSucceeded_noWarningBlock() {
    val body = buildPrBody(
      version = "0.17", fromSha = "aaa", toSha = "bbb",
      prEntries = emptyList(), issueNumbers = emptyList(), llmFailed = false
    )

    assertThat(body).doesNotContain("LLM generation failed")
  }

  // ---------------------------------------------------------------------------
  // invokeLlmWithFallback()
  // ---------------------------------------------------------------------------

  @Test
  fun testInvokeLlmWithFallback_successfulCall_returnsSummaryAndFalse() {
    fakeVertexAiClient = FakeVertexAiClient(defaultResponse = "Great release summary.")

    val (summary, failed) = invokeLlmWithFallback(fakeVertexAiClient, "prompt text")

    assertThat(summary).isEqualTo("Great release summary.")
    assertThat(failed).isFalse()
  }

  @Test
  fun testInvokeLlmWithFallback_clientThrows_returnsFallbackMarkerAndTrue() {
    fakeVertexAiClient.shouldFail = true

    val (summary, failed) = invokeLlmWithFallback(fakeVertexAiClient, "prompt text")

    assertThat(summary).isEqualTo(LLM_FALLBACK_MARKER)
    assertThat(failed).isTrue()
  }

  @Test
  fun testInvokeLlmWithFallback_clientThrows_promptIsStillRecorded() {
    fakeVertexAiClient.shouldFail = true

    invokeLlmWithFallback(fakeVertexAiClient, "my prompt")

    assertThat(fakeVertexAiClient.receivedPrompts).containsExactly("my prompt")
  }

  // ---------------------------------------------------------------------------
  // generateChangelogs() -- integrated tests
  // ---------------------------------------------------------------------------

  @Test
  fun testGenerateChangelogs_changelogAlreadyExists_doesNotCallLlm() {
    writeVersionBzl(major = 0, minor = 18)
    val changelogsDir = tempFolder.newFolder("config", "changelogs")
    File(changelogsDir, "0.17.md").writeText("Existing changelog.")

    generateChangelogs(
      workspaceRoot = tempFolder.root,
      commandExecutor = fakeExecutor,
      vertexAiClient = fakeVertexAiClient
    )

    assertThat(fakeVertexAiClient.receivedPrompts).isEmpty()
  }

  @Test
  fun testGenerateChangelogs_minorVersionIsZero_throwsWithMessage() {
    writeVersionBzl(major = 0, minor = 0)

    val exception = assertThrows<IllegalStateException> {
      generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)
    }

    assertThat(exception).hasMessageThat().contains("MINOR_VERSION")
    assertThat(exception).hasMessageThat().contains("0")
  }

  @Test
  fun testGenerateChangelogs_llmSucceeds_writesChangelogWithSummary() {
    writeVersionBzl(major = 0, minor = 18)
    fakeVertexAiClient = FakeVertexAiClient(defaultResponse = "LLM-generated summary.")
    setupStandardGitHandlers(mergeBaseSha = "deadbeef")

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    val changelogFile = File(tempFolder.root, "config/changelogs/0.17.md")
    assertThat(changelogFile.exists()).isTrue()
    assertThat(changelogFile.readText()).contains("LLM-generated summary.")
    assertThat(changelogFile.readText()).doesNotContain(LLM_FALLBACK_MARKER)
  }

  @Test
  fun testGenerateChangelogs_llmFails_writesChangelogWithFallbackMarker() {
    writeVersionBzl(major = 0, minor = 18)
    fakeVertexAiClient.shouldFail = true
    setupStandardGitHandlers(mergeBaseSha = "deadbeef")

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    val changelogFile = File(tempFolder.root, "config/changelogs/0.17.md")
    assertThat(changelogFile.exists()).isTrue()
    assertThat(changelogFile.readText()).contains(LLM_FALLBACK_MARKER)
  }

  @Test
  fun testGenerateChangelogs_llmSucceeds_promptContainsVersion() {
    writeVersionBzl(major = 0, minor = 18)
    setupStandardGitHandlers(mergeBaseSha = "deadbeef")

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    assertThat(fakeVertexAiClient.receivedPrompts).hasSize(1)
    assertThat(fakeVertexAiClient.receivedPrompts[0]).contains("0.17")
  }

  @Test
  fun testGenerateChangelogs_withPrsInLog_promptContainsPrTitles() {
    writeVersionBzl(major = 0, minor = 18)
    setupStandardGitHandlers(
      mergeBaseSha = "deadbeef",
      logLines = listOf("abc1234 Add cool feature (#6300)")
    )

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    assertThat(fakeVertexAiClient.receivedPrompts[0]).contains("Add cool feature (#6300)")
  }

  @Test
  fun testGenerateChangelogs_prevBranchNotFound_fallsBackAndStillCallsLlm() {
    writeVersionBzl(major = 0, minor = 18)
    val firstCommit = "firstcommitsha"
    val toSha = "toshasha123"
    var mergeBaseCallCount = 0
    fakeExecutor.registerHandler("git") { _, args, out, _ ->
      when {
        args.contains("merge-base") -> {
          mergeBaseCallCount++
          if (mergeBaseCallCount == 1) {
            out.println(toSha); 0
          } else {
            // Previous release branch not found -- simulate failure with realistic git output.
            out.println("fatal: Not a valid object name: unknown revision 'release-0.16'"); 1
          }
        }
        args.contains("--max-parents=0") -> { out.println(firstCommit); 0 }
        args.contains("log") -> { out.println(""); 0 }
        else -> { out.println(""); 0 }
      }
    }

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    assertThat(fakeVertexAiClient.receivedPrompts).hasSize(1)
  }

  @Test
  fun testGenerateChangelogs_prevBranchAmbiguousArgument_fallsBackToFirstCommit() {
    // Simulates the second git error phrase that indicates a missing branch:
    // "ambiguous argument" (e.g. git merge-base origin/release-0.16 origin/develop).
    writeVersionBzl(major = 0, minor = 18)
    val firstCommit = "firstcommitsha"
    val toSha = "toshasha456"
    var mergeBaseCallCount = 0
    fakeExecutor.registerHandler("git") { _, args, out, _ ->
      when {
        args.contains("merge-base") -> {
          mergeBaseCallCount++
          if (mergeBaseCallCount == 1) {
            out.println(toSha); 0
          } else {
            // Simulate "ambiguous argument" failure for the previous release branch.
            out.println("fatal: ambiguous argument 'release-0.16': unknown revision"); 1
          }
        }
        args.contains("--max-parents=0") -> { out.println(firstCommit); 0 }
        args.contains("log") -> { out.println(""); 0 }
        else -> { out.println(""); 0 }
      }
    }

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    assertThat(fakeVertexAiClient.receivedPrompts).hasSize(1)
  }

  @Test
  fun testGenerateChangelogs_prevBranchMergeBaseFailsWithUnrelatedError_rethrows() {
    // If git merge-base fails for a reason unrelated to a missing branch (e.g. a corrupt
    // repository), the exception must propagate rather than being silently swallowed.
    writeVersionBzl(major = 0, minor = 18)
    val toSha = "toshasha789"
    var mergeBaseCallCount = 0
    fakeExecutor.registerHandler("git") { _, args, out, _ ->
      when {
        args.contains("merge-base") -> {
          mergeBaseCallCount++
          if (mergeBaseCallCount == 1) {
            out.println(toSha); 0
          } else {
            // Simulate an unrelated git failure that must NOT be swallowed.
            out.println("fatal: unable to read tree"); 1
          }
        }
        else -> { out.println(""); 0 }
      }
    }

    assertThrows<IllegalStateException> {
      generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)
    }
  }

  @Test
  fun testGenerateChangelogs_firstEverRelease_prevMinorIsZero_usesFirstCommit() {
    // minor=1 means prevMinor=0, which triggers the first-release path.
    writeVersionBzl(major = 0, minor = 1)
    val firstCommit = "firstcommitsha"
    val toSha = "toshasha123"
    fakeExecutor.registerHandler("git") { _, args, out, _ ->
      when {
        args.contains("merge-base") -> { out.println(toSha); 0 }
        args.contains("--max-parents=0") -> { out.println(firstCommit); 0 }
        args.contains("log") -> { out.println(""); 0 }
        else -> { out.println(""); 0 }
      }
    }

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    assertThat(fakeVertexAiClient.receivedPrompts).hasSize(1)
  }

  @Test
  fun testGenerateChangelogs_changelogFileWrittenToCorrectPath() {
    writeVersionBzl(major = 0, minor = 18)
    setupStandardGitHandlers(mergeBaseSha = "deadbeef")

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    val expectedPath = File(tempFolder.root, "config/changelogs/0.17.md")
    assertThat(expectedPath.exists()).isTrue()
  }

  @Test
  fun testGenerateChangelogs_nonZeroMajorVersion_writesCorrectChangelogFileName() {
    writeVersionBzl(major = 1, minor = 5)
    setupStandardGitHandlers(mergeBaseSha = "deadbeef")

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    val expectedPath = File(tempFolder.root, "config/changelogs/1.4.md")
    assertThat(expectedPath.exists()).isTrue()
  }

  @Test
  fun testGenerateChangelogs_ghPrCreate_usesCorrectBranchName() {
    writeVersionBzl(major = 0, minor = 18)
    var capturedGhArgs: List<String> = emptyList()
    setupStandardGitHandlers(mergeBaseSha = "deadbeef")
    fakeExecutor.registerHandler("gh") { _, args, out, _ ->
      capturedGhArgs = args
      out.println("https://github.com/oppia/oppia-android/pull/9999")
      0
    }

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    assertThat(capturedGhArgs).contains("--head")
    val headIndex = capturedGhArgs.indexOf("--head")
    assertThat(capturedGhArgs[headIndex + 1]).isEqualTo("automated/changelog-0.17")
  }

  @Test
  fun testGenerateChangelogs_ghPrCreate_bodyContainsVersionAndCommitRange() {
    writeVersionBzl(major = 0, minor = 18)
    var capturedGhArgs: List<String> = emptyList()
    setupStandardGitHandlers(mergeBaseSha = "deadbeef")
    fakeExecutor.registerHandler("gh") { _, args, out, _ ->
      capturedGhArgs = args
      out.println("https://github.com/oppia/oppia-android/pull/9999")
      0
    }

    generateChangelogs(tempFolder.root, fakeExecutor, fakeVertexAiClient)

    val bodyIndex = capturedGhArgs.indexOf("--body")
    assertThat(bodyIndex).isGreaterThan(-1)
    val prBody = capturedGhArgs[bodyIndex + 1]
    assertThat(prBody).contains("0.17")
    assertThat(prBody).contains("deadbeef")
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------
  private fun writeVersionBzl(major: Int, minor: Int) {
    tempFolder.newFile("version.bzl").writeText(
      "MAJOR_VERSION = $major\nMINOR_VERSION = $minor\n"
    )
  }

  /**
   * Registers git and gh handlers on [fakeExecutor] that model the happy-path flow:
   * - `git merge-base` returns [mergeBaseSha]
   * - `git log` returns [logLines]
   * - All other git sub-commands (config, checkout, add, commit, push) succeed silently
   * - `gh pr create` succeeds silently
   */
  private fun setupStandardGitHandlers(
    mergeBaseSha: String,
    logLines: List<String> = emptyList()
  ) {
    fakeExecutor.registerHandler("git") { _, args, out, _ ->
      when {
        args.contains("merge-base") -> { out.println(mergeBaseSha); 0 }
        args.contains("log") -> { logLines.forEach { out.println(it) }; 0 }
        else -> { out.println(""); 0 }
      }
    }
    fakeExecutor.registerHandler("gh") { _, _, out, _ ->
      out.println("https://github.com/oppia/oppia-android/pull/9999")
      0
    }
  }
}
