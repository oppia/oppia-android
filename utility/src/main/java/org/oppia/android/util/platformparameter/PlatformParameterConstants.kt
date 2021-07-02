package org.oppia.android.util.platformparameter

import javax.inject.Qualifier

/**
 * This file contains all the constants that are associated with individual Platform Parameters.
 * These constants are:
 *  - Qualifier Annotation
 *  - Platform Parameter Name
 *  - Platform Parameter Default Value
 */

// These constants are only used for testing purpose.
@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class TestStringParam

val TEST_STRING_PARAM_NAME = "test_string_param_name"
val TEST_STRING_PARAM_DEFAULT_VALUE = "test_string_param_default_value"
val TEST_STRING_PARAM_VALUE = "test_string_param_value"

@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class TestIntegerParam

val TEST_INTEGER_PARAM_NAME = "test_integer_param_name"
val TEST_INTEGER_PARAM_DEFAULT_VALUE = 0
val TEST_INTEGER_PARAM_VALUE = 1

@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class TestBooleanParam

val TEST_BOOLEAN_PARAM_NAME = "test_boolean_param_name"
val TEST_BOOLEAN_PARAM_DEFAULT_VALUE = false
val TEST_BOOLEAN_PARAM_VALUE = true
