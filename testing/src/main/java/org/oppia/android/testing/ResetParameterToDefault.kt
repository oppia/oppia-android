package org.oppia.android.testing

@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class ResetParameterToDefault(val name: String)
