package org.oppia.data.backends.gae.api;

import java.lang.System;

/**
 * Service that provides access to topic endpoints.
 */
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\'\u00a8\u0006\u0007"}, d2 = {"Lorg/oppia/data/backends/gae/api/TopicService;", "", "getTopicByName", "Lretrofit2/Call;", "Lorg/oppia/data/backends/gae/model/GaeTopic;", "topicName", "", "data_debug"})
public abstract interface TopicService {
    
    @org.jetbrains.annotations.NotNull()
    @retrofit2.http.GET(value = "topic_data_handler/{topic_name}")
    public abstract retrofit2.Call<org.oppia.data.backends.gae.model.GaeTopic> getTopicByName(@org.jetbrains.annotations.NotNull()
    @retrofit2.http.Path(value = "topic_name")
    java.lang.String topicName);
}