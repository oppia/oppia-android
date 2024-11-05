package org.oppia.android.util.ktlint

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.oppia.android.util.ktlint.rules.KDocFormatRule

class CustomRuleSetProvider : RuleSetProvider {

  override fun get(): RuleSet = RuleSet("custom", KDocFormatRule())
}
