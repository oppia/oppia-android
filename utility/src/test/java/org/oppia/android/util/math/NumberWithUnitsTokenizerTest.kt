package org.oppia.android.util.math

import org.junit.runner.RunWith
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.robolectric.annotation.LooperMode

/** Tests for [NumberWithUnitsTokenizer]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NumberWithUnitsTokenizerTest
