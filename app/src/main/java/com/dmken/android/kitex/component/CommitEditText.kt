package com.dmken.android.kitex.component

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v13.view.inputmethod.EditorInfoCompat
import android.support.v13.view.inputmethod.InputConnectionCompat
import android.support.v4.os.BuildCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.dmken.android.kitex.util.CommonConstants

class CommitEditText : AppCompatEditText {
    var commitListener: ((uri: Uri) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttribute: Int) : super(context, attributeSet, defStyleAttribute)

    override fun onCreateInputConnection(info: EditorInfo): InputConnection {
        val input = super.onCreateInputConnection(info)
        EditorInfoCompat.setContentMimeTypes(info, arrayOf(CommonConstants.IMAGE_MIME_TYPE))
        return InputConnectionCompat.createWrapper(input, info) { inputInfo, flags, _ ->
            var failed = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && (flags or InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    inputInfo.requestPermission()
                } catch (ex: Exception) {
                    failed = false
                }
            }
            if (failed) {
                commitListener?.invoke(inputInfo.contentUri)
            }
            failed
        }
    }
}