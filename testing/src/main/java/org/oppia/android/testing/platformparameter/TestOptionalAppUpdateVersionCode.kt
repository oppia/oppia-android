package org.oppia.android.testing.platformparameter

import javax.inject.Qualifier

/**
 * Qualifier for test optional app update version code. Only used in tests related to platform parameter.
 */
@Qualifier
annotation class TestOptionalAppUpdateVersionCode

/**
 * Name for the test optional app update version code. Only used in tests related to platform parameter.
 */
const val TEST_OPTIONAL_APP_UPDATE_VERSION_CODE_NAME = "test_optional_app_update_version_code_name"

/**
 * Default value for the test optional app update version code. Only used in tests related to platform parameter.
 */
const val TEST_OPTIONAL_APP_UPDATE_VERSION_CODE_DEFAULT_VALUE = 0

/**
 * Server value for the test optional app update version code. Only used in tests related to platform parameter.
 */
const val TEST_OPTIONAL_APP_UPDATE_VERSION_CODE_SERVER_VALUE = 1
