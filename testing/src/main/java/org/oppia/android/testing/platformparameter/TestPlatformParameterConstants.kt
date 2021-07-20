package org.oppia.android.testing.platformparameter

import javax.inject.Qualifier

// These constants are only used for any tests that are related to Platform Parameter.
@Qualifier
annotation class TestStringParam

val TEST_STRING_PARAM_NAME = "test_string_param_name"
val TEST_STRING_PARAM_DEFAULT_VALUE = "test_string_param_default_value"
val TEST_STRING_PARAM_SERVER_VALUE = "test_string_param_value"

@Qualifier
annotation class TestIntegerParam

val TEST_INTEGER_PARAM_NAME = "test_integer_param_name"
val TEST_INTEGER_PARAM_DEFAULT_VALUE = 0
val TEST_INTEGER_PARAM_SERVER_VALUE = 1

@Qualifier
annotation class TestBooleanParam

val TEST_BOOLEAN_PARAM_NAME = "test_boolean_param_name"
val TEST_BOOLEAN_PARAM_DEFAULT_VALUE = false
val TEST_BOOLEAN_PARAM_SERVER_VALUE = true
