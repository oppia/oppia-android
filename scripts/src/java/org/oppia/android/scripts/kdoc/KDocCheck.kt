package org.oppia.android.scripts.kdoc

import org.oppia.android.scripts.common.RepositoryFile
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtConstructorDelegationReferenceExpression
import org.jetbrains.kotlin.psi.doNotAnalyze
import java.io.File
import java.io.FileInputStream
import org.jetbrains.kotlin.lexer.KtTokens
import java.util.Scanner
import org.oppia.android.scripts.proto.KDocExemptions

fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  val kdocExemptiontextProto = "scripts/assets/kdoc_exemptions"

  // A list of all the files to be exempted for this check.
  val kdocExemptionList = loadKdocExemptionsProto(kdocExemptiontextProto).getExemptedFilePathList()

  // A list of all kotlin files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath,
    expectedExtension = ".kt",
    exemptionsList = kdocExemptionList
  )

  val matchedFiles = searchFiles.filter { file -> hasKdocFailure(file) }

  println("testing kdoc")
}

private val project by lazy {
  val config = CompilerConfiguration()
  config.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
  KotlinCoreEnvironment.createForProduction(
    Disposer.newDisposable(),
    config,
    EnvironmentConfigFiles.JVM_CONFIG_FILES
  ).project
}

private fun createKtFile(codeString: String, fileName: String) =
  PsiManager.getInstance(project)
    .findFile(
      LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
    ) as KtFile

private fun hasKdocFailure(file: File): Boolean {
  val ktFile = createKtFile(file.asString(), file.name)
  return ktFile.children.fold(initial = false) { isFailing, elem ->
    val childFailingKdoc = if (elem is KtElement) nester(elem, file) else false
    isFailing || childFailingKdoc
  }
}

private fun nester(elem: KtElement, file: File): Boolean {
  if (elem is KtClass) {
    val classKdocMissing = checkIfKDocMissing(elem, file)
    val memberMissingKdoc = elem.declarations.fold(initial = false) { isMissingKdoc, childElem ->
      val childMissingKdoc = nester(childElem, file)
      isMissingKdoc || childMissingKdoc
    }
    return classKdocMissing || memberMissingKdoc
  } else if (elem is KtObjectDeclaration && elem.isCompanion()) {
    val memberMissingKdoc = elem.declarations.fold(initial = false) { isMissingKdoc, childElem ->
      val childMissingKdoc = nester(childElem, file)
      isMissingKdoc || childMissingKdoc
    }
    return memberMissingKdoc
  } else if (elem is KtNamedFunction
    || elem is KtVariableDeclaration
    || elem is KtSecondaryConstructor
  ) {
    return checkIfKDocMissing(elem as KtDeclaration, file)
  }
  return false
}

private fun checkIfKDocMissing(elem: KtDeclaration, file: File): Boolean {
  if (
    !elem.hasModifier(KtTokens.PRIVATE_KEYWORD)
    && !elem.hasModifier(KtTokens.OVERRIDE_KEYWORD)
  ) {
    elem.getModifierList()?.getAnnotationEntries()?.forEach { it ->
      if (it.getShortName().toString() == "Test") {
        return false
      }
    }
    if (elem.docComment == null) {
      println("$file:${getLineNumberForElement(elem, false)}")
      return true
    }
    return false
  }
  return false
}

private fun getLineNumberForElement(statement: KtElement, markEndOffset: Boolean): Int? {
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

private fun File.asString(): String {
  val fileContents = StringBuilder(this.length().toInt())
  Scanner(this).use { scanner ->
    while (scanner.hasNextLine()) {
      fileContents.append(scanner.nextLine() + System.lineSeparator())
    }
    return fileContents.toString()
  }
}

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