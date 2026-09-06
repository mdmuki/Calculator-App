package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.CalculatorTab
import com.example.ui.theme.ThemeMode

data class SavedCalculatorState(
    val expression: String = "",
    val previewResult: String = "",
    val finalResult: String? = null,
    val isDegreeMode: Boolean = true,
    val isSecondFunction: Boolean = false,
    val selectedTab: CalculatorTab = CalculatorTab.BASIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class CalculatorPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EXPRESSION = "key_expression"
        private const val KEY_PREVIEW_RESULT = "key_preview_result"
        private const val KEY_FINAL_RESULT = "key_final_result"
        private const val KEY_HAS_FINAL_RESULT = "key_has_final_result"
        private const val KEY_DEGREE_MODE = "key_degree_mode"
        private const val KEY_SECOND_FUNCTION = "key_second_function"
        private const val KEY_SELECTED_TAB = "key_selected_tab"
        private const val KEY_THEME_MODE = "key_theme_mode"
    }

    fun saveState(
        expression: String,
        previewResult: String,
        finalResult: String?,
        isDegreeMode: Boolean,
        isSecondFunction: Boolean,
        selectedTab: CalculatorTab,
        themeMode: ThemeMode
    ) {
        prefs.edit()
            .putString(KEY_EXPRESSION, expression)
            .putString(KEY_PREVIEW_RESULT, previewResult)
            .putBoolean(KEY_HAS_FINAL_RESULT, finalResult != null)
            .putString(KEY_FINAL_RESULT, finalResult ?: "")
            .putBoolean(KEY_DEGREE_MODE, isDegreeMode)
            .putBoolean(KEY_SECOND_FUNCTION, isSecondFunction)
            .putString(KEY_SELECTED_TAB, selectedTab.name)
            .putString(KEY_THEME_MODE, themeMode.name)
            .apply()
    }

    fun loadState(): SavedCalculatorState {
        val expression = prefs.getString(KEY_EXPRESSION, "") ?: ""
        val previewResult = prefs.getString(KEY_PREVIEW_RESULT, "") ?: ""
        val hasFinal = prefs.getBoolean(KEY_HAS_FINAL_RESULT, false)
        val finalResult = if (hasFinal) prefs.getString(KEY_FINAL_RESULT, null) else null
        val isDegreeMode = prefs.getBoolean(KEY_DEGREE_MODE, true)
        val isSecondFunction = prefs.getBoolean(KEY_SECOND_FUNCTION, false)
        val tabName = prefs.getString(KEY_SELECTED_TAB, CalculatorTab.BASIC.name) ?: CalculatorTab.BASIC.name
        val selectedTab = try {
            CalculatorTab.valueOf(tabName)
        } catch (_: Exception) {
            CalculatorTab.BASIC
        }
        val themeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeName)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        return SavedCalculatorState(
            expression = expression,
            previewResult = previewResult,
            finalResult = finalResult,
            isDegreeMode = isDegreeMode,
            isSecondFunction = isSecondFunction,
            selectedTab = selectedTab,
            themeMode = themeMode
        )
    }
}
