package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.gae.json.GaeCustomizationArgValue.GaeImageWithRegions.GaeLabeledRegion.GaeNormalizedRectangle2d

object MoshiFactory {
  fun createMoshi(): Moshi {
    return Moshi.Builder().apply {
      val typeResolutionContext = TypeResolutionContext()
      add(AndroidActivityRequests.Adapter())
      add(AndroidActivityRequests.ActivityRequest.Adapter())
      add(GaeCustomizationArgValue.Adapter(typeResolutionContext))
      add(GaeNormalizedRectangle2d.Adapter())
      add(GaeInteractionInstance.Adapter(typeResolutionContext))
      add(GaeInteractionObject.Adapter(typeResolutionContext))
      add(GaeInteractionObject.TranslatableHtmlContentId.Adapter())
      add(GaeInteractionObject.SetOfXlatableContentIds.Adapter())
      add(GaeInteractionObject.SetsOfXlatableContentIds.Adapter())
      add(GaeInteractionObject.RatioExpression.Adapter())
      add(GaeParamCustomizationArgs.Adapter())
      add(GaeRuleSpec.Adapter(typeResolutionContext))
      add(GaeWrittenTranslation.Adapter(typeResolutionContext))
      add(GaeWrittenTranslation.Translation.Adapter(typeResolutionContext))
      add(GaeTranslatedContent.Adapter(typeResolutionContext))
      add(GaeTranslatedContent.Translation.Adapter(typeResolutionContext))
      add(GaeTranslatableContentFormat.Adapter())
      add(GaeInteractionCustomizationArgsMap.Adapter(typeResolutionContext))
      add(GaeEntityTranslations.Adapter)
      add(KotlinJsonAdapterFactory()) // TODO: Remove this so that it can be done w/o reflection.
    }.build()
  }
}
