package com.example.myapplication.backend.model;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonClass;

@JsonClass(generateAdapter = true)
data class TopicSummaryHandler(

/*
 * Ignore below params
 * is_admin, iframed, is_moderator,
 * is_super_admin, state_classifier_mapping,
 * preferred_audio_language_code, can_edit,
 * is_topic_manager, additional_angular_modules
 * auto_tts_enabled
 */

@Json(name = "topic_summary_dicts") val topic_summary_dicts: List<TopicSummary>?

)
