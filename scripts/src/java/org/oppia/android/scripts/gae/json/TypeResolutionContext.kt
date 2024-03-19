package org.oppia.android.scripts.gae.json

import org.oppia.proto.v1.structure.InteractionInstanceDto

class TypeResolutionContext {
  private val currentInteractionTypeStore =
    createThreadLocal<InteractionInstanceDto.InteractionTypeCase>()
  private val currentCustomizationArgKeyNameStore = createThreadLocal<String>()
  private val currentRuleTypeNameStore = createThreadLocal<String>()
  private val currentRuleInputNameStore = createThreadLocal<String>()
  private val currentContentFormatStore = createThreadLocal<GaeTranslatableContentFormat>()

  var currentInteractionType: InteractionInstanceDto.InteractionTypeCase?
    get() = currentInteractionTypeStore.get()
    set(value) = currentInteractionTypeStore.set(value)

  var currentCustomizationArgKeyName: String?
    get() = currentCustomizationArgKeyNameStore.get()
    set(value) = currentCustomizationArgKeyNameStore.set(value)

  var currentRuleTypeName: String?
    get() = currentRuleTypeNameStore.get()
    set(value) = currentRuleTypeNameStore.set(value)

  var currentRuleInputName: String?
    get() = currentRuleInputNameStore.get()
    set(value) = currentRuleInputNameStore.set(value)

  var currentContentFormat: GaeTranslatableContentFormat?
    get() = currentContentFormatStore.get()
    set(value) = currentContentFormatStore.set(value)

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

  private companion object {
    private fun <T: Any> createThreadLocal(defaultValue: T? = null): ThreadLocal<T?> =
      ThreadLocal.withInitial { defaultValue }
  }
}
