package com.dmken.android.kitex.service

import android.content.*
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.*
import android.provider.MediaStore
import android.support.v13.view.inputmethod.EditorInfoCompat
import android.support.v13.view.inputmethod.InputConnectionCompat
import android.support.v13.view.inputmethod.InputContentInfoCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dmken.android.kitex.R
import com.dmken.android.kitex.preference.Preferences
import com.dmken.android.kitex.util.CommonConstants
import com.dmken.android.kitex.util.PermissionUtil
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*


class KeyboardInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    private var caps = false

    private lateinit var keyboard: Keyboard;
    private lateinit var keyboardView: KeyboardView

    override fun onCreateInputView(): View {
        // Thread: UI

        if (!PermissionUtil.arePermissionsGranted(this)) {
            throw IllegalStateException("Permissions not granted!")
        }

        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        val layoutId = when (Preferences.getKeyboardLayout(this)) {
            Preferences.KeyboardLayout.GERMAN -> R.xml.keys_layout_qwertz
            Preferences.KeyboardLayout.ENGLISH -> R.xml.keys_layout_querty
        }
        keyboard = Keyboard(this, layoutId)

        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        return keyboardView
    }

    override fun swipeRight() {}

    override fun onPress(code: Int) {}

    override fun onRelease(code: Int) {}

    override fun swipeLeft() {}

    override fun swipeUp() {}

    override fun swipeDown() {}

    override fun onKey(code: Int, secondaryCodes: IntArray?) {
        // Thread: UI

        val setCaps = { shouldCaps: Boolean ->
            if (caps != shouldCaps) {
                caps = shouldCaps
                keyboard.setShifted(caps)
                keyboardView.invalidateAllKeys()
            }
        }

        when {
            code == Keyboard.KEYCODE_DELETE -> {
                if ((currentInputConnection.getSelectedText(0) ?: "").isEmpty()) {
                    currentInputConnection.deleteSurroundingText(1, 0)
                } else {
                    currentInputConnection.commitText("", 1)
                }
            }
            code == Keyboard.KEYCODE_DONE -> {
                // TODO: Is there a better way to get ALL text?
                currentInputConnection.performContextMenuAction(android.R.id.selectAll)
                val text = (currentInputConnection.getSelectedText(0) ?: "").toString()

                setCaps(false)

                startCompile(text)
            }
            code == Keyboard.KEYCODE_SHIFT -> setCaps(!caps)
            code < 0 -> throw IllegalStateException("Unknown code $code.")
            else -> {
                var capsChanged: Boolean = false;
                val char = code.toChar()
                val text: String;
                if (caps) {
                    when {
                        char == 'ß' -> {
                            text = "SS"

                            Log.d("DEMO", "<${code.toChar()}> <${secondaryCodes!!.map { c -> c.toChar() }}>")

                            capsChanged = true
                        }
                        char.isLetter() -> {
                            text = char.toUpperCase().toString()

                            capsChanged = true
                        }
                        else -> text = char.toString()
                    }
                } else {
                    text = char.toString()
                }
                currentInputConnection.commitText(text, text.length)

                if (capsChanged) {
                    setCaps(false)
                }
            }
        }
    }

    override fun onText(text: CharSequence?) {}

    private fun startCompile(code: String) {
        // Thread: UI

        Toast.makeText(this, getString(R.string.msg_compileStarted), Toast.LENGTH_LONG).show()

        LatexService().retrieveEquation(code, { state, bytes ->
            // Thread: Web

            Handler(Looper.getMainLooper()).post {
                // Thread: UI

                when (state) {
                    LatexService.LatexState.INVALID_EQUATION -> Toast.makeText(applicationContext, getString(R.string.msg_invalidEquation), Toast.LENGTH_LONG).show()
                    LatexService.LatexState.SERVER_ERROR -> Toast.makeText(applicationContext, getString(R.string.msg_serverError), Toast.LENGTH_LONG).show()
                    LatexService.LatexState.SUCCESSFUL -> finishedCompilation(code, bytes!!) // '!!' is safe here as SUCCESSFUL implies that bytes != null
                }
            }
        })
    }

    private fun finishedCompilation(code: String, bytes: InputStream) {
        // Thread: UI

        val date = Date()
        val name = "equation-${date.time}"

        AsyncTask.execute {
            // Thread: Async

            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, name)
            values.put(MediaStore.Images.Media.MIME_TYPE, CommonConstants.IMAGE_MIME_TYPE)
            values.put(MediaStore.Images.Media.DATE_ADDED, date.time)
            values.put(MediaStore.Images.Media.DATE_TAKEN, date.time)
            values.put(MediaStore.Images.Media.DATA, saveEquation(name, bytes))
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            Handler(Looper.getMainLooper()).post {
                // Thread: UI

                Toast.makeText(this, "The compilation was finished!", Toast.LENGTH_LONG).show()

                when (Preferences.getCodeHandling(applicationContext)) {
                    Preferences.CodeHandling.DISCARD -> {
                        // TODO: Is there a better way to delete ALL text?
                        currentInputConnection.performContextMenuAction(android.R.id.selectAll)
                        currentInputConnection.commitText("", 1)
                    }
                    Preferences.CodeHandling.COPY_TO_CLIPBOARD -> {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.primaryClip = ClipData.newPlainText("latex-code", code)
                    }
                    Preferences.CodeHandling.DO_NOTHING -> {
                    }
                }

                if (isCommitContentSupported()) {
                    val flag: Int;
                    if (Build.VERSION.SDK_INT >= 25) {
                        flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
                    } else {
                        flag = 0
                        grantUriPermission(currentInputEditorInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val info = InputContentInfoCompat(uri, ClipDescription(name, arrayOf(CommonConstants.IMAGE_MIME_TYPE)), null)
                    InputConnectionCompat.commitContent(currentInputConnection, currentInputEditorInfo, info, flag, null)
                } else {
                    Toast.makeText(applicationContext, getText(R.string.msg_commitContentNotSupported), Toast.LENGTH_SHORT).show()

                    val share = Intent(Intent.ACTION_SEND)
                    share.putExtra(Intent.EXTRA_STREAM, uri)
                    share.type = CommonConstants.IMAGE_MIME_TYPE
                    startActivity(Intent.createChooser(share, getText(R.string.label_share)))
                }
            }
        }
    }

    private fun isCommitContentSupported(): Boolean {
        // Thread: ?

        if (currentInputConnection == null) {
            return false
        }
        if (currentInputEditorInfo == null) {
            return false
        }
        return EditorInfoCompat.getContentMimeTypes(currentInputEditorInfo).contains(CommonConstants.IMAGE_MIME_TYPE)
    }

    private fun saveEquation(name: String, bytes: InputStream): String {
        // Thread: Async

        val directory = File(Environment.getExternalStorageDirectory(), CommonConstants.IMAGE_DIRECTORY)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "$name.${CommonConstants.IMAGE_FILE_ENDING}")
        if (file.exists()) {
            file.delete()
        }
        FileOutputStream(file).use { out -> bytes.copyTo(out) }
        return file.absolutePath
    }
}
