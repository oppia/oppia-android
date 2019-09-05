package org.oppia.data.backends.gae.model;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u0006\n\u0002\b\u001e\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B}\u0012\n\b\u0001\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\f\u001a\u0004\u0018\u00010\r\u0012\n\b\u0001\u0010\u000e\u001a\u0004\u0018\u00010\r\u00a2\u0006\u0002\u0010\u000fJ\u0010\u0010\u001f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010 \u001a\u0004\u0018\u00010\rH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0019J\u000b\u0010!\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\"\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010#\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010$\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010%\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010&\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010\'\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010(\u001a\u0004\u0018\u00010\rH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0019J\u0086\u0001\u0010)\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\r2\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\rH\u00c6\u0001\u00a2\u0006\u0002\u0010*J\u0013\u0010+\u001a\u00020,2\b\u0010-\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010.\u001a\u00020\u0003H\u00d6\u0001J\t\u0010/\u001a\u00020\u0005H\u00d6\u0001R\u0015\u0010\n\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u0010\u0010\u0011R\u0015\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u0013\u0010\u0011R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u0015\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u0017\u0010\u0011R\u0015\u0010\u000e\u001a\u0004\u0018\u00010\r\u00a2\u0006\n\n\u0002\u0010\u001a\u001a\u0004\b\u0018\u0010\u0019R\u0015\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\n\n\u0002\u0010\u001a\u001a\u0004\b\u001b\u0010\u0019R\u0015\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u001c\u0010\u0011R\u0015\u0010\t\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u001d\u0010\u0011R\u0015\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u001e\u0010\u0011\u00a8\u00060"}, d2 = {"Lorg/oppia/data/backends/gae/model/GaeTopicSummarytDict;", "", "version", "", "id", "", "name", "subtopic_count", "canonical_story_count", "uncategorized_skill_count", "additional_story_count", "total_skill_count", "topic_model_last_updated", "", "topic_model_created_on", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Double;Ljava/lang/Double;)V", "getAdditional_story_count", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getCanonical_story_count", "getId", "()Ljava/lang/String;", "getName", "getSubtopic_count", "getTopic_model_created_on", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getTopic_model_last_updated", "getTotal_skill_count", "getUncategorized_skill_count", "getVersion", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Double;Ljava/lang/Double;)Lorg/oppia/data/backends/gae/model/GaeTopicSummarytDict;", "equals", "", "other", "hashCode", "toString", "data_debug"})
@com.squareup.moshi.JsonClass(generateAdapter = true)
public final class GaeTopicSummarytDict {
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer version = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer subtopic_count = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer canonical_story_count = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer uncategorized_skill_count = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer additional_story_count = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer total_skill_count = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double topic_model_last_updated = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double topic_model_created_on = null;
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getVersion() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getSubtopic_count() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getCanonical_story_count() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getUncategorized_skill_count() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getAdditional_story_count() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getTotal_skill_count() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getTopic_model_last_updated() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getTopic_model_created_on() {
        return null;
    }
    
    public GaeTopicSummarytDict(@org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "version")
    java.lang.Integer version, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "id")
    java.lang.String id, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "name")
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "subtopic_count")
    java.lang.Integer subtopic_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "canonical_story_count")
    java.lang.Integer canonical_story_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "uncategorized_skill_count")
    java.lang.Integer uncategorized_skill_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "additional_story_count")
    java.lang.Integer additional_story_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "total_skill_count")
    java.lang.Integer total_skill_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "topic_model_last_updated")
    java.lang.Double topic_model_last_updated, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "topic_model_created_on")
    java.lang.Double topic_model_created_on) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component9() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final org.oppia.data.backends.gae.model.GaeTopicSummarytDict copy(@org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "version")
    java.lang.Integer version, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "id")
    java.lang.String id, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "name")
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "subtopic_count")
    java.lang.Integer subtopic_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "canonical_story_count")
    java.lang.Integer canonical_story_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "uncategorized_skill_count")
    java.lang.Integer uncategorized_skill_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "additional_story_count")
    java.lang.Integer additional_story_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "total_skill_count")
    java.lang.Integer total_skill_count, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "topic_model_last_updated")
    java.lang.Double topic_model_last_updated, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "topic_model_created_on")
    java.lang.Double topic_model_created_on) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object p0) {
        return false;
    }
}