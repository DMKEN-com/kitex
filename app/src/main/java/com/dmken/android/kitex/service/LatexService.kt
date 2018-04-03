package com.dmken.android.kitex.service

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import java.util.logging.Logger

class LatexService {
    companion object {
        private const val TAG = "LatexService"

        private const val SECRET_TOKEN = "dsoifhgsfzugsafi76t32oiuro872z308r0932urigsa8fdii8t3o872gfi7ewgafai8sg8632t8fzgsai8fh8fg32"
        val BASE_URL = "https://www.dmken.com/kitex/?token=$SECRET_TOKEN"
    }

    private val client = OkHttpClient.Builder().build()

    fun retrieveEquation(code: String, callback: (LatexState, InputStream?) -> Unit) {
        // Thread: ?

        Log.d(TAG, "Rendering <$code>.")

        client.newCall(makeRequest(code)).enqueue(object : Callback {
            // Thread: Web

            override fun onFailure(call: Call, ex: IOException?) {
                return callback(LatexState.SERVER_ERROR, null)
            }

            override fun onResponse(call: Call, response: Response) {
                when {
                    response.isSuccessful -> {
                        Log.d(TAG, "Rendering of <$code> was successful.")

                        return handle(response, callback)
                    }
                    response.code() == 400 -> {
                        Log.d(TAG, "Rendering of <$code> failed (invalid equation).")

                        return callback(LatexState.INVALID_EQUATION, null)
                    }
                    response.code() == 401 -> {
                        Log.e(TAG, "Secret token was not accepted by the server (error 401).")

                        return callback(LatexState.SERVER_ERROR, null)
                    }
                    response.code() == 405 -> {
                        Log.e(TAG, "INTERNAL ERROR: Wrong HTTP method was used (error 405).")

                        return callback(LatexState.SERVER_ERROR, null)
                    }
                    else -> {
                        Log.e(TAG, "Internal server error (code ${response.code()}.")

                        return callback(LatexState.SERVER_ERROR, null)
                    }
                }
            }
        })
    }

    private fun handle(response: Response, callback: (LatexState, InputStream?) -> Unit) {
        val body = response.body() ?: return callback(LatexState.SERVER_ERROR, null)
        return callback(LatexState.SUCCESSFUL, body.byteStream())
    }

    private fun makeRequest(code: String): Request {
        return Request.Builder()
                .url(BASE_URL)
                .post(RequestBody.create(MediaType.parse("text/latex"), "\\begin{equation*} $code \\end{equation*}"))
                .build()
    }

    enum class LatexState {
        SUCCESSFUL,
        INVALID_EQUATION,
        SERVER_ERROR
    }
}