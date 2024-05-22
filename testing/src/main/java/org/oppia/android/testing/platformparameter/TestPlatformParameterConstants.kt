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
 * Default value for the test string platform parameter. Only used in tests related to platform
 * parameter.
 */
const val TEST_STRING_PARAM_DEFAULT_VALUE = "test_string_param_default_value"

/**
 * Server value for the test string platform parameter. Only used in tests related to platform
 * parameter.
 */
const val TEST_STRING_PARAM_SERVER_VALUE = "test_string_param_value"

/**
 * Qualifier for test boolean platform parameter. Only used in tests related to platform parameter.
 */
@Qualifier
annotation class TestBooleanParam

/**
 * Name for the test boolean platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_BOOLEAN_PARAM_NAME = "test_boolean_param_name"

/**
 * Default value for the test boolean platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_BOOLEAN_PARAM_DEFAULT_VALUE = false

/**
 * Server value for the test boolean platform parameter. Only used in tests related to platform parameter.
 */
const val TEST_BOOLEAN_PARAM_SERVER_VALUE = true

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
