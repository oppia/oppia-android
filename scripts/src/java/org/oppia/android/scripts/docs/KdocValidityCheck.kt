package org.oppia.android.scripts.docs

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructorDelegationReferenceExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.oppia.android.scripts.common.BinaryProtoResourceLoader
import org.oppia.android.scripts.common.BinaryProtoResourceLoader.Companion.loadProto
import org.oppia.android.scripts.common.BinaryProtoResourceLoaderImpl
import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.proto.KdocValidityExemptions
import java.io.File

/**
 * Script for ensuring the KDocs validity on all non-private:
 * - Classes
 * - Functions
 * - Values/fields
 * - Explicit constructors
 * - Companion objects
 * - Nested classes
 * - Enums
 * - Annotations
 * - Interfaces
 *
 * Note: If any of the above member has an override modifier present, then it is automatically
 * exempted for the KDoc check.
 *
 * Usage:
 *   bazel run //scripts:kdoc_validity_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:kdoc_validity_check -- $(pwd)
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoRoot = File(args[0]).absoluteFile.normalize()
  KdocValidityCheck(repoRoot).checkForMissingKdocs()
}

/**
 * Utility for checking the presence and validity of Kotlin documentation comments.
 *
 * @param repoRoot the root directory of the repository
 * @param binaryProtoResourceLoader the resource loader to use when loading binary proto resources
 */
class KdocValidityCheck(
  private val repoRoot: File,
  private val binaryProtoResourceLoader: BinaryProtoResourceLoader = BinaryProtoResourceLoaderImpl()
) {
  /** IntelliJ Project object, which is required to generate a [KtFile] from a Kotlin file. */
  private val project by lazy {
    val config = CompilerConfiguration()
    config.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
    KotlinCoreEnvironment.createForProduction(
      Disposer.newDisposable(),
      config,
      EnvironmentConfigFiles.JVM_CONFIG_FILES
    ).project
  }

  /**
   * Checks for missing KDocs from the configured [repoRoot] repository Kotlin files, throwing an
   * exception if failures are encountered.
   */
  fun checkForMissingKdocs() {
    // A list of all the files to be exempted for this check.
    val kdocExemptionList =
      binaryProtoResourceLoader.loadProto(
        javaClass, "assets/kdoc_validity_exemptions.pb", KdocValidityExemptions.getDefaultInstance()
      ).exemptedFilePathList
    // A list of all kotlin files in the repo to be analyzed.
    val searchFiles =
      RepositoryFile.collectSearchFiles(repoPath = repoRoot.path, expectedExtension = ".kt")

    // List of files excluding the Test files.
    val filesExcludingTestFiles = searchFiles.filter { file ->
      !file.nameWithoutExtension.endsWith("Test")
    }
    // A list of all kdoc presence failures.
    val kdocPresenceFailures = filesExcludingTestFiles.flatMap(::hasKdocFailure)
    val matchedFilesRelativePaths = kdocPresenceFailures.map {
      it.file.toRelativeString(repoRoot)
    }
    val redundantExemptions = kdocExemptionList.filter { exemption ->
      exemption !in matchedFilesRelativePaths
    }
    val kdocPresenceFailuresAfterExemption = kdocPresenceFailures.filter {
      it.file.toRelativeString(repoRoot) !in kdocExemptionList
    }

    logRedundantExemptions(redundantExemptions)
    logKdocPresenceFailures(kdocPresenceFailuresAfterExemption)

    if (kdocPresenceFailuresAfterExemption.isNotEmpty()) {
      println(
        "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
          "#kdoc-validity-check for more details on how to fix this.\n"
      )
    }
    if (kdocPresenceFailuresAfterExemption.isNotEmpty() || redundantExemptions.isNotEmpty()) {
      throw Exception("KDOC VALIDITY CHECK FAILED")
    } else {
      println("KDOC VALIDITY CHECK PASSED")
    }
  }

  private fun createKtFile(codeString: String, fileName: String): KtFile {
    return PsiManager.getInstance(project)
      .findFile(LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)) as KtFile
  }

  private fun hasKdocFailure(file: File): List<MissingKdocFinding> {
    val ktFile =
      createKtFile(file.readLines().joinToString(separator = System.lineSeparator()), file.name)
    return ktFile.children.filterIsInstance<KtElement>().flatMap {
      recursiveKdocPresenceChecker(it, file)
    }
  }

  private fun recursiveKdocPresenceChecker(elem: KtElement, file: File): List<MissingKdocFinding> {
    return when {
      elem is KtObjectDeclaration && elem.isCompanion() ->
        elem.declarations.flatMap { childElem -> recursiveKdocPresenceChecker(childElem, file) }
      elem is KtClass -> {
        val classKdocMissing = checkIfKDocIsMissing(elem, file)
        val memberMissingKdoc =
          elem.declarations.flatMap { childElem -> recursiveKdocPresenceChecker(childElem, file) }
        (memberMissingKdoc + classKdocMissing).filterNotNull()
      }
      elem is KtObjectDeclaration -> {
        val objectKdocMissing = checkIfKDocIsMissing(elem, file)
        val memberMissingKdoc =
          elem.declarations.flatMap { childElem -> recursiveKdocPresenceChecker(childElem, file) }
        (memberMissingKdoc + objectKdocMissing).filterNotNull()
      }
      elem is KtNamedFunction || elem is KtVariableDeclaration || elem is KtSecondaryConstructor ->
        listOfNotNull(checkIfKDocIsMissing(elem as KtDeclaration, file))
      else -> emptyList()
    }
  }

  private fun checkIfKDocIsMissing(elem: KtDeclaration, file: File): MissingKdocFinding? {
    return if (isKdocRequired(elem) && elem.docComment == null) {
      MissingKdocFinding(file, retrieveLineNumberForElement(elem))
    } else null
  }

  private fun isKdocRequired(elem: KtDeclaration): Boolean {
    if (elem.hasModifier(KtTokens.PRIVATE_KEYWORD) || elem.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
      return false
    }
    val parentContainers =
      elem.parents.filterIsInstance<KtClassOrObject>().filter {
        it is KtClass || it is KtObjectDeclaration
      }
    if (parentContainers.any { it.hasModifier(KtTokens.PRIVATE_KEYWORD) }) {
      // If the element is nested within a private class or object at any level then it's
      // technically private and doesn't require a KDoc.
      return false
    }
    return elem.modifierList?.annotationEntries?.none {
      it.shortName.toString() in KDOC_NOT_REQUIRES_ANNOTATION_ENTRIES
    } ?: true
  }

  private fun retrieveLineNumberForElement(statement: KtElement): Int? {
    val file = statement.containingFile
    if (statement is KtConstructorDelegationReferenceExpression && statement.textLength == 0) {
      // PsiElement for constructor delegation reference is always generated, so we shouldn't mark
      // it's line number if it's empty
      return null
    }
    return file.viewProvider.document?.getLineNumber(statement.textOffset)?.plus(1)
  }

  private fun logKdocPresenceFailures(nonExemptedFailures: List<MissingKdocFinding>) {
    if (nonExemptedFailures.isNotEmpty()) {
      println("KDoc missing for files:")
      nonExemptedFailures.sorted().forEach {
        print("- ${it.file.toRelativeString(repoRoot)}")
        if (it.lineNumber != null) print(":${it.lineNumber}")
        println()
      }
      println()
    }
  }

  private fun logRedundantExemptions(redundantExemptions: List<String>) {
    if (redundantExemptions.isNotEmpty()) {
      println("Redundant exemptions:")
      redundantExemptions.sorted().forEach { exemption ->
        println("- $exemption")
      }
      println("Please remove them from kdoc_validity_exemptions.textproto")
      println()
    }
  }

  private data class MissingKdocFinding(
    val file: File,
    val lineNumber: Int?
  ) : Comparable<MissingKdocFinding> {
    override fun compareTo(other: MissingKdocFinding): Int = COMPARATOR.compare(this, other)

    private companion object {
      private val COMPARATOR =
        compareBy(MissingKdocFinding::file).thenBy(MissingKdocFinding::lineNumber)
    }
  }

  private companion object {
    /**
     * List of annotation entries which, when present on an element, the element does not needs to
     * be checked for a KDoc.
     */
    val KDOC_NOT_REQUIRES_ANNOTATION_ENTRIES =
      listOf(
        "Rule",
        "Mock",
        "Test",
        "Before",
        "Captor",
        "BeforeClass",
        "After",
        "AfterClass",
        "Inject",
        "Provides",
        "Binds",
        "BindsOptionalOf"
      )
  }
}
