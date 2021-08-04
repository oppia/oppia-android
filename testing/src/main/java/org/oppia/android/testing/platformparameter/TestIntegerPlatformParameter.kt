package org.oppia.android.testing.platformparameter

import javax.inject.Qualifier

/**
 * Qualifier for test integer platform parameter. Only used in tests related to platform parameter.
 */
@Qualifier
annotation class TestIntegerParam

/**
 * Name for the test integer platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_INTEGER_PARAM_NAME = "test_integer_param_name"

/**
 * Default value for the test integer platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_INTEGER_PARAM_DEFAULT_VALUE = 0

/**
 * Server value for the test integer platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_INTEGER_PARAM_SERVER_VALUE = 1
