package org.oppia.testing

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import java.lang.reflect.Field

// TODO(#59): Remove this runner once application classes can be specified per-test suite.
/**
 * Custom test runner for Oppia's AndroidX tests to facilitate loading custom Dagger applications in
 * a way that's interoperable with Espresso.
 *
 * Loosely based on https://stackoverflow.com/a/42541784 and https://stackoverflow.com/a/36778841.
 *
 * This runner will load the default OppiaApplication that the production app uses unless the test
 * declares a Robolectric @Config annotation specifying the test class that should be used, instead.
 * This allows tests to declare a test application class to be used in both Espresso & Robolectric
 * contexts with the same code declaration.
 */
@Suppress("unused") // This class is used directly by Gradle during instrumentation test setup.
class OppiaTestRunner: AndroidJUnitRunner() {
  private lateinit var applicationClassLoader: ClassLoader
  private lateinit var application: Application

  override fun onCreate(arguments: Bundle?) {
    // Load a new application if it's different than the original.
    val bindApplication = retrieveTestApplicationName(arguments?.getString("class"))?.let {
      newApplication(applicationClassLoader, it, targetContext)
    } ?: targetContext.applicationContext as Application

    // Ensure the bound application is forcibly overwritten in the target context, and used
    // subsequently throughout the runner since it's replacing the previous application.
    overrideApplicationInContext(targetContext, bindApplication)
    application = bindApplication

    super.onCreate(arguments)
  }

  override fun callApplicationOnCreate(app: Application?) {
    // Use the overridden application, instead.
    super.callApplicationOnCreate(application)
  }

  override fun newApplication(
    cl: ClassLoader?,
    className: String?,
    context: Context?
  ): Application {
    applicationClassLoader = checkNotNull(cl) {
      "Expected non-null class loader to be passed to newApplication"
    }
    return super.newApplication(cl, className, context)
  }

  @Suppress("UNCHECKED_CAST")
  private fun retrieveTestApplicationName(className: String?): String? {
    val testClassName = className?.substringBefore('#')
    val classLoader = OppiaTestRunner::class.java.classLoader!!
    val testClass = classLoader.loadClass(testClassName)
    val configClass =
      classLoader.loadClass("org.robolectric.annotation.Config") as Class<Annotation>
    val configAnnotation = testClass.getAnnotation(configClass)
    // Only consider overriding the application if it's defined via a Robolectric configuration (to
    // have parity with the Robolectric version of the test).
    if (configAnnotation != null) {
      val applicationMethod = configClass.getDeclaredMethod("application")
      val applicationClass = applicationMethod.invoke(configAnnotation) as Class<*>
      val defaultApplicationClass =
        classLoader.loadClass("org.robolectric.annotation.DefaultApplication")
      if (!defaultApplicationClass.isAssignableFrom(applicationClass)) {
        // Only consider taking the test application if it's been defined, otherwise use the default
        // for Espresso.
        return applicationClass.name
      }
    }
    return null
  }

  private fun overrideApplicationInContext(context: Context, application: Application) {
    val packageInfo = getPrivateFieldFromObject(context, "mPackageInfo")
    setPrivateFieldFromObject(packageInfo, "mApplication", application)
  }

  private fun getPrivateFieldFromObject(container: Any, fieldName: String): Any {
    return retrieveAccessibleFieldFromObject(container, fieldName).get(container)
  }

  private fun setPrivateFieldFromObject(container: Any, fieldName: String, newValue: Any) {
    retrieveAccessibleFieldFromObject(container, fieldName).set(container, newValue)
  }

  private fun retrieveAccessibleFieldFromObject(container: Any, fieldName: String): Field {
    return container.javaClass.getDeclaredField(fieldName).apply {
      isAccessible = true
    }
  }
}
