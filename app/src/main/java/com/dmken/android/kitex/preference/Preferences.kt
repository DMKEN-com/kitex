package com.dmken.android.kitex.preference

import android.content.Context
import android.preference.PreferenceManager

class Preferences {
    companion object {
        val PREF_KEYBOARD_LAYOUT = "keyboard_layout"
        val PREF_KEYBOARD_LAYOUT_DEFAULT = 1

        val PREF_LATEX_ENVIRONMENT = "latex_environment"
        val PREF_LATEX_ENVIRONMENT_DEFAULT = 1

        // ~ Getter/Setter ~

        fun getKeyboardLayout(context: Context): KeyboardLayout
                = KeyboardLayout.byId(getInt(context, PREF_KEYBOARD_LAYOUT, PREF_KEYBOARD_LAYOUT_DEFAULT))

        fun setKeyboardLayout(context: Context, value: KeyboardLayout)
                = setInt(context, PREF_KEYBOARD_LAYOUT, value.id)

        fun getLatexEnvironment(context: Context): LatexEnvironment
                = LatexEnvironment.byId(getInt(context, PREF_LATEX_ENVIRONMENT, PREF_LATEX_ENVIRONMENT_DEFAULT))

        fun setLatexEnvironment(context: Context, value: LatexEnvironment)
                = setInt(context, PREF_LATEX_ENVIRONMENT, value.id)

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

    enum class LatexEnvironment(val id: Int) {
        EQUATION(0),
        ALIGN(1),
        DOLLAR(2),
        BRACKETS(3);

        companion object {
            fun byId(id: Int): LatexEnvironment {
                for (environment in values()) {
                    if (environment.id == id) {
                        return environment
                    }
                }

                throw IllegalArgumentException("Unknown environment id $id!")
            }
        }
    }
}