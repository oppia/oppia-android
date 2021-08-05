package org.oppia.android.testing.platformparameter

import javax.inject.Qualifier

/**
 * Qualifier for test string platform parameter. Only used in tests related to platform parameter.
 */
@Qualifier
annotation class TestStringParam

/**
 * Name for the test string platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_STRING_PARAM_NAME = "test_string_param_name"

/**
 * Default value for the test string platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_STRING_PARAM_DEFAULT_VALUE = "test_string_param_default_value"

/**
 * Server value for the test string platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_STRING_PARAM_SERVER_VALUE = "test_string_param_value"
