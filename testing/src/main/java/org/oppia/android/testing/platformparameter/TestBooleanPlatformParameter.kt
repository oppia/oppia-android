package org.oppia.android.testing.platformparameter

import javax.inject.Qualifier

/**
 * Qualifier for Test Boolean Platform Parameter. Only used in tests related to Platform Parameter.
 */
@Qualifier
annotation class TestBooleanParam

/**
 * Name for the Test Boolean Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_BOOLEAN_PARAM_NAME = "test_boolean_param_name"

/**
 * Default value for the Test Boolean Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_BOOLEAN_PARAM_DEFAULT_VALUE = false

/**
 * Server value for the Test Boolean Platform Parameter. Only used in tests related to Platform Parameter.
 */
const val TEST_BOOLEAN_PARAM_SERVER_VALUE = true
