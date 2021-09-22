package org.oppia.android.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.util.Locale
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.oppia.android.app.translation.AppLanguageApplicationInjectorProvider
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.domain.locale.LocaleApplicationInjectorProvider
import org.oppia.android.domain.locale.LocaleController

class InitializeDefaultLocaleRule: TestRule {
  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val localeHandler = context.getAppLanguageLocaleHandler()
        val localeController = context.getLocaleController()
        val defaultContext = localeController.getLikelyDefaultAppStringLocaleContext()
        val defaultLocale = localeController.reconstituteDisplayLocale(defaultContext)
        localeHandler.initializeLocale(defaultLocale)

        val oldLocale = Locale.getDefault()
        base?.evaluate()
        Locale.setDefault(oldLocale) // Restore the locale to avoid leaking cross-test.
      }
    }
  }

  private companion object {
    private fun Context.asAppLanguageApplicationInjectorProvider() =
      this as? AppLanguageApplicationInjectorProvider

    private fun Context.asLocaleApplicationInjectorProvider() =
      this as? LocaleApplicationInjectorProvider

    private fun Context.getLocaleApplicationInjector() =
      asLocaleApplicationInjectorProvider()?.getLocaleApplicationInjector()

    private fun Context.getAppLanguageApplicationInjector() =
      asAppLanguageApplicationInjectorProvider()?.getAppLanguageApplicationInjector()

    private fun Context.getAppLanguageLocaleHandler(): AppLanguageLocaleHandler {
      return checkNotNull(getAppLanguageApplicationInjector()?.getAppLanguageHandler()) {
        "Failed to retrieve locale handler (something is misconfigured in the test application)"
      }
    }

    private fun Context.getLocaleController(): LocaleController {
      return checkNotNull(getLocaleApplicationInjector()?.getLocaleController()) {
        "Failed to retrieve locale cotnroller (something is misconfigured in the test application)"
      }
    }
  }
}
