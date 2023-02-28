package org.oppia.android.scripts.gae.json

import org.oppia.proto.v1.structure.InteractionInstanceDto

data class TypeResolutionContext(
  var currentInteractionType: InteractionInstanceDto.InteractionTypeCase? = null,
  var currentCustomizationArgKeyName: String? = null,
  var currentRuleTypeName: String? = null,
  var currentRuleInputName: String? = null,
  var currentContentFormat: GaeTranslatableContentFormat? = null
) {
  val expectedInteractionType: InteractionInstanceDto.InteractionTypeCase
    get() {
      return checkNotNull(currentInteractionType) {
        "Expected to parse this object within an interaction."
      }
    }

  val expectedCustomizationArgKeyName: String
    get() {
      return checkNotNull(currentCustomizationArgKeyName) {
        "Expected to parse this object within a customization argument."
      }
    }

  val expectedRuleTypeName: String
    get() {
      return checkNotNull(currentRuleTypeName) {
        "Expected to parse this object within a rule spec."
      }
    }

  val expectedRuleInputName: String
    get() {
      return checkNotNull(currentRuleInputName) {
        "Expected to parse this object within a rule spec."
      }
    }

  val expectedContentFormat: GaeTranslatableContentFormat
    get() {
      return checkNotNull(currentContentFormat) {
        "Expected to parse this object within a translation context."
      }
    }
}
