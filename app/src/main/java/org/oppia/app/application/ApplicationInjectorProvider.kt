package org.oppia.app.application

/**
 * Provides [ApplicationComponent] for [ProfileInputView].
 */
interface ApplicationInjectorProvider {
  fun getApplicationInjector(): ApplicationInjector
}
