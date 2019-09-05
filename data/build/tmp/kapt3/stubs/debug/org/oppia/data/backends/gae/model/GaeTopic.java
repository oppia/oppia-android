package org.oppia.data.backends.gae.model;

import java.lang.System;

/**
 * Data class for Topic model containing full information
 * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/controllers/topic_viewer.py#L45">Topic structure</a>
 */
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0016\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u00a1\u0001\u0012\n\b\u0001\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0012\b\u0001\u0010\u0005\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0018\u00010\u0006\u0012\u0012\b\u0001\u0010\b\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0018\u00010\u0006\u0012\u0018\b\u0001\u0010\t\u001a\u0012\u0012\u0004\u0012\u00020\u0003\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\n\u0012\u0018\b\u0001\u0010\u000b\u001a\u0012\u0012\u0004\u0012\u00020\u0003\u0012\u0006\u0012\u0004\u0018\u00010\f\u0018\u00010\n\u0012\u0012\b\u0001\u0010\r\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\u0006\u0012\u0012\b\u0001\u0010\u000e\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u000f\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0010J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0013\u0010\u001e\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0018\u00010\u0006H\u00c6\u0003J\u0013\u0010\u001f\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0018\u00010\u0006H\u00c6\u0003J\u0019\u0010 \u001a\u0012\u0012\u0004\u0012\u00020\u0003\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\nH\u00c6\u0003J\u0019\u0010!\u001a\u0012\u0012\u0004\u0012\u00020\u0003\u0012\u0006\u0012\u0004\u0018\u00010\f\u0018\u00010\nH\u00c6\u0003J\u0013\u0010\"\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\u0006H\u00c6\u0003J\u0013\u0010#\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u000f\u0018\u00010\u0006H\u00c6\u0003J\u00a5\u0001\u0010$\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00032\u0012\b\u0003\u0010\u0005\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0018\u00010\u00062\u0012\b\u0003\u0010\b\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0018\u00010\u00062\u0018\b\u0003\u0010\t\u001a\u0012\u0012\u0004\u0012\u00020\u0003\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\n2\u0018\b\u0003\u0010\u000b\u001a\u0012\u0012\u0004\u0012\u00020\u0003\u0012\u0006\u0012\u0004\u0018\u00010\f\u0018\u00010\n2\u0012\b\u0003\u0010\r\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\u00062\u0012\b\u0003\u0010\u000e\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u000f\u0018\u00010\u0006H\u00c6\u0001J\u0013\u0010%\u001a\u00020&2\b\u0010\'\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010(\u001a\u00020)H\u00d6\u0001J\t\u0010*\u001a\u00020\u0003H\u00d6\u0001R\u001b\u0010\b\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u001b\u0010\u0005\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R!\u0010\u000b\u001a\u0012\u0012\u0004\u0012\u00020\u0003\u0012\u0006\u0012\u0004\u0018\u00010\f\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R!\u0010\t\u001a\u0012\u0012\u0004\u0012\u00020\u0003\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u001b\u0010\u000e\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u000f\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0012R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u001b\u0010\r\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0012\u00a8\u0006+"}, d2 = {"Lorg/oppia/data/backends/gae/model/GaeTopic;", "", "topicId", "", "topicName", "canonicalStoryDicts", "", "Lorg/oppia/data/backends/gae/model/GaeStorySummary;", "additionalStoryDicts", "skillDescriptions", "", "degreesOfMastery", "", "uncategorizedSkillIds", "subtopics", "Lorg/oppia/data/backends/gae/model/GaeSubtopicSummary;", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/util/Map;Ljava/util/Map;Ljava/util/List;Ljava/util/List;)V", "getAdditionalStoryDicts", "()Ljava/util/List;", "getCanonicalStoryDicts", "getDegreesOfMastery", "()Ljava/util/Map;", "getSkillDescriptions", "getSubtopics", "getTopicId", "()Ljava/lang/String;", "getTopicName", "getUncategorizedSkillIds", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "", "other", "hashCode", "", "toString", "data_debug"})
@com.squareup.moshi.JsonClass(generateAdapter = true)
public final class GaeTopic {
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String topicId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String topicName = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> canonicalStoryDicts = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> additionalStoryDicts = null;
    
    /**
     * A map of skill descriptions keyed by skill ID.
     */
    @org.jetbrains.annotations.Nullable()
    private final java.util.Map<java.lang.String, java.lang.String> skillDescriptions = null;
    
    /**
     * A map of degree masteries keyed by skill ID.
     */
    @org.jetbrains.annotations.Nullable()
    private final java.util.Map<java.lang.String, java.lang.Float> degreesOfMastery = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> uncategorizedSkillIds = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<org.oppia.data.backends.gae.model.GaeSubtopicSummary> subtopics = null;
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTopicId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTopicName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> getCanonicalStoryDicts() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> getAdditionalStoryDicts() {
        return null;
    }
    
    /**
     * A map of skill descriptions keyed by skill ID.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.util.Map<java.lang.String, java.lang.String> getSkillDescriptions() {
        return null;
    }
    
    /**
     * A map of degree masteries keyed by skill ID.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.util.Map<java.lang.String, java.lang.Float> getDegreesOfMastery() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getUncategorizedSkillIds() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<org.oppia.data.backends.gae.model.GaeSubtopicSummary> getSubtopics() {
        return null;
    }
    
    public GaeTopic(@org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "topic_id")
    java.lang.String topicId, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "topic_name")
    java.lang.String topicName, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "canonical_story_dicts")
    java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> canonicalStoryDicts, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "additional_story_dicts")
    java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> additionalStoryDicts, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "skill_descriptions")
    java.util.Map<java.lang.String, java.lang.String> skillDescriptions, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "degrees_of_mastery")
    java.util.Map<java.lang.String, java.lang.Float> degreesOfMastery, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "uncategorized_skill_ids")
    java.util.List<java.lang.String> uncategorizedSkillIds, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "subtopics")
    java.util.List<org.oppia.data.backends.gae.model.GaeSubtopicSummary> subtopics) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> component4() {
        return null;
    }
    
    /**
     * A map of skill descriptions keyed by skill ID.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.util.Map<java.lang.String, java.lang.String> component5() {
        return null;
    }
    
    /**
     * A map of degree masteries keyed by skill ID.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.util.Map<java.lang.String, java.lang.Float> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<org.oppia.data.backends.gae.model.GaeSubtopicSummary> component8() {
        return null;
    }
    
    /**
     * Data class for Topic model containing full information
     * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/controllers/topic_viewer.py#L45">Topic structure</a>
     */
    @org.jetbrains.annotations.NotNull()
    public final org.oppia.data.backends.gae.model.GaeTopic copy(@org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "topic_id")
    java.lang.String topicId, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "topic_name")
    java.lang.String topicName, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "canonical_story_dicts")
    java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> canonicalStoryDicts, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "additional_story_dicts")
    java.util.List<org.oppia.data.backends.gae.model.GaeStorySummary> additionalStoryDicts, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "skill_descriptions")
    java.util.Map<java.lang.String, java.lang.String> skillDescriptions, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "degrees_of_mastery")
    java.util.Map<java.lang.String, java.lang.Float> degreesOfMastery, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "uncategorized_skill_ids")
    java.util.List<java.lang.String> uncategorizedSkillIds, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "subtopics")
    java.util.List<org.oppia.data.backends.gae.model.GaeSubtopicSummary> subtopics) {
        return null;
    }
    
    /**
     * Data class for Topic model containing full information
     * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/controllers/topic_viewer.py#L45">Topic structure</a>
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    /**
     * Data class for Topic model containing full information
     * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/controllers/topic_viewer.py#L45">Topic structure</a>
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * Data class for Topic model containing full information
     * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/controllers/topic_viewer.py#L45">Topic structure</a>
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object p0) {
        return false;
    }
}