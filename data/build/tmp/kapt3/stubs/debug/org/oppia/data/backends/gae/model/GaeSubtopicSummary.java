package org.oppia.data.backends.gae.model;

import java.lang.System;

/**
 * Data class for Subtopic summary model
 * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/domain/topic_domain.py#L297">SubtopicSummary structure</a>
 */
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B1\u0012\n\b\u0001\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0012\b\u0001\u0010\u0005\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007J\u000b\u0010\r\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u000e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0013\u0010\u000f\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\u0006H\u00c6\u0003J5\u0010\u0010\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00032\u0012\b\u0003\u0010\u0005\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\u0006H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001R\u001b\u0010\u0005\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000b\u00a8\u0006\u0017"}, d2 = {"Lorg/oppia/data/backends/gae/model/GaeSubtopicSummary;", "", "subtopicId", "", "title", "skillIds", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V", "getSkillIds", "()Ljava/util/List;", "getSubtopicId", "()Ljava/lang/String;", "getTitle", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "data_debug"})
@com.squareup.moshi.JsonClass(generateAdapter = true)
public final class GaeSubtopicSummary {
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String subtopicId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> skillIds = null;
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSubtopicId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getSkillIds() {
        return null;
    }
    
    public GaeSubtopicSummary(@org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "id")
    java.lang.String subtopicId, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "title")
    java.lang.String title, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "skill_ids")
    java.util.List<java.lang.String> skillIds) {
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
    public final java.util.List<java.lang.String> component3() {
        return null;
    }
    
    /**
     * Data class for Subtopic summary model
     * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/domain/topic_domain.py#L297">SubtopicSummary structure</a>
     */
    @org.jetbrains.annotations.NotNull()
    public final org.oppia.data.backends.gae.model.GaeSubtopicSummary copy(@org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "id")
    java.lang.String subtopicId, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "title")
    java.lang.String title, @org.jetbrains.annotations.Nullable()
    @com.squareup.moshi.Json(name = "skill_ids")
    java.util.List<java.lang.String> skillIds) {
        return null;
    }
    
    /**
     * Data class for Subtopic summary model
     * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/domain/topic_domain.py#L297">SubtopicSummary structure</a>
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    /**
     * Data class for Subtopic summary model
     * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/domain/topic_domain.py#L297">SubtopicSummary structure</a>
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * Data class for Subtopic summary model
     * @see <a href="https://github.com/oppia/oppia/blob/b33aa9/core/domain/topic_domain.py#L297">SubtopicSummary structure</a>
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object p0) {
        return false;
    }
}