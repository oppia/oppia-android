package org.oppia.android.scripts.build

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.oppia.android.scripts.common.RepositoryFile
import java.io.File

fun main(vararg args: String) {
  require(args.size == 1) {
    "Usage: bazel run //scripts:dagger_module_consistency_and_correctness_check --" +
      " </path/to/repo/root>"
  }
  val repoRoot = File(args[0]).absoluteFile.normalize()
  DaggerModuleConsistencyAndCorrectnessCheck(repoRoot).checkForModuleConsistencyAndCorrectness()
}

class DaggerModuleConsistencyAndCorrectnessCheck(private val repoRoot: File) {
  private val project by lazy {
    val config = CompilerConfiguration()
    config.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
    KotlinCoreEnvironment.createForProduction(
      Disposer.newDisposable(),
      config,
      EnvironmentConfigFiles.JVM_CONFIG_FILES
    ).project
  }

  fun checkForModuleConsistencyAndCorrectness() {
    // First, verify that all classes marked as a module are named & annotated correctly.
    val kotlinFiles =
      RepositoryFile.collectSearchFiles(
        repoRoot.path,
        expectedExtension = "kt",
        exemptionsList = GLIDE_MODULE_EXEMPTIONS.toList()
      )
    val filesMarkedAsModule = kotlinFiles.filterTo(mutableSetOf()) { it.isAnnotatedAsModule() }
    val filesNamedAsModule = kotlinFiles.filterTo(mutableSetOf()) { it.isNamedAsModule() }
    if (filesMarkedAsModule != filesNamedAsModule) {
      val filesMissingDeclarations = filesNamedAsModule - filesMarkedAsModule
      val filesMisnamed = filesMarkedAsModule - filesNamedAsModule
      if (filesMissingDeclarations.isNotEmpty()) {
        println("There are ${filesMissingDeclarations.size} file(s) with missing @Module:")
        filesMissingDeclarations.sorted().forEach { println("- ${it.toRelativeString(repoRoot)}") }
        println()
      }
      if (filesMisnamed.isNotEmpty()) {
        println("There are ${filesMisnamed.size} file(s) that should be in a 'Module.kt' file:")
        filesMisnamed.sorted().forEach { println("- ${it.toRelativeString(repoRoot)}") }
        println()
      }

      error("Some modules are missing annotations or modules with the wrong file name.")
    }

    // Second, verify that no modules start with 'Debug' or 'Test' since these should go toward the
    // end of the filename.
    val modulesStartingWithDebug =
      filesNamedAsModule.filterTo(mutableSetOf()) { it.nameWithoutExtension.startsWith("Debug") }
    val modulesStartingWithTest =
      filesNamedAsModule.filterTo(mutableSetOf()) {
        // Some modules can start with 'testing' legitimately as non-test modules.
        it.nameWithoutExtension.startsWith("Test") && !it.nameWithoutExtension.startsWith("Testing")
      }
    if (modulesStartingWithDebug.isNotEmpty() || modulesStartingWithTest.isNotEmpty()) {
      if (modulesStartingWithDebug.isNotEmpty()) {
        println("${modulesStartingWithDebug.size} file(s) incorrectly start with 'Debug':")
        modulesStartingWithDebug.sorted().forEach { println("- ${it.toRelativeString(repoRoot)}") }
        println()
        println("Rename to end with 'DebugModule', instead.")
        println()
      }
      if (modulesStartingWithTest.isNotEmpty()) {
        println("${modulesStartingWithTest.size} file(s) incorrectly start with 'Test':")
        modulesStartingWithTest.sorted().forEach { println("- ${it.toRelativeString(repoRoot)}") }
        println()
        println("Rename to end with 'TestModule', instead.")
        println()
      }

      error("At least one module file has an invalid name prefix.")
    }

    // Third, check that all debug and test modules have correspond production variants (as part of
    // checking that all modules confirm to the naming style).
    val prodModules = filesNamedAsModule.filterTo(mutableSetOf()) { it.isNamedAsProdModule() }
    val debugModules = filesNamedAsModule.filterTo(mutableSetOf()) { it.isNamedAsDebugModule() }
    val testModules = filesNamedAsModule.filterTo(mutableSetOf()) { it.isNamedAsTestModule() }
    val debugModulesWithoutProdModules = debugModules.filter { dbgMod ->
      val prodModuleName = dbgMod.nameWithoutExtension.removeSuffix("DebugModule") + "ProdModule"
      prodModules.none { it.nameWithoutExtension == prodModuleName }
    }
    val testModulesWithoutProdModules = testModules.filter { testMod ->
      val prodModuleName = testMod.nameWithoutExtension.removeSuffix("TestModule") + "ProdModule"
      prodModules.none { it.nameWithoutExtension == prodModuleName }
    }.filterNot { it.toRelativeString(repoRoot) in TEST_MODULE_EXEMPTIONS }
    if (debugModulesWithoutProdModules.isNotEmpty() || testModulesWithoutProdModules.isNotEmpty()) {
      if (debugModulesWithoutProdModules.isNotEmpty()) {
        println(
          "There are ${debugModulesWithoutProdModules.size} debug module(s) without a" +
            " corresponding prod module:"
        )
        debugModulesWithoutProdModules.forEach { println("- ${it.toRelativeString(repoRoot)}") }
        println()
      }
      if (testModulesWithoutProdModules.isNotEmpty()) {
        println(
          "There are ${testModulesWithoutProdModules.size} test module(s) without a" +
            " corresponding prod module:"
        )
        testModulesWithoutProdModules.forEach { println("- ${it.toRelativeString(repoRoot)}") }
        println()
      }

      error("At least one debug/test module doesn't have a known corresponding prod module.")
    }

    // Fourth, check that all debug modules provide at least the same bindings as their prod mod.
    // Also check that extra bindings don't correspond to different prod modules (since that
    // indicates cross-module inconsistencies).
    val debugModulesWithMatchingProd =
      debugModules.associateWith { dbgMod ->
        val prodModuleName = dbgMod.nameWithoutExtension.removeSuffix("DebugModule") + "ProdModule"
        // This null check shouldn't ever fail since it's already been vetted above.
        checkNotNull(prodModules.find { it.nameWithoutExtension == prodModuleName })
      }
    val trueProdModules = filesNamedAsModule - (debugModules + testModules)
    debugModulesWithMatchingProd.forEach { (dbgMod, prodMod) ->
      val debugModuleTypes = loadProvidedTypes(dbgMod).toSet()
      val prodModuleTypes = loadProvidedTypes(prodMod).toSet()
      val missingTypes = prodModuleTypes - debugModuleTypes
      if (missingTypes.isNotEmpty()) {
        println("Debug module does not rebind expected types from its corresponding prod module:")
        missingTypes.forEach { println("- ${it.toReadableString()}") }
        println()
        println("Debug module file: ${dbgMod.toRelativeString(repoRoot)}.")
        println("Production module file: ${prodMod.toRelativeString(repoRoot)}.")
        println()
        error("Failed to verify that all debug modules replicate their prod module bindings.")
      }

      // It's okay for debug modules to bind extra things, but these should be unique to the module.
      val extraTypes = debugModuleTypes - prodModuleTypes
      if (extraTypes.isNotEmpty()) {
        trueProdModules.forEach { otherProdModule ->
          val nonOverlappingTypes = extraTypes - loadProvidedTypes(otherProdModule).toSet()
          if (extraTypes.size != nonOverlappingTypes.size) {
            println("Debug module includes bindings from other production modules:")
            (extraTypes - nonOverlappingTypes).forEach { println("- ${it.toReadableString()}") }
            println()
            println("Debug module file: ${dbgMod.toRelativeString(repoRoot)}.")
            println("Other production module file: ${otherProdModule.toRelativeString(repoRoot)}.")
            println()
            println("These should be moved to the correct debug module.")
          }
        }
      }
    }

    // Fifth, check that all test modules provide at least the same bindings as their prod mod.
    // Also check that extra bindings don't correspond to different prod modules (since that
    // indicates cross-module inconsistencies).
    val testModulesWithMatchingProd =
      testModules.filterNot {
        it.toRelativeString(repoRoot) in TEST_MODULE_EXEMPTIONS
      }.associateWith { testMod ->
        val prodModuleName = testMod.nameWithoutExtension.removeSuffix("TestModule") + "ProdModule"
        // This null check shouldn't ever fail since it's already been vetted above.
        checkNotNull(prodModules.find { it.nameWithoutExtension == prodModuleName })
      }
    testModulesWithMatchingProd.forEach { (testMod, prodMod) ->
      val testModuleTypes = loadProvidedTypes(testMod).toSet()
      val prodModuleTypes = loadProvidedTypes(prodMod).toSet()
      val missingTypes = prodModuleTypes - testModuleTypes
      if (missingTypes.isNotEmpty()) {
        println("Test module does not rebind expected types from its corresponding prod module:")
        missingTypes.forEach { println("- ${it.toReadableString()}") }
        println()
        println("Test module file: ${testMod.toRelativeString(repoRoot)}.")
        println("Production module file: ${prodMod.toRelativeString(repoRoot)}.")
        println()
        error("Failed to verify that all test modules replicate their prod module bindings.")
      }

      // It's okay for test modules to bind extra things, but these should be unique to the module.
      val extraTypes = testModuleTypes - prodModuleTypes
      if (extraTypes.isNotEmpty()) {
        trueProdModules.forEach { otherProdModule ->
          val nonOverlappingTypes = extraTypes - loadProvidedTypes(otherProdModule).toSet()
          if (extraTypes.size != nonOverlappingTypes.size) {
            println("Test module includes bindings from other production modules:")
            (extraTypes - nonOverlappingTypes).forEach { println("- ${it.toReadableString()}") }
            println()
            println("Test module file: ${testMod.toRelativeString(repoRoot)}.")
            println("Other production module file: ${otherProdModule.toRelativeString(repoRoot)}.")
            println()
            println("These should be moved to the correct test module.")
          }
        }
      }
    }

    println("No issues were found when analyzing Dagger modules.")
  }

  private fun loadProvidedTypes(moduleFile: File): List<ProvidedType> {
    // Sometimes modules have no bodies (which is generally the case when they are associated with
    // subcomponents).
    return loadModuleKtClass(moduleFile).body?.extractAllProvidedTypes(moduleFile) ?: emptyList()
  }

  private fun loadModuleKtClass(file: File): KtClass {
    return loadKtFile(file).children.filterIsInstance<KtClass>().find { ktClass ->
      ktClass.annotationEntries.any { it.shortName?.toString() == "Module" }
    } ?: error("Failed to extract module class from ${file.toRelativeString(repoRoot)}.")
  }

  private fun loadKtFile(file: File): KtFile {
    val virtualFile = LightVirtualFile(file.name, KotlinFileType.INSTANCE, file.readText())
    return PsiManager.getInstance(project).findFile(virtualFile) as KtFile
  }

  private fun KtClassBody.extractAllProvidedTypes(file: File): List<ProvidedType> =
    children.filterIsInstance<KtFunction>().map { it.convertToProvidedType(file) }

  private fun KtFunction.convertToProvidedType(file: File): ProvidedType {
    val annotations =
      modifierList?.annotationEntries?.mapNotNullTo(mutableSetOf()) {
        it.shortName?.toString()
      } ?: emptySet()
    check(annotations.isNotEmpty()) {
      "Expected function '$name' to have @Provides or @Binds annotation in file:" +
        " ${file.toRelativeString(repoRoot)}."
    }
    val returnTypeName = typeReference?.text
    check(hasDeclaredReturnType() && returnTypeName != null) {
      "Expected function '$name' to have a declared return type in file:" +
        " ${file.toRelativeString(repoRoot)}."
    }
    return when {
      "IntoSet" in annotations -> ProvidedType.SetType(returnTypeName)
      "IntoMap" in annotations -> {
        val stringKeyAnnotationEntry = modifierList?.annotationEntries?.find {
          it.shortName?.toString() == "StringKey"
        }
        val routeKeyAnnotationEntry = modifierList?.annotationEntries?.find {
          it.shortName?.toString() == "RouteKey"
        }
        when {
          stringKeyAnnotationEntry != null -> {
            val argumentList =
              stringKeyAnnotationEntry.children.filterIsInstance<KtValueArgumentList>()
            val keyValue = argumentList.singleOrNull()?.arguments?.singleOrNull()?.text
            check(keyValue != null && keyValue.startsWith('"') && keyValue.endsWith('"')) {
              "Expected function '$name' to have a @StringKey with a single string passed to it:" +
                " ${file.toRelativeString(repoRoot)}."
            }
            ProvidedType.StringMapType(
              keyValue.removePrefix("\"").removeSuffix("\""), returnTypeName
            )
          }
          routeKeyAnnotationEntry != null -> {
            val argumentList =
              routeKeyAnnotationEntry.children.filterIsInstance<KtValueArgumentList>()
            val keyValue = argumentList.singleOrNull()?.arguments?.singleOrNull()?.text
            check(keyValue != null) {
              "Expected function '$name' to have a @StringKey with a single string passed to it:" +
                " ${file.toRelativeString(repoRoot)}."
            }
            ProvidedType.RouteKeyMapType(keyValue, returnTypeName)
          }
          else -> {
            error(
              "Expected function '$name' to have a @StringKey or @RouteKey:" +
                " ${file.toRelativeString(repoRoot)}."
            )
          }
        }
      }
      else -> ProvidedType.ElementType(returnTypeName)
    }
  }

  private companion object {
    /** File paths for Glide modules which should not be treated as Dagger modules. */
    private val GLIDE_MODULE_EXEMPTIONS = setOf(
      "utility/src/main/java/org/oppia/android/util/parser/image/RepositoryGlideModule.kt"
    )

    /** File paths for test modules which do not require exact corresponding production modules. */
    private val TEST_MODULE_EXEMPTIONS = setOf(
      "domain/src/main/java/org/oppia/android/domain/hintsandsolution/" +
        "HintsAndSolutionConfigFastShowTestModule.kt"
    )

    private fun File.isAnnotatedAsModule(): Boolean {
      return inputStream().bufferedReader().use { reader ->
        // These are strongly assuming certain formatting rules guaranteed by the linter. This
        // avoids needing to perform a complete parsing and reading of the file.
        val lines = reader.lineSequence().takeUntil { it.isClassOrInterfaceDeclaration() }.toSet()
        return@use lines.contains("import dagger.Module") && lines.last().startsWith("@Module")
      }
    }

    private fun File.isNamedAsModule() = nameWithoutExtension.endsWith("Module")

    private fun File.isNamedAsProdModule() = nameWithoutExtension.endsWith("ProdModule")

    private fun File.isNamedAsDebugModule() = nameWithoutExtension.endsWith("DebugModule")

    private fun File.isNamedAsTestModule() = nameWithoutExtension.endsWith("TestModule")

    private fun <T> Sequence<T>.takeUntil(predicate: (T) -> Boolean): List<T> {
      val baseIterator = iterator()
      return generateSequence {
        if (!baseIterator.hasNext()) return@generateSequence null
        val nextValue = baseIterator.next()
        if (predicate(nextValue)) return@generateSequence null
        return@generateSequence nextValue
      }.toList()
    }

    private fun String.isClassOrInterfaceDeclaration() =
      startsWith("class ") || startsWith("abstract class ") || startsWith("interface ")

    private sealed class ProvidedType {
      abstract fun toReadableString(): String

      data class StringMapType(val key: String, val valueTypeName: String) : ProvidedType() {
        override fun toReadableString() = "@IntoMap @StringKey(\"$key\") $valueTypeName"
      }

      data class RouteKeyMapType(val key: String, val valueTypeName: String) : ProvidedType() {
        override fun toReadableString() = "@IntoMap @RouteKey(\"$key\") $valueTypeName"
      }

      data class SetType(val elementTypeName: String) : ProvidedType() {
        override fun toReadableString() = "@IntoSet $elementTypeName"
      }

      data class ElementType(val typeName: String) : ProvidedType() {
        override fun toReadableString() = typeName
      }
    }
  }
}
