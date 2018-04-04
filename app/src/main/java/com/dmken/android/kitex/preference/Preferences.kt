package com.dmken.android.kitex.preference

import android.content.Context
import android.preference.PreferenceManager
import java.security.Key

class Preferences {
    companion object {
        val PREF_KEYBOARD_LAYOUT = "keyboard_layout"
        val PREF_KEYBOARD_LAYOUT_DEFAULT = 1;

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

        // ~ Getter/Setter ~

        fun getKeyboardLayout(context: Context): KeyboardLayout
                = KeyboardLayout.byId(getInt(context, PREF_KEYBOARD_LAYOUT, PREF_KEYBOARD_LAYOUT_DEFAULT))

        fun setKeyboardLayout(context: Context, value: KeyboardLayout)
                = setInt(context, PREF_KEYBOARD_LAYOUT, value.id)

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
}