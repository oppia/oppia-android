package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the results of a single static check run.
 *
 * @property results the list of [SarifResult]s found during the run
 */
@JsonClass(generateAdapter = true)
data class SarifRun(@Json(name = "results") val results: List<SarifResult>)
