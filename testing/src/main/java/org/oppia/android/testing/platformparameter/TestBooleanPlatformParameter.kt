package org.oppia.android.testing.platformparameter

import javax.inject.Qualifier

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
