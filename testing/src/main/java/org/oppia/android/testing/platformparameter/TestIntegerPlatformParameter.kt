package org.oppia.android.testing.platformparameter

import javax.inject.Qualifier

/**
 * Qualifier for Test Integer Platform Parameter. Only used in tests related to Platform Parameter.
 */
@Qualifier
annotation class TestIntegerParam

/**
 * Name for the Test Integer Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_INTEGER_PARAM_NAME = "test_integer_param_name"

/**
 * Default value for the Test Integer Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_INTEGER_PARAM_DEFAULT_VALUE = 0

/**
 * Server value for the Test Integer Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_INTEGER_PARAM_SERVER_VALUE = 1
