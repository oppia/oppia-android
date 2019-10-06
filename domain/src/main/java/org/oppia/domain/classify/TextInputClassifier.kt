package org.oppia.domain.classify

import javax.inject.Inject

/**
 * Implementation of [InteractionClassifier] for the text input interaction. Oppia web's implementation:
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts.
 */
internal class TextInputClassifier: InteractionClassifier {
  override fun getRuleClassifier(ruleType: String): RuleClassifier? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
