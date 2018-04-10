package com.dmken.android.kitex.preference

import android.content.Context
import android.preference.PreferenceManager

class Preferences {
    companion object {
        val PREF_KEYBOARD_LAYOUT = "keyboard_layout"
        val PREF_KEYBOARD_LAYOUT_DEFAULT = 1

        val PREF_COPY_TO_CLIPBOARD = "copy_to_clipboard"
        val PREF_COPY_TO_CLIPBOARD_DEFAULT = 0

        // ~ Getter/Setter ~

        fun getKeyboardLayout(context: Context): KeyboardLayout
                = KeyboardLayout.byId(getInt(context, PREF_KEYBOARD_LAYOUT, PREF_KEYBOARD_LAYOUT_DEFAULT))

        fun setKeyboardLayout(context: Context, value: KeyboardLayout)
                = setInt(context, PREF_KEYBOARD_LAYOUT, value.id)

        fun getCodeHandling(context: Context): CodeHandling
                = CodeHandling.byId(getInt(context, PREF_COPY_TO_CLIPBOARD, PREF_COPY_TO_CLIPBOARD_DEFAULT))

        fun setCodeHandling(context: Context, value: CodeHandling)
                = setInt(context, PREF_COPY_TO_CLIPBOARD, value.id)

        // ~ Private Getter/Setter ~

        private fun getInt(context: Context, name: String, default: Int): Int
                = getPreferences(context).getString(name, default.toString()).toInt()

        private fun setInt(context: Context, name: String, value: Int)
                = getPreferences(context).edit().putString(name, value.toString()).apply()

        private fun getBoolean(context: Context, name: String, default: Boolean): Boolean
                = getPreferences(context).getString(name, default.toString()).toBoolean()

        private fun setBoolean(context: Context, name: String, value: Boolean)
                = getPreferences(context).edit().putString(name, value.toString()).apply()

        private fun getPreferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
    }

    enum class KeyboardLayout(val id: Int) {
        ENGLISH(0),
        GERMAN(1);

        companion object {
            fun byId(id: Int): KeyboardLayout {
                for (layout in values()) {
                    if (layout.id == id) {
                        return layout;
                    }
                }
                throw IllegalArgumentException("Unknown layout id $id!");
            }
        }
    }

    enum class CodeHandling(val id: Int) {
        DO_NOTHING(0),
        COPY_TO_CLIPBOARD(1),
        DISCARD(2);

        companion object {
            fun byId(id: Int): CodeHandling {
                for (handling in values()) {
                    if (handling.id == id) {
                        return handling
                    }
                }

                throw IllegalArgumentException("Unknown handling id $id!")
            }
        }
    }
}