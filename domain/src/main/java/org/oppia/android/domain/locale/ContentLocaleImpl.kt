package org.oppia.android.domain.locale

import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.util.locale.OppiaLocale

/**
 * A data class implementation of [OppiaLocale.ContentLocale].
 *
 * @property oppiaLocaleContext the [OppiaLocaleContext] corresponding to this locale
 */
data class ContentLocaleImpl(
  val oppiaLocaleContext: OppiaLocaleContext
) : OppiaLocale.ContentLocale(oppiaLocaleContext)
