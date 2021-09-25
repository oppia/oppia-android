package org.oppia.android.testing.junit

/**
 * Specifies a custom locale context to use either at the class level or method level in conjunction
 * with [InitializeDefaultLocaleRule].
 *
 * This can be useful to simulate that a specific context has already been established by a
 * conceptual "previous" activity. Note that a language must be defined, and the app string must be
 * defined either using an IETF or macaronic ID.
 *
 * @property oppiaLanguageEnumId the OppiaLanguage enum constant integer corresponding to this
 *     locale
 * @property appStringIetfTag the IETF BCP 47 language tag to use for app strings (e.g. 'pt-BR')
 * @property appStringMacaronicId the Macaronic language ID to use for app strings (e.g. 'hi-en').
 *     This will only be used if [appStringIetfTag] isn't defined.
 * @property appStringAndroidLanguageId the language ID to be used when selecting app strings from
 *     Android resources (e.g. 'pt')
 * @property appStringAndroidRegionId the region ID to be used when selecting app strings from
 *     Android resources (e.g. 'BR')
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DefineAppLanguageLocaleContext(
  val oppiaLanguageEnumId: Int,
  val appStringIetfTag: String = DEFAULT_UNDEFINED_STRING_VALUE,
  val appStringMacaronicId: String = DEFAULT_UNDEFINED_STRING_VALUE,
  val appStringAndroidLanguageId: String = DEFAULT_UNDEFINED_STRING_VALUE,
  val appStringAndroidRegionId: String = DEFAULT_UNDEFINED_STRING_VALUE,
) {
  companion object {
    /**
     * Represents the value of an undefined property for strings passed to
     * [DefineAppLanguageLocaleContext] (undefined is generally treated as absent).
     */
    const val DEFAULT_UNDEFINED_STRING_VALUE = "<undefined>"
  }
}
