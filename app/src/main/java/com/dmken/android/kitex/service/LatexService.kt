package com.dmken.android.kitex.service

import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder

class LatexService {
    // FIXME: Use own service and do better validation.

    companion object {
        val BASE_URL = "http://www.texrendr.com/cgi-bin/mathtex.cgi?\\dpi{4096}"

        val HEADER_CONTENT_TYPE = "Content-Type"
        val HEADER_CONTENT_TYPE_VALID = "image/gif";

        // TODO: Extract encoding here.
        fun createEquationUrl(code: String): String = BASE_URL + URLEncoder.encode(code, "UTF-8")
    }

    private val client = OkHttpClient.Builder().build()

    fun retrieveEquation(code: String, callback: (LatexState, InputStream?) -> Unit) {
        // Thread: ?

        client.newCall(makeRequest(code)).enqueue(object : Callback {
            // Thread: Web

            override fun onFailure(call: Call, ex: IOException?) {
                return callback(LatexState.SERVER_ERROR, null)
            }

            override fun onResponse(call: Call, response: Response) {
                when {
                    !response.isSuccessful -> return callback(LatexState.SERVER_ERROR, null)
                    response.header("Content-Length") == "145" -> return callback(LatexState.INVALID_EQUATION, null)
                    response.header(HEADER_CONTENT_TYPE) == HEADER_CONTENT_TYPE_VALID -> handle(response, callback)
                    else -> callback(LatexState.SERVER_ERROR, null)
                }
            }
        })
    }

    private fun handle(response: Response, callback: (LatexState, InputStream?) -> Unit) {
        val body = response.body() ?: return callback(LatexState.SERVER_ERROR, null)
        return callback(LatexState.SUCCESSFUL, body.byteStream())
    }

    private fun makeRequest(code: String): Request = Request.Builder().url(createEquationUrl(code)).build()

    enum class LatexState {
        SUCCESSFUL,
        INVALID_EQUATION,
        SERVER_ERROR
    }
}