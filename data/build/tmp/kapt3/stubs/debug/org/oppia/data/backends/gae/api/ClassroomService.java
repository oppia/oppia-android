package org.oppia.data.backends.gae.api;

import java.lang.System;

/**
 * Service that provides access to classroom endpoints.
 */
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\'\u00a8\u0006\u0007"}, d2 = {"Lorg/oppia/data/backends/gae/api/ClassroomService;", "", "getClassroomTopicSummaryDicts", "Lretrofit2/Call;", "Lorg/oppia/data/backends/gae/model/GaeClassroom;", "classRoomName", "", "data_debug"})
public abstract interface ClassroomService {
    
    @org.jetbrains.annotations.NotNull()
    @retrofit2.http.GET(value = "classroom_data_handler/{classroom_name}")
    public abstract retrofit2.Call<org.oppia.data.backends.gae.model.GaeClassroom> getClassroomTopicSummaryDicts(@org.jetbrains.annotations.NotNull()
    @retrofit2.http.Path(value = "classroom_name")
    java.lang.String classRoomName);
}