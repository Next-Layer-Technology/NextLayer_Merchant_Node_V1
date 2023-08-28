package com.sis.clightapp.util
import java.io.InputStream
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class CustomTrustManager(private val caCertificate: InputStream) : X509TrustManager {
    private var trustManagers: Array<TrustManager>
    private val defaultTrustManager: X509TrustManager

    init {
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        trustManagers = trustManagerFactory.trustManagers
        if ((trustManagers as Array<out TrustManager>?)?.isEmpty() == true || trustManagers[0] !is X509TrustManager) {
            throw NoSuchAlgorithmException("No trust manager found")
        }
        defaultTrustManager = trustManagers[0] as X509TrustManager
    }

    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
        return defaultTrustManager.acceptedIssuers
    }

    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
        defaultTrustManager.checkClientTrusted(chain, authType)
    }

    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
        // Create a KeyStore containing the CA certificate
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val caCert = certificateFactory.generateCertificate(caCertificate)
        keyStore.setCertificateEntry("ca", caCert)

        // Create a TrustManager that trusts the CA certificate in the KeyStore
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        val trustManagers = trustManagerFactory.trustManagers
        val customTrustManager = trustManagers[0] as X509TrustManager

        // Validate the server certificate using the customTrustManager
        customTrustManager.checkServerTrusted(chain, authType)
    }
}