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

/**
 * Feature flag sync status name for the test string platform parameter. This name helps retrieve
 * the sync status of the [TestStringParam]. Only used in tests related to platform parameter.
 */
const val FLAG_TEST_STRING_PARAM_IS_SERVER_PROVIDED =
  "flag_test_string_param_name_is_server_provided"

/**
 * Default value for the feature flag sync status tracker for the [TestStringParam]. Only used in
 * tests related to platform parameter.
 */
const val FLAG_TEST_STRING_PARAM_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/**
 * Server value for the test string platform parameter. Only used in tests related to platform
 * parameter.
 */
const val TEST_STRING_PARAM_SYNC_STATUS_FLAG_SERVER_VALUE = true
