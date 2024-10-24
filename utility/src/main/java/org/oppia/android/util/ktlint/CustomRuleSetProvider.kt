package org.oppia.android.util.ktlint

import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2
import org.oppia.android.util.ktlint.rules.KDocFormatRule

class CustomRuleSetProvider : RuleSetProviderV2(
  id = "kdoc-format",
  about = NO_ABOUT,
) {
  override fun getRuleProviders(): Set<RuleProvider> {
    return setOf(
      RuleProvider {
        KDocFormatRule()
      }
    )
  }
}
