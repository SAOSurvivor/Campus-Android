package de.tum.`in`.tumcampusapp.component.ui.eduroam

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import de.tum.`in`.tumcampusapp.utils.Const
import org.jetbrains.anko.wifiManager

class EduroamController(
    private val context: Context
) {

    val configuration: WifiConfiguration?
        get() = context.wifiManager.configuredNetworks
            .firstOrNull { it.SSID == "\"${Const.EDUROAM_SSID}\"" }

    val isConfigured: Boolean
        get() = configuration != null

    val isNotConfigured: Boolean
        get() = configuration == null

    fun configureEduroam(
        lrzId: String,
        password: String
    ): Boolean {
        val update = configuration != null
        val config = configuration ?: WifiConfiguration()

        config.apply {
            SSID = "\"${Const.EDUROAM_SSID}\""
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            status = WifiConfiguration.Status.ENABLED
        }

        config.setupEnterpriseConfig(lrzId, password)

        val networkId  = if (update) {
            context.wifiManager.updateNetwork(config)
        } else {
            context.wifiManager.addNetwork(config)
        }

        if (networkId == -1) {
            return false
        }

        context.wifiManager.enableNetwork(networkId, true)
        return true
    }

    private fun WifiConfiguration.setupEnterpriseConfig(
        lrzId: String,
        password: String
    ) {
        enterpriseConfig.identity = "$lrzId@eduroam.mwn.de"
        enterpriseConfig.password = password
        enterpriseConfig.eapMethod = WifiEnterpriseConfig.Eap.PWD
    }

}