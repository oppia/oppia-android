# Reference: https://github.com/square/okhttp/blob/e1af67f082/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro.

-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# See: https://github.com/square/okhttp/issues/5167.
-dontwarn okhttp3.Authenticator*, okhttp3.CookieJar*, okhttp3.Dns*
-dontwarn okhttp3.internal.http2.PushObserver*, okhttp3.internal.io.FileSystem*
-dontwarn org.conscrypt.Conscrypt*

# See: https://github.com/square/okhttp/issues/6258#issuecomment-894168816.
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.bouncycastle.provider.BouncyCastleJsseProvider
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.net.ssl.OpenJSSE
