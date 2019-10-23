package org.oppia.util.parser

import javax.inject.Qualifier

// These 3 annotations provides dependencies for image-extraction URL.
/** Corresponds to the default GCS Resource Bucket Name. */
@Qualifier annotation class DefaultGcsResource

/** Corresponds to the default Gcs Prefix. */
@Qualifier annotation class DefaultGcsPrefix

/** Corresponds to the default Image Download Url Template. */
@Qualifier annotation class ImageDownloadUrlTemplate
