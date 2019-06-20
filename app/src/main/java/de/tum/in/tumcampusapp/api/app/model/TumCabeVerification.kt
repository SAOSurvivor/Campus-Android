package de.tum.`in`.tumcampusapp.api.app.model

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.utils.Utils
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Date

data class TumCabeVerification(
    val signature: String,
    val date: String,
    val rand: String,
    val device: String,
    var data: Any? = null
)

interface TumCabeVerificationProvider {
    val deviceId: String
    fun create(data: Any? = null): TumCabeVerification?
}

class RealTumCabeVerificationProvider(
    private val context: Context
) : TumCabeVerificationProvider {

    override val deviceId: String
        get() = AuthenticationManager.getDeviceID(context)

    override fun create(data: Any?): TumCabeVerification? {
        val date = Date().toString()
        val rand = BigInteger(130, SecureRandom()).toString(32)
        val deviceID = AuthenticationManager.getDeviceID(context)

        val signature = try {
            AuthenticationManager(context).sign(date + rand + deviceID)
        } catch (e: NoPrivateKey) {
            Utils.log(e)
            return null
        }

        return TumCabeVerification(signature, date, rand, deviceID, data)
    }

}
