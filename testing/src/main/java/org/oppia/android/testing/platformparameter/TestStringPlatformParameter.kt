package org.oppia.android.testing.platformparameter

import javax.inject.Qualifier

/**
 * Qualifier for Test String Platform Parameter. Only used in tests related to Platform Parameter.
 */
@Qualifier
annotation class TestStringParam

/**
 * Name for the Test String Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_STRING_PARAM_NAME = "test_string_param_name"

/**
 * Default value for the Test String Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_STRING_PARAM_DEFAULT_VALUE = "test_string_param_default_value"

/**
 * Server value for the Test String Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_STRING_PARAM_SERVER_VALUE = "test_string_param_value"
