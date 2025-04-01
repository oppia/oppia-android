package org.oppia.android.testing.modulebundle.utility

import dagger.Module
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for loading images using
 * Oppia's image loader utility.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(includes = [
  GcsResourceModule::class, HtmlParserEntityTypeModule::class, ImageParsingModule::class,
  TestImageLoaderModule::class
])
interface ImageLoadingBundleModule
