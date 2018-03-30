package com.dmken.android.kitex.service

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.dmken.android.kitex.R

class KeyboardInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    private var caps = false

    private lateinit var keyboard: Keyboard;
    private lateinit var keyboardView: KeyboardView

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.keys_layout)

        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        return keyboardView
    }

    override fun swipeRight() { }

    override fun onPress(code: Int) { }

    override fun onRelease(code: Int) { }

    override fun swipeLeft() { }

    override fun swipeUp() { }

    override fun swipeDown() { }

    override fun onKey(code: Int, secondaryCodes: IntArray?) {
        val input = currentInputConnection;
        when {
            code == Keyboard.KEYCODE_DELETE -> input.deleteSurroundingText(1, 0)
            code == Keyboard.KEYCODE_DONE -> Toast.makeText(this,  "The data is going to be compiled...", Toast.LENGTH_LONG).show()
            code == Keyboard.KEYCODE_SHIFT -> {
                caps = !caps
                keyboard.setShifted(caps)
                keyboardView.invalidateAllKeys()
            }
            code < 0 -> throw IllegalStateException("Unknown code $code.")
            else -> {
                val char = code.toChar()
                val text: String;
                if (caps) {
                    when {
                        char == 'ß' -> text = "SS"
                        char.isLetter() -> text = char.toUpperCase().toString()
                        else -> text = char.toString()
                    }
                } else {
                    text = char.toString()
                }
                input.commitText(text, text.length)
            }
        }
    }

    override fun onText(text: CharSequence?) { }
}
