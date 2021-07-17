package org.oppia.android.scripts.kdoc

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
import org.jetbrains.kotlin.psi.KtConstructorDelegationReferenceExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.doNotAnalyze
import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.proto.KDocExemptions
import java.io.File
import java.io.FileInputStream
import java.util.Scanner

/**
 * Script for ensuring that KDocs are present on all non-private:
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
 *   bazel run //scripts:kdoc_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:kdoc_check -- $(pwd)
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  val kdocExemptiontextProto = "scripts/assets/kdoc_exemptions"

  // List of annotation entries which when present on an element, the element does not needs to be
  // checked for a KDoc.
  val kDocNotRequiredAnnotationEntryList = listOf<String>(
    "Rule",
    "Mock",
    "Test",
    "Before",
    "Captor",
    "BeforeClass",
    "After",
    "AfterClass",
    "Inject",
    "Provides"
  )

  // A list of all the files to be exempted for this check.
  val kdocExemptionList = loadKdocExemptionsProto(kdocExemptiontextProto).getExemptedFilePathList()

  // A list of all kotlin files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath,
    expectedExtension = ".kt",
    exemptionsList = kdocExemptionList
  )

  // A list of all the files missing KDoc(s).
  val matchedFiles = searchFiles.filter { file ->
    hasKdocFailure(file, kDocNotRequiredAnnotationEntryList)
  }

  if (matchedFiles.isNotEmpty()) {
    throw Exception("KDOC CHECK FAILED")
  } else {
    println("KDOC CHECK PASSED")
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
 * Genrates a [KtFile] from a Kotlin file.
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
 * @return whether the file has a KDoc presence failure
 */
private fun hasKdocFailure(file: File, kDocNotRequiredAnnotationEntryList: List<String>): Boolean {
  val ktFile = createKtFile(file.asString(), file.name)
  return ktFile.children.fold(initial = false) { isFailing, elem ->
    val childFailingKdoc = if (elem is KtElement) elementIterator(
      elem,
      file,
      kDocNotRequiredAnnotationEntryList
    ) else false
    isFailing || childFailingKdoc
  }
}

/**
 * Returns the string equivalent of a file.
 *
 * @return the string equivalent
 */
private fun File.asString(): String {
  val fileContents = StringBuilder(this.length().toInt())
  Scanner(this).use { scanner ->
    while (scanner.hasNextLine()) {
      fileContents.append(scanner.nextLine() + System.lineSeparator())
    }
    return fileContents.toString()
  }
}

/**
 * Recursively iterates over an element and checks for a KDoc presence.
 *
 * @param elem the element over which we have to iterate
 * @param file the file to be checked for the KDoc presence
 * @param kDocNotRequiredAnnotationEntryList the list of annotation entries which when present
 *     on an element, the element does not needs to be checked for a KDoc.
 * @return whether the element is missing a KDoc
 */
private fun elementIterator(
  elem: KtElement,
  file: File,
  kDocNotRequiredAnnotaionEntryList: List<String>
): Boolean {
  if (elem is KtClass) {
    val classKdocMissing = checkIfKDocMissing(elem, file, kDocNotRequiredAnnotaionEntryList)
    val memberMissingKdoc = elem.declarations.fold(initial = false) { isMissingKdoc, childElem ->
      val childMissingKdoc = elementIterator(childElem, file, kDocNotRequiredAnnotaionEntryList)
      isMissingKdoc || childMissingKdoc
    }
    return classKdocMissing || memberMissingKdoc
  } else if (elem is KtObjectDeclaration && elem.isCompanion()) {
    val memberMissingKdoc = elem.declarations.fold(initial = false) { isMissingKdoc, childElem ->
      val childMissingKdoc = elementIterator(childElem, file, kDocNotRequiredAnnotaionEntryList)
      isMissingKdoc || childMissingKdoc
    }
    return memberMissingKdoc
  } else if (elem is KtNamedFunction ||
    elem is KtVariableDeclaration ||
    elem is KtSecondaryConstructor
  ) {
    return checkIfKDocMissing(elem as KtDeclaration, file, kDocNotRequiredAnnotaionEntryList)
  }
  return false
}

/**
 * Checks if an element is missing a KDoc.
 *
 * @param elem the element to be checked
 * @param file the file to be checked for KDoc presence
 * @param kDocNotRequiredAnnotationEntryList the list of annotation entries which when present
 *     on an element, the element does not needs to be checked for a KDoc.
 * @return whether the element is missing a KDoc
 */
private fun checkIfKDocMissing(
  elem: KtDeclaration,
  file: File,
  kDocNotRequiredAnnotationEntryList: List<String>
): Boolean {
  if (!isKdocRequired(elem, kDocNotRequiredAnnotationEntryList)) {
    return false
  }
  if (elem.docComment == null) {
    println("$file:${retrieveLineNumberForElement(elem, false)}: missing KDoc")
    return true
  }
  return false
}

/**
 * Returns whether an element is required to be checked for a KDoc presence
 *
 * @param elem the element to be checked
 * @param kDocNotRequiredAnnotationEntryList the list of annotation entries which when present
 *     on an element, the element does not needs to be checked for a KDoc.
 * @return whether a KDoc is required for this element
 */
private fun isKdocRequired(
  elem: KtDeclaration,
  kDocNotRequiredAnnotationEntryList: List<String>
): Boolean {
  if (elem.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
    return false
  }
  if (elem.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
    return false
  }
  elem.getModifierList()?.getAnnotationEntries()?.forEach { it ->
    if (it.getShortName().toString() in kDocNotRequiredAnnotationEntryList) {
      return false
    }
  }
  return true
}

/**
 * Retrieves the line number for a particular [KtElement].
 *
 * @param statement the [KtElement] for which we want to get the line number
 * @param markEndOffset Whether to mark the end offset for the given element. For example:
 *     when set to true, the function will return the line number where the statement ends.
 *     Similarly, when this is set to false, the function returns the line number where the
 *     statement starts.
 * @return the line number
 */
private fun retrieveLineNumberForElement(statement: KtElement, markEndOffset: Boolean): Int? {
  val file = statement.containingFile
  if (file is KtFile && file.doNotAnalyze != null) {
    return null
  }
  if (statement is KtConstructorDelegationReferenceExpression && statement.textLength == 0) {
    // PsiElement for constructor delegation reference is always generated, so we shouldn't mark
    // it's line number if it's empty
    return null
  }
  val document = file.viewProvider.document
  return document?.getLineNumber(
    if (markEndOffset) statement.textRange.endOffset else statement.textOffset
  )?.plus(1)
}

/**
 * Loads the KDoc exemptions list to proto.
 *
 * @param kdocExemptiontextProto the location of the kdoc exemption textproto file
 * @return proto class from the parsed textproto file
 */
private fun loadKdocExemptionsProto(kdocExemptiontextProto: String): KDocExemptions {
  val protoBinaryFile = File("$kdocExemptiontextProto.pb")
  val builder = KDocExemptions.getDefaultInstance().newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  @Suppress("UNCHECKED_CAST")
  val protoObj: KDocExemptions =
    FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as KDocExemptions
  return protoObj
}
