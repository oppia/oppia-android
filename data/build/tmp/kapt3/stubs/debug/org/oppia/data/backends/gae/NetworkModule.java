package org.oppia.data.backends.gae;

import java.lang.System;

/**
 * Module which provides all required dependencies about network
 */
@kotlin.Suppress(names = {"unused"})
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001:\u0001\nB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\b\u0010\u0007\u001a\u00020\u0006H\u0007J\u0012\u0010\b\u001a\u00020\t2\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u000b"}, d2 = {"Lorg/oppia/data/backends/gae/NetworkModule;", "", "()V", "provideClassroomService", "Lorg/oppia/data/backends/gae/api/ClassroomService;", "retrofit", "Lretrofit2/Retrofit;", "provideRetrofitInstance", "provideTopicService", "Lorg/oppia/data/backends/gae/api/TopicService;", "OppiaRetrofit", "data_debug"})
@dagger.Module()
public final class NetworkModule {
    
    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Singleton()
    @dagger.Provides()
    @org.oppia.data.backends.gae.NetworkModule.OppiaRetrofit()
    public final retrofit2.Retrofit provideRetrofitInstance() {
        return null;
    }
    
    /**
     * Provides the Topic service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Topic service implementation.
     */
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Singleton()
    @dagger.Provides()
    public final org.oppia.data.backends.gae.api.TopicService provideTopicService(@org.jetbrains.annotations.NotNull()
    @org.oppia.data.backends.gae.NetworkModule.OppiaRetrofit()
    retrofit2.Retrofit retrofit) {
        return null;
    }
    
    /**
     * Provides the Classroom service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Classroom service implementation.
     */
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Singleton()
    @dagger.Provides()
    public final org.oppia.data.backends.gae.api.ClassroomService provideClassroomService(@org.jetbrains.annotations.NotNull()
    @org.oppia.data.backends.gae.NetworkModule.OppiaRetrofit()
    retrofit2.Retrofit retrofit) {
        return null;
    }
    
    public NetworkModule() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\n\n\u0002\u0018\u0002\n\u0002\u0010\u001b\n\u0000\b\u0083\u0002\u0018\u00002\u00020\u0001B\u0000\u00a8\u0006\u0002"}, d2 = {"Lorg/oppia/data/backends/gae/NetworkModule$OppiaRetrofit;", "", "data_debug"})
    @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
    @javax.inject.Qualifier()
    static abstract @interface OppiaRetrofit {
    }
}