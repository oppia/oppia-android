package org.oppia.android.testing.junit

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.AndroidLanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.IetfBcp47LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.MacaronicLanguageId
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.domain.locale.LocaleApplicationInjectorProvider
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.util.locale.OppiaLocale
import java.util.Locale
import org.oppia.android.app.model.OppiaRegion

// TODO(#2747): Update this to not need reflection, and instead depend on the necessary app layer
//  packages directly. At the time of writing this utility, Gradle disallowed a dependency in
//  testing module on app module since the latter is an Android application rather than a library.
//  This utility also can't be moved to the app module since it depends on test-only dependencies
//  like JUnit and AndroidX test.
/**
 * JUnit rule for automatically initializing the application's locale in app layer tests. Note that
 * this is likely needed for all app layer tests that make use of activities which interact with the
 * application's Dagger graph (likely nearly all of them).
 *
 * This rule initializes the app to the same default locale that the splash activity would if it
 * failed to look up a locale (US English).
 *
 * Custom locales can defined at the class & method level using [DefineAppLanguageLocaleContext].
 */
class InitializeDefaultLocaleRule : TestRule {
  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val localeHandler = context.getAppLanguageLocaleHandler()
        val localeController = context.getLocaleController()

        val initialContext =
          description?.getDefineAppLanguageLocaleContext()?.createLocaleContext()
            ?: localeController.getLikelyDefaultAppStringLocaleContext()
        val defaultLocale = localeController.reconstituteDisplayLocale(initialContext)
        initializeLocale(localeHandler, defaultLocale)

        val oldLocale = Locale.getDefault()
        base?.evaluate()
        Locale.setDefault(oldLocale) // Restore the locale to avoid leaking cross-test.
      }
    }
  }

  private companion object {
    private val injectorProviderClass by lazy {
      Class.forName("org.oppia.android.app.translation.AppLanguageApplicationInjectorProvider")
    }
    private val injectorClass by lazy {
      Class.forName("org.oppia.android.app.translation.AppLanguageApplicationInjector")
    }
    private val handlerClass by lazy {
      Class.forName("org.oppia.android.app.translation.AppLanguageLocaleHandler")
    }
    private val displayLocaleClass by lazy {
      Class.forName("org.oppia.android.util.locale.OppiaLocale\$DisplayLocale")
    }

    private fun Context.asLocaleApplicationInjectorProvider() =
      this as? LocaleApplicationInjectorProvider

    private fun Context.getLocaleApplicationInjector() =
      asLocaleApplicationInjectorProvider()?.getLocaleApplicationInjector()

    private fun Context.getAppLanguageLocaleHandler(): Any {
      val getInjectorMethod =
        injectorProviderClass.getDeclaredMethod("getAppLanguageApplicationInjector")
      return injectorProviderClass.tryCast(this)?.let { injectorProvider ->
        val getHandlerMethod = injectorClass.getDeclaredMethod("getAppLanguageHandler")
        injectorClass.tryCast(getInjectorMethod.invoke(injectorProvider))?.let { injector ->
          handlerClass.tryCast(getHandlerMethod.invoke(injector))
        }
      } ?: error(
        "Failed to retrieve locale handler (something is misconfigured in the test application)"
      )
    }

    private fun initializeLocale(localeHandler: Any, defaultLocale: OppiaLocale.DisplayLocale) {
      val initializeLocaleMethod =
        handlerClass.getDeclaredMethod("initializeLocale", displayLocaleClass)
      initializeLocaleMethod.invoke(localeHandler, defaultLocale)
    }

    private fun Context.getLocaleController(): LocaleController {
      return checkNotNull(getLocaleApplicationInjector()?.getLocaleController()) {
        "Failed to retrieve locale controller (something is misconfigured in the test application)"
      }
    }

    private fun Description?.getDefineAppLanguageLocaleContext(): DefineAppLanguageLocaleContext? {
      return this?.getAnnotation(DefineAppLanguageLocaleContext::class.java)
        ?: this?.testClass?.getDefineAppLanguageLocaleContext()
    }

    private fun Class<*>?.getDefineAppLanguageLocaleContext(): DefineAppLanguageLocaleContext? =
      this?.getAnnotation(DefineAppLanguageLocaleContext::class.java)

    private fun DefineAppLanguageLocaleContext?.createLocaleContext(): OppiaLocaleContext? {
      return this?.let { defineContext ->
        return OppiaLocaleContext.newBuilder().apply {
          languageDefinition = LanguageSupportDefinition.newBuilder().apply {
            defineContext.getOppiaLanguage()?.let {
              language = it
            } ?: error("Invalid enum int used for language: ${defineContext.oppiaLanguageEnumId}")
            minAndroidSdkVersion = 1
            defineContext.getAppStringId()?.let {
              appStringId = it
            } ?: error("Must define app string ID either through IETF tag or macaronic ID")
            // Default the content & audio translation IDs. They aren't used for this usage mode,
            // but with English app strings this can help reduce redundant requests to recreate the
            // activity upon test startup.
            contentStringId = constructLanguageId(ietfTag = "en", combinedMacaronicId = null)
            audioTranslationId = constructLanguageId(ietfTag = "en", combinedMacaronicId = null)
          }.build()
          regionDefinition = defineContext.getRegionDefinition()
          usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
        }.build()
      }
    }

    private fun DefineAppLanguageLocaleContext.getOppiaLanguage() =
      OppiaLanguage.values().getOrNull(oppiaLanguageEnumId)

    private fun DefineAppLanguageLocaleContext.getAppStringId(): LanguageId? {
      return constructLanguageId(
        ietfTag = appStringIetfTag.tryExtractAnnotationStringConstant(),
        combinedMacaronicId = appStringMacaronicId.tryExtractAnnotationStringConstant()
      )?.toBuilder()?.apply {
        val androidLanguageId = appStringAndroidLanguageId.tryExtractAnnotationStringConstant()
        val androidRegionId = appStringAndroidRegionId.tryExtractAnnotationStringConstant()
        if (androidLanguageId != null || androidRegionId != null) {
          androidResourcesLanguageId = AndroidLanguageId.newBuilder().apply {
            androidLanguageId?.let { languageCode = it }
            androidRegionId?.let { regionCode = it }
          }.build()
        }
      }?.build()
    }

    private fun DefineAppLanguageLocaleContext.getRegionDefinition(): RegionSupportDefinition? {
      return RegionSupportDefinition.newBuilder().apply {
        getOppiaRegion()?.let { region = it }
        addAllLanguages(getOppiaRegionLanguages())
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          regionIetfTag.tryExtractAnnotationStringConstant()?.let {
            ietfRegionTag = it
          }
        }.build()
      }.build()
    }

    private fun DefineAppLanguageLocaleContext.getOppiaRegion() =
      OppiaRegion.values().getOrNull(oppiaRegionEnumId)

    private fun DefineAppLanguageLocaleContext.getOppiaRegionLanguages() =
      regionLanguageEnumIds.toList().mapNotNull { OppiaLanguage.values().getOrNull(it) }

    private fun constructLanguageId(
      ietfTag: String?,
      combinedMacaronicId: String?
    ): LanguageId? {
      return LanguageId.newBuilder().apply {
        when {
          ietfTag != null -> { // IETF takes precedence.
            ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
              ietfLanguageTag = ietfTag
            }.build()
          }
          combinedMacaronicId != null -> {
            macaronicId = MacaronicLanguageId.newBuilder().apply {
              combinedLanguageCode = combinedMacaronicId
            }.build()
          }
          else -> return null // A language ID must be defined.
        }
      }.build()
    }

    private fun String.tryExtractAnnotationStringConstant(): String? = takeIf {
      it != DefineAppLanguageLocaleContext.DEFAULT_UNDEFINED_STRING_VALUE
    }

    /**
     *  A version of [Class.cast] that simulates Kotlin's "as?" syntax, that is, returns null if the
     *  cast fails rather than throwing an exception.
     */
    private fun Class<*>.tryCast(obj: Any?): Any? =
      try { cast(obj) } catch (e: ClassCastException) { null }
  }
}
