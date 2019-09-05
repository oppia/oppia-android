package org.oppia.data.backends.gae;

import java.lang.System;

/**
 * An object that contains functions and constants specifically related to network only.
 */
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\t\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lorg/oppia/data/backends/gae/NetworkSettings;", "", "()V", "DEVELOPER_URL", "", "PROD_URL", "XSSI_PREFIX", "isDeveloperMode", "", "getBaseUrl", "data_debug"})
public final class NetworkSettings {
    private static boolean isDeveloperMode;
    
    /**
     * DEVELOPER URL which connects to development server
     */
    private static final java.lang.String DEVELOPER_URL = "https://oppia.org";
    
    /**
     * PRODUCTION URL which connects to production server
     */
    private static final java.lang.String PROD_URL = "https://oppia.org";
    
    /**
     * Prefix in Json response for extra layer of security in API calls
     *
     * @see <a href="https://github.com/oppia/oppia/blob/8f9eed/feconf.py#L319">XSSI_PREFIX</a>
     *
     * Remove this prefix from every Json response which is achieved in [NetworkInterceptor]
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String XSSI_PREFIX = ")]}\'";
    public static final org.oppia.data.backends.gae.NetworkSettings INSTANCE = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBaseUrl() {
        return null;
    }
    
    private NetworkSettings() {
        super();
    }
}