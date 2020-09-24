package org.oppia.android.app.application

<<<<<<< HEAD:app/src/main/java/org/oppia/android/app/application/ApplicationInjector.kt
import org.oppia.android.app.profile.ProfileInputView
=======
import org.oppia.app.profile.ProfileInputView
import org.oppia.util.data.DataProvidersInjector
>>>>>>> develop:app/src/main/java/org/oppia/app/application/ApplicationInjector.kt

/** Injector for application-level dependencies that can't be directly injected where needed. */
interface ApplicationInjector : DataProvidersInjector {

  // TODO(#1619): Remove post-modularization.
  fun inject(profileInputView: ProfileInputView)
}
