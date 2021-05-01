package org.oppia.android.app.topic

import javax.inject.Qualifier

/**
 * Corresponds to an injectable boolean indicating whether practice sessions are enabled. When this
 * is false, the practice sessions tab itself will not be loaded.
 */
@Qualifier annotation class EnablePracticeTab
