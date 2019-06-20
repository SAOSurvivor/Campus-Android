package de.tum.`in`.tumcampusapp.api.app.interceptors

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.tum.`in`.tumcampusapp.api.app.model.ObfuscatedIdsUpload
import de.tum.`in`.tumcampusapp.api.app.model.TumCabeVerificationProvider
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.InterruptedIOException
import javax.inject.Inject

private const val REQUIRES_VERIFICATION = "x-requires-verification"
private val JSON = MediaType.parse("application/json; charset=utf-8")

class TumCabeVerificationInterceptor @Inject constructor(
    private val verificationProvider: TumCabeVerificationProvider
) : Interceptor {

    private val gson: Gson
        get() = GsonBuilder().disableHtmlEscaping().create()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requiresVerification = request.headers().getBoolean(REQUIRES_VERIFICATION)

        val modifiedRequest = if (requiresVerification) {
            request.withVerification()
        } else {
            request
        }

        return chain.proceed(modifiedRequest)
    }

    private fun Headers.getBoolean(key: String): Boolean = get(key)?.toBoolean() ?: false

    private fun Request.withVerification(): Request {
        if (url().encodedPath().contains("members/uploadIds/")) {
            // While every other request sends a TumCabeVerification object, this request requires
            // an ObfuscatedIdsUpload object which contains a TumCabeVerification. Therefore, we
            // need to do this special case handling.
            return buildObfuscatedIdsUploadRequest(this)
        }

        val body = body()?.stringOrEmpty()

        val verification = verificationProvider.create(body) ?: throw TumCabeVerificationException()
        val payload = gson.toJson(verification)

        println("Payload: \n")
        println(payload)

        val requestBody = RequestBody.create(JSON, payload)

        return when (method()) {
            "POST" -> newBuilder().post(requestBody).build()
            "PUT" -> newBuilder().put(requestBody).build()
            else -> this
        }
    }

    private fun buildObfuscatedIdsUploadRequest(request: Request): Request {
        val body = request.body().stringOrEmpty()
        val idsUpload = gson.fromJson(body, ObfuscatedIdsUpload::class.java)
        idsUpload.verification = verificationProvider.create()

        val requestBody = RequestBody.create(JSON, gson.toJson(idsUpload))
        return request.newBuilder().post(requestBody).build()
    }

    private fun RequestBody?.stringOrEmpty(): String {
        if (this == null) {
            return ""
        }

        val buffer = Buffer()
        writeTo(buffer)
        return buffer.readUtf8()
    }

}

class TumCabeVerificationException : InterruptedIOException()
