package org.oppia.android.scripts.codegen

import com.google.protobuf.Message
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.oppia.android.app.model.FeatureFlagDefinition
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.PlatformParameterDefinition
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SupportedFeatureFlags
import org.oppia.android.app.model.SupportedPlatformParameters
import org.oppia.android.app.model.SyncStatus
import java.io.File
import java.io.PrintStream
import kotlin.reflect.KClass

/*
  bazel run //scripts:generate_platform_config_module --
    org.oppia.android.domain.platformparameter.PlatformParameterModule
    $(pwd)/PlatformParameterModule.kt
    config/src/java/org/oppia/android/config/platform/platform_parameters.pb
    config/src/java/org/oppia/android/config/platform/feature_flags.pb
 */

private const val USAGE_ERROR_MESSAGE =
  "Usage: bazel run //scripts:generate_platform_config_module --" +
    " <qualified.module.class.name> </output/file/Path.kt>" +
    " <path/to/supported_platform_parameters.pb> <path/to/supported_feature_flags.pb>"

fun main(vararg args: String) {
  check(args.size == 4) { USAGE_ERROR_MESSAGE }
  val qualifiedModuleClassName = args[0]
  val outputFilePath = args[1]
  val supportedPlatformParametersProto = File(args[2]).absoluteFile.normalize().also {
    check(it.exists()) { "Expected pb file to exist: ${args[2]}." }
  }
  val supportedFeatureFlagsProto = File(args[3]).absoluteFile.normalize().also {
    check(it.exists()) { "Expected pb file to exist: ${args[3]}." }
  }
  val supportedPlatformParameters =
    loadProto(supportedPlatformParametersProto, SupportedPlatformParameters.getDefaultInstance())
  val supportedFeatureFlags =
    loadProto(supportedFeatureFlagsProto, SupportedFeatureFlags.getDefaultInstance())
  PrintStream(outputFilePath).use {
    GeneratePlatformConfigModule(it)
      .generateModule(qualifiedModuleClassName, supportedPlatformParameters, supportedFeatureFlags)
  }
}

class GeneratePlatformConfigModule(private val output: PrintStream) {
  fun generateModule(
    qualifiedModuleClassName: String,
    supportedPlatformParameters: SupportedPlatformParameters,
    supportedFeatureFlags: SupportedFeatureFlags
  ) {
    val modulePackage = qualifiedModuleClassName.substringBeforeLast('.')
    val moduleName = ClassName(modulePackage, qualifiedModuleClassName.substringAfterLast('.'))

    val platformParameterProviders =
      supportedPlatformParameters.platformParameterDefinitionList.map { generateValueProvider(it) }
    val featureFlagProviders =
      supportedFeatureFlags.featureFlagDefinitionList.map { generateValueProvider(it) }
    val featureFlagStatusProviders =
      supportedFeatureFlags.featureFlagDefinitionList.map { generateStatusProvider(it) }
    val moduleClassType = TypeSpec.classBuilder(moduleName).apply {
      addAnnotation(MODULE_CLASS)
      addFunctions(platformParameterProviders)
      addFunctions(featureFlagProviders)
      addFunctions(featureFlagStatusProviders)
    }.build()
    FileSpec.builder(modulePackage, moduleName.simpleName).apply {
      addType(moduleClassType)
    }.build().writeTo(output)
  }

  private companion object {
    private val MODULE_CLASS = ClassName("dagger", "Module")
    private val PROVIDES_CLASS = ClassName("dagger", "Provides")
    private val INTO_MAP_CLASS = ClassName("dagger.multibindings", "IntoMap")
    private val PLATFORM_PARAMETER_CLASS =
      ClassName("org.oppia.android.domain.platformparameter", "PlatformParameter")
    private val FEATURE_FLAG_CLASS =
      ClassName("org.oppia.android.domain.platformparameter", "FeatureFlag")
    private val FEATURE_FLAG_ID_KEY_CLASS =
      ClassName("org.oppia.android.domain.platformparameter", "FeatureFlagIdKey")
    private val FEATURE_FLAG_SYNC_STATUSES_CLASS =
      ClassName("org.oppia.android.domain.platformparameter", "FeatureFlagSyncStatuses")
    private val PLATFORM_PARAMETER_ID_CLASS = PlatformParameterId::class.asClassName()
    private val FEATURE_FLAG_ID_CLASS = FeatureFlagId::class.asClassName()
    private val PLATFORM_PARAMETER_PROCESS_STATE_CLASS =
      ClassName("org.oppia.android.domain.platformparameter", "PlatformParameterProcessState")

    private fun generateValueProvider(definition: PlatformParameterDefinition): FunSpec {
      val paramName = definition.id.name.upperSnakeToUpperCamelCase()
      val member = MemberName(PLATFORM_PARAMETER_ID_CLASS, definition.id.name)
      return FunSpec.builder("provide${paramName}Value").apply {
        addAnnotation(PROVIDES_CLASS)
        addAnnotation(
          AnnotationSpec.builder(PLATFORM_PARAMETER_CLASS).addMember("%M", member).build()
        )
        addParameter(
          ParameterSpec.builder("processState", PLATFORM_PARAMETER_PROCESS_STATE_CLASS).build()
        )
        returns(definition.defaultValue.asKClassType())
        val retrieveFuncName = definition.defaultValue.computeRetrieveFunctionName()
        addStatement("return processState.$retrieveFuncName(%M)", member)
      }.build()
    }

    private fun generateValueProvider(definition: FeatureFlagDefinition): FunSpec {
      val paramName = definition.id.name.upperSnakeToUpperCamelCase()
      val member = MemberName(FEATURE_FLAG_ID_CLASS, definition.id.name)
      return FunSpec.builder("provide${paramName}Value").apply {
        addAnnotation(PROVIDES_CLASS)
        addAnnotation(AnnotationSpec.builder(FEATURE_FLAG_CLASS).addMember("%M", member).build())
        addParameter(
          ParameterSpec.builder("processState", PLATFORM_PARAMETER_PROCESS_STATE_CLASS).build()
        )
        returns(Boolean::class)
        addStatement("return processState.retrieveFeatureFlagState(%M)", member)
      }.build()
    }

    private fun generateStatusProvider(definition: FeatureFlagDefinition): FunSpec {
      val paramName = definition.id.name.upperSnakeToUpperCamelCase()
      val member = MemberName(FEATURE_FLAG_ID_CLASS, definition.id.name)
      return FunSpec.builder("provide${paramName}Status").apply {
        addAnnotation(PROVIDES_CLASS)
        addAnnotation(INTO_MAP_CLASS)
        addAnnotation(FEATURE_FLAG_SYNC_STATUSES_CLASS)
        addAnnotation(
          AnnotationSpec.builder(FEATURE_FLAG_ID_KEY_CLASS).addMember("%M", member).build()
        )
        addParameter(
          ParameterSpec.builder("processState", PLATFORM_PARAMETER_PROCESS_STATE_CLASS).build()
        )
        returns(SyncStatus::class)
        addStatement("return processState.retrieveFeatureFlagSyncStatus(%M)", member)
      }.build()
    }

    private fun PlatformParameterValue.asKClassType(): KClass<*> {
      return when (valueTypeCase) {
        PlatformParameterValue.ValueTypeCase.BOOLEAN -> Boolean::class
        PlatformParameterValue.ValueTypeCase.INTEGER -> Int::class
        PlatformParameterValue.ValueTypeCase.STRING -> String::class
        PlatformParameterValue.ValueTypeCase.VALUETYPE_NOT_SET, null ->
          error("Invalid value type: $valueTypeCase.")
      }
    }

    private fun PlatformParameterValue.computeRetrieveFunctionName(): String {
      return when (valueTypeCase) {
        PlatformParameterValue.ValueTypeCase.BOOLEAN -> "retrievePlatformParameterBooleanState"
        PlatformParameterValue.ValueTypeCase.INTEGER -> "retrievePlatformParameterIntegerState"
        PlatformParameterValue.ValueTypeCase.STRING -> "retrievePlatformParameterStringState"
        PlatformParameterValue.ValueTypeCase.VALUETYPE_NOT_SET, null ->
          error("Invalid value type: $valueTypeCase.")
      }
    }

    private fun String.upperSnakeToUpperCamelCase(): String =
      lowercase().split('_').joinToString(separator = "") { it.replaceFirstChar(Char::titlecase) }
  }
}

private inline fun <reified T : Message> loadProto(file: File, baseMessage: T): T =
  file.inputStream().use { baseMessage.newBuilderForType().mergeFrom(it).build() } as T
