package org.oppia.data.backends.gae;

import java.lang.System;

/**
 * Interceptor on top of Retrofit to modify requests and response
 *
 * The Interceptor removes XSSI_PREFIX from every response to produce valid Json
 */
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b\u00a8\u0006\n"}, d2 = {"Lorg/oppia/data/backends/gae/NetworkInterceptor;", "Lokhttp3/Interceptor;", "()V", "intercept", "Lokhttp3/Response;", "chain", "Lokhttp3/Interceptor$Chain;", "removeXSSIPrefix", "", "rawJson", "data_debug"})
@javax.inject.Singleton()
public final class NetworkInterceptor implements okhttp3.Interceptor {
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public okhttp3.Response intercept(@org.jetbrains.annotations.NotNull()
    okhttp3.Interceptor.Chain chain) throws java.io.IOException {
        return null;
    }
    
    /**
     * This function accepts a non-null string which is a JSON response and
     * removes XSSI_PREFIX from response before deserialization
     * @param rawJson: This is the string that we get in body of our response
     * @return String: rawJson without XSSI_PREFIX
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String removeXSSIPrefix(@org.jetbrains.annotations.NotNull()
    java.lang.String rawJson) {
        return null;
    }
    
    @javax.inject.Inject()
    public NetworkInterceptor() {
        super();
    }
}