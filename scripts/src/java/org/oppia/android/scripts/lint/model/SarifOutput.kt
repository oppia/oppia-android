package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a static check's complete output as represented by SARIF
 * (https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html).
 *
 * @property runs the list of [SarifRun]s found during by the check suite
 */
@JsonClass(generateAdapter = true)
data class SarifOutput(@Json(name = "runs") val runs: List<SarifRun>)
