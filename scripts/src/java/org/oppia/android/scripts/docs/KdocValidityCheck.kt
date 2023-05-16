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
import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.proto.KdocValidityExemptions
import java.io.File
import java.io.InputStream

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
  val repoPath = "${args[0]}/"

  // List of annotation entries which when present on an element, the element does not needs to be
  // checked for a KDoc.
  val kDocNotRequiredAnnotationEntryList = listOf(
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

  // A list of all the files to be exempted for this check.
  val kdocExemptionList =
    ResourceLoader.loadResource("assets/kdoc_validity_exemptions.pb")
      .use(InputStream::loadKdocExemptionsProto)
      .exemptedFilePathList
  // A list of all kotlin files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath,
    expectedExtension = ".kt"
  )

  // List of files excluding the Test files.
  val filesExcludingTestFiles = searchFiles.filter { file ->
    !file.nameWithoutExtension.endsWith("Test")
  }
  // A list of all kdoc presence failures.
  val kdocPresenceFailures = filesExcludingTestFiles.flatMap { file ->
    hasKdocFailure(file, kDocNotRequiredAnnotationEntryList)
  }
  val matchedFilesRelativePaths = kdocPresenceFailures.map {
    RepositoryFile.retrieveRelativeFilePath(it.first, repoPath)
  }
  val redundantExemptions = kdocExemptionList.filter { exemption ->
    exemption !in matchedFilesRelativePaths
  }
  val kdocPresenceFailuresAfterExemption = kdocPresenceFailures.filter {
    RepositoryFile.retrieveRelativeFilePath(it.first, repoPath) !in kdocExemptionList
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
 * Generates a [KtFile] from a Kotlin file.
 *
 * @param codeString the string equivalent of the Kotlin file
 * @param fileName name of the file
 * @return the generated [KtFile] for the given file
 */
private fun createKtFile(codeString: String, fileName: String): KtFile {
  return PsiManager.getInstance(project)
    .findFile(LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)) as KtFile
}

/**
 * Checks whether a file has a KDoc presence failure.
 *
 * @param file the file to be checked
 * @param kDocNotRequiredAnnotationEntryList the list of annotation entries which when present
 *     on an element, the element does not needs to be checked for a KDoc.
 * @return a list of elements in the file which are missing KDocs
 */
private fun hasKdocFailure(
  file: File,
  kDocNotRequiredAnnotationEntryList: List<String>
): List<Pair<File, Int?>> {
  val ktFile = createKtFile(
    file.readLines().joinToString(separator = System.lineSeparator()),
    file.name
  )
  return ktFile.children.flatMap { elem ->
    if (elem is KtElement) {
      recursiveKdocPresenceChecker(
        elem,
        file,
        kDocNotRequiredAnnotationEntryList
      )
    } else {
      emptyList()
    }
  }
}

/**
 * Recursively iterates over an element and checks for a KDoc presence.
 *
 * @param elem the element over which we have to iterate
 * @param file the file to be checked for the KDoc presence
 * @param kDocNotRequiredAnnotationEntryList the list of annotation entries which when present
 *     on an element, the element does not needs to be checked for a KDoc.
 * @return a list of elements which are missing KDocs
 */
private fun recursiveKdocPresenceChecker(
  elem: KtElement,
  file: File,
  kDocNotRequiredAnnotationEntryList: List<String>
): List<Pair<File, Int?>> {
  when {
    elem is KtObjectDeclaration && elem.isCompanion() -> {
      val memberMissingKdoc = elem.declarations.flatMap { childElem ->
        recursiveKdocPresenceChecker(
          childElem,
          file,
          kDocNotRequiredAnnotationEntryList
        )
      }
      return memberMissingKdoc
    }
    elem is KtClass -> {
      val classKdocMissing = checkIfKDocIsMissing(elem, file, kDocNotRequiredAnnotationEntryList)
      val memberMissingKdoc = elem.declarations.flatMap { childElem ->
        recursiveKdocPresenceChecker(
          childElem,
          file,
          kDocNotRequiredAnnotationEntryList
        )
      }
      return (memberMissingKdoc + classKdocMissing).filterNotNull()
    }
    elem is KtObjectDeclaration -> {
      val objectKdocMissing = checkIfKDocIsMissing(elem, file, kDocNotRequiredAnnotationEntryList)
      val memberMissingKdoc = elem.declarations.flatMap { childElem ->
        recursiveKdocPresenceChecker(
          childElem,
          file,
          kDocNotRequiredAnnotationEntryList
        )
      }
      return (memberMissingKdoc + objectKdocMissing).filterNotNull()
    }
    elem is KtNamedFunction || elem is KtVariableDeclaration || elem is KtSecondaryConstructor ->
      return listOfNotNull(
        checkIfKDocIsMissing(elem as KtDeclaration, file, kDocNotRequiredAnnotationEntryList)
      )
    else -> return emptyList()
  }
}

/**
 * Checks if an element is missing a KDoc.
 *
 * @param elem the element to be checked
 * @param file the file to be checked for KDoc presence
 * @param kDocNotRequiredAnnotationEntryList the list of annotation entries which when present
 *     on an element, the element does not needs to be checked for a KDoc.
 * @return Returns the pair of file and line number if KDoc is missing, else returns null
 */
private fun checkIfKDocIsMissing(
  elem: KtDeclaration,
  file: File,
  kDocNotRequiredAnnotationEntryList: List<String>
): Pair<File, Int?>? {
  if (!isKdocRequired(elem, kDocNotRequiredAnnotationEntryList)) {
    return null
  }
  if (elem.docComment == null) {
    return Pair(file, retrieveLineNumberForElement(elem))
  }
  return null
}

/**
 * Returns whether an element is required to be checked for a KDoc presence.
 *
 * @param elem the element to be checked
 * @param exemptionList the list of annotation entries which, when present on an element, the
 *     element does not needs to be checked for a KDoc.
 * @return whether a KDoc is required for this element
 */
private fun isKdocRequired(
  elem: KtDeclaration,
  exemptionList: List<String>
): Boolean {
  if (elem.hasModifier(KtTokens.PRIVATE_KEYWORD) || elem.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
    return false
  }
  val parentContainers =
    elem.parents.filterIsInstance<KtClassOrObject>().filter {
      it is KtClass || it is KtObjectDeclaration
    }
  if (parentContainers.any { it.hasModifier(KtTokens.PRIVATE_KEYWORD) }) {
    // If the element is nested within a private class or object at any level then it's technically
    // private and doesn't require a KDoc.
    return false
  }
  return elem.modifierList?.annotationEntries?.none {
    it.shortName.toString() in exemptionList
  } ?: true
}

/**
 * Retrieves the line number for a particular [KtElement].
 *
 * @param statement the [KtElement] for which we want to get the line number
 * @return the line number
 */
private fun retrieveLineNumberForElement(statement: KtElement): Int? {
  val file = statement.containingFile
  if (statement is KtConstructorDelegationReferenceExpression && statement.textLength == 0) {
    // PsiElement for constructor delegation reference is always generated, so we shouldn't mark
    // it's line number if it's empty
    return null
  }
  return file.viewProvider.document?.getLineNumber(statement.textOffset)?.plus(1)
}

/**
 * Logs the failures for KDoc validity check.
 *
 * @param kdocPresenceFailuresAfterExemption list of KDoc presence failures
 */
private fun logKdocPresenceFailures(kdocPresenceFailuresAfterExemption: List<Pair<File, Int?>>) {
  if (kdocPresenceFailuresAfterExemption.isNotEmpty()) {
    println("KDoc missing for files:")
    kdocPresenceFailuresAfterExemption.sortedWith(compareBy({ it.first }, { it.second })).forEach {
      println("- ${it.first}:${it.second}")
    }
    println()
  }
}

/**
 * Logs the redundant exemptions.
 *
 * @param redundantExemptions list of redundant exemptions
 */
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

private fun InputStream.loadKdocExemptionsProto(): KdocValidityExemptions =
  KdocValidityExemptions.newBuilder().mergeFrom(this).build()

private object ResourceLoader {
  fun loadResource(name: String): InputStream {
    return checkNotNull(ResourceLoader::class.java.getResourceAsStream(name)) {
      "Failed to find resource corresponding to name: $name."
    }
  }
}
