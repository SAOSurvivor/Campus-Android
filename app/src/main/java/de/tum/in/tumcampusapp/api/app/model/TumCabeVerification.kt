package de.tum.`in`.tumcampusapp.api.app.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.isNotEmpty
import de.tum.`in`.tumcampusapp.utils.string
import okhttp3.RequestBody
import org.json.JSONObject
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
    fun create(body: RequestBody? = null): String?
}

class RealTumCabeVerificationProvider(
    private val context: Context
) : TumCabeVerificationProvider {

    private val gson: Gson
        get() = GsonBuilder().disableHtmlEscaping().create()

    override val deviceId: String
        get() = AuthenticationManager.getDeviceID(context)

    override fun create(body: RequestBody?): String? {
        val date = Date().toString()
        val rand = BigInteger(130, SecureRandom()).toString(32)
        val deviceID = AuthenticationManager.getDeviceID(context)

        val signature = try {
            AuthenticationManager(context).sign(date + rand + deviceID)
        } catch (e: NoPrivateKey) {
            Utils.log(e)
            return null
        }

        val verification = TumCabeVerification(signature, date, rand, deviceID)
        val payload = gson.toJson(verification)

        return if (body != null && body.isNotEmpty()) {
            val bodyJson = JSONObject(body.string())
            val payloadJson = JSONObject(payload)
            payloadJson.putOpt("data", bodyJson)
            payloadJson.toString()
        } else {
            payload
        }
    }

}
