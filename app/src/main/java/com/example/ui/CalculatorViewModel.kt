package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.MathEvaluator
import com.example.data.local.CalculationHistory
import com.example.data.local.CalculatorDatabase
import com.example.data.local.CalculatorPreferences
import com.example.data.repository.CalculatorRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CalculatorTab {
    BASIC,
    SCIENTIFIC
}

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String = "",
    val finalResult: String? = null,
    val isDegreeMode: Boolean = true,
    val isSecondFunction: Boolean = false,
    val selectedTab: CalculatorTab = CalculatorTab.BASIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isHistoryOpen: Boolean = false,
    val error: String? = null
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculatorRepository
    private val preferences = CalculatorPreferences(application)

    private val _uiState: MutableStateFlow<CalculatorUiState>
    val uiState: StateFlow<CalculatorUiState>

    init {
        val dao = CalculatorDatabase.getDatabase(application).calculationHistoryDao()
        repository = CalculatorRepository(dao)

        val savedState = preferences.loadState()
        val restoredState = CalculatorUiState(
            expression = savedState.expression,
            previewResult = savedState.previewResult,
            finalResult = savedState.finalResult,
            isDegreeMode = savedState.isDegreeMode,
            isSecondFunction = savedState.isSecondFunction,
            selectedTab = savedState.selectedTab,
            themeMode = savedState.themeMode
        )
        val initialCalculatedState = if (restoredState.expression.isNotEmpty() && restoredState.finalResult == null) {
            updatePreview(restoredState)
        } else {
            restoredState
        }

        _uiState = MutableStateFlow(initialCalculatedState)
        uiState = _uiState.asStateFlow()

        viewModelScope.launch {
            _uiState.collect { state ->
                preferences.saveState(
                    expression = state.expression,
                    previewResult = state.previewResult,
                    finalResult = state.finalResult,
                    isDegreeMode = state.isDegreeMode,
                    isSecondFunction = state.isSecondFunction,
                    selectedTab = state.selectedTab,
                    themeMode = state.themeMode
                )
            }
        }
    }

    val historyList: StateFlow<List<CalculationHistory>> = repository.historyList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setTab(tab: CalculatorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleTheme() {
        _uiState.update { state ->
            val nextTheme = when (state.themeMode) {
                ThemeMode.SYSTEM -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.LIGHT
                ThemeMode.LIGHT -> ThemeMode.SYSTEM
            }
            state.copy(themeMode = nextTheme)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setHistoryOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = isOpen) }
    }

    fun toggleDegreeMode() {
        _uiState.update {
            val newMode = !it.isDegreeMode
            val newState = it.copy(isDegreeMode = newMode)
            updatePreview(newState)
        }
    }

    fun toggleSecondFunction() {
        _uiState.update { it.copy(isSecondFunction = !it.isSecondFunction) }
    }

    fun onDigit(digit: String) {
        _uiState.update { state ->
            // If we just evaluated a final result and user starts typing a digit, start fresh
            val baseExpr = if (state.finalResult != null && state.error == null) {
                ""
            } else {
                state.expression
            }

            val newExpr = if (baseExpr == "0" && digit != ".") {
                digit
            } else {
                baseExpr + digit
            }

            val updated = state.copy(
                expression = newExpr,
                finalResult = null,
                error = null
            )
            updatePreview(updated)
        }
    }

    fun onDecimal() {
        _uiState.update { state ->
            val baseExpr = if (state.finalResult != null && state.error == null) {
                "0"
            } else {
                state.expression
            }

            if (baseExpr.isEmpty()) {
                val updated = state.copy(expression = "0.", finalResult = null, error = null)
                updatePreview(updated)
            } else {
                // Check if current token already has a decimal
                val lastNumber = baseExpr.takeLastWhile { it.isDigit() || it == '.' }
                if (!lastNumber.contains('.')) {
                    val updated = state.copy(
                        expression = "$baseExpr.",
                        finalResult = null,
                        error = null
                    )
                    updatePreview(updated)
                } else {
                    state
                }
            }
        }
    }

    fun onOperator(op: String) {
        _uiState.update { state ->
            // If final result exists, continue operation on that result
            val baseExpr = if (state.finalResult != null && state.error == null) {
                state.finalResult
            } else {
                state.expression
            }

            if (baseExpr.isEmpty()) {
                if (op == "−" || op == "-") {
                    val updated = state.copy(expression = "−", finalResult = null, error = null)
                    updatePreview(updated)
                } else {
                    state
                }
            } else {
                val lastChar = baseExpr.last()
                val operators = listOf('+', '−', '-', '×', '*', '÷', '/', '^', '%')

                val newExpr = if (lastChar in operators) {
                    baseExpr.dropLast(1) + op
                } else {
                    baseExpr + op
                }

                val updated = state.copy(
                    expression = newExpr,
                    finalResult = null,
                    error = null
                )
                updatePreview(updated)
            }
        }
    }

    fun onFunction(funcName: String) {
        _uiState.update { state ->
            val baseExpr = if (state.finalResult != null && state.error == null) {
                ""
            } else {
                state.expression
            }

            val toAppend = "$funcName("
            val newExpr = if (baseExpr.isNotEmpty() && (baseExpr.last().isDigit() || baseExpr.last() == ')' || baseExpr.last() == 'π' || baseExpr.last() == 'e')) {
                "$baseExpr × $toAppend"
            } else {
                baseExpr + toAppend
            }

            val updated = state.copy(
                expression = newExpr,
                finalResult = null,
                error = null
            )
            updatePreview(updated)
        }
    }

    fun onConstant(constant: String) {
        _uiState.update { state ->
            val baseExpr = if (state.finalResult != null && state.error == null) {
                ""
            } else {
                state.expression
            }

            val newExpr = if (baseExpr.isNotEmpty() && (baseExpr.last().isDigit() || baseExpr.last() == ')' || baseExpr.last() == 'π' || baseExpr.last() == 'e')) {
                "$baseExpr × $constant"
            } else {
                baseExpr + constant
            }

            val updated = state.copy(
                expression = newExpr,
                finalResult = null,
                error = null
            )
            updatePreview(updated)
        }
    }

    fun onParenthesis() {
        _uiState.update { state ->
            val expr = state.expression
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }

            val newExpr = if (expr.isEmpty()) {
                "("
            } else {
                val lastChar = expr.last()
                if (lastChar == '(' || lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '^')) {
                    "$expr("
                } else if (openCount > closeCount && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '%')) {
                    "$expr)"
                } else {
                    "$expr × ("
                }
            }

            val updated = state.copy(
                expression = newExpr,
                finalResult = null,
                error = null
            )
            updatePreview(updated)
        }
    }

    fun onSpecial(action: String) {
        when (action) {
            "%" -> onOperator("%")
            "!" -> {
                _uiState.update { state ->
                    if (state.expression.isNotEmpty()) {
                        val updated = state.copy(
                            expression = state.expression + "!",
                            finalResult = null,
                            error = null
                        )
                        updatePreview(updated)
                    } else state
                }
            }
            "x²" -> {
                _uiState.update { state ->
                    if (state.expression.isNotEmpty()) {
                        val updated = state.copy(
                            expression = state.expression + "^2",
                            finalResult = null,
                            error = null
                        )
                        updatePreview(updated)
                    } else state
                }
            }
            "x³" -> {
                _uiState.update { state ->
                    if (state.expression.isNotEmpty()) {
                        val updated = state.copy(
                            expression = state.expression + "^3",
                            finalResult = null,
                            error = null
                        )
                        updatePreview(updated)
                    } else state
                }
            }
            "1/x" -> {
                _uiState.update { state ->
                    val expr = state.expression
                    val newExpr = if (expr.isEmpty()) {
                        "1÷("
                    } else {
                        "1÷($expr)"
                    }
                    val updated = state.copy(
                        expression = newExpr,
                        finalResult = null,
                        error = null
                    )
                    updatePreview(updated)
                }
            }
            "±" -> {
                _uiState.update { state ->
                    val expr = state.expression
                    if (expr.isEmpty()) {
                        state.copy(expression = "−")
                    } else if (expr.startsWith("−")) {
                        state.copy(expression = expr.removePrefix("−"))
                    } else if (expr.startsWith("-")) {
                        state.copy(expression = expr.removePrefix("-"))
                    } else {
                        state.copy(expression = "−$expr")
                    }
                }
            }
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.expression.isNotEmpty()) {
                // Check if deleting a multi-char function like "sin(", "cos(", "sqrt("
                val expr = state.expression
                val knownPrefixes = listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "asin(", "acos(", "atan(", "sinh(", "cosh(", "tanh(", "sqrt(", "cbrt(", "log(", "sin(", "cos(", "tan(", "ln(")
                var cutLength = 1
                for (p in knownPrefixes) {
                    if (expr.endsWith(p)) {
                        cutLength = p.length
                        break
                    }
                }

                val newExpr = expr.dropLast(cutLength).trimEnd()
                val updated = state.copy(
                    expression = newExpr,
                    finalResult = null,
                    error = null
                )
                updatePreview(updated)
            } else {
                state.copy(expression = "", previewResult = "", finalResult = null, error = null)
            }
        }
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                expression = "",
                previewResult = "",
                finalResult = null,
                error = null
            )
        }
    }

    fun onEquals() {
        val state = _uiState.value
        val expr = state.expression.trim()

        if (expr.isEmpty()) return

        // Auto-close any unclosed parens before evaluating
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        val balancedExpr = if (openCount > closeCount) {
            expr + ")".repeat(openCount - closeCount)
        } else {
            expr
        }

        when (val result = MathEvaluator.evaluate(balancedExpr, state.isDegreeMode)) {
            is MathEvaluator.EvalResult.Success -> {
                val formatted = result.formatted
                _uiState.update {
                    it.copy(
                        expression = balancedExpr,
                        finalResult = formatted,
                        previewResult = "",
                        error = null
                    )
                }

                // Save to Room DB
                viewModelScope.launch {
                    repository.saveCalculation(
                        expression = balancedExpr,
                        result = formatted,
                        isScientific = state.selectedTab == CalculatorTab.SCIENTIFIC
                    )
                }
            }
            is MathEvaluator.EvalResult.Error -> {
                _uiState.update {
                    it.copy(
                        finalResult = null,
                        error = result.message
                    )
                }
            }
        }
    }

    fun onHistoryItemClick(item: CalculationHistory, loadResultOnly: Boolean = false) {
        _uiState.update { state ->
            if (loadResultOnly) {
                state.copy(
                    expression = item.result,
                    finalResult = null,
                    previewResult = "",
                    error = null,
                    isHistoryOpen = false
                )
            } else {
                state.copy(
                    expression = item.expression,
                    finalResult = item.result,
                    previewResult = "",
                    error = null,
                    isHistoryOpen = false
                )
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    private fun updatePreview(state: CalculatorUiState): CalculatorUiState {
        val expr = state.expression.trim()
        if (expr.isEmpty() || state.finalResult != null) {
            return state.copy(previewResult = "")
        }

        // Complete any open parentheses tentatively for preview
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        val testExpr = if (openCount > closeCount) {
            expr + ")".repeat(openCount - closeCount)
        } else {
            expr
        }

        val eval = MathEvaluator.evaluate(testExpr, state.isDegreeMode)
        return if (eval is MathEvaluator.EvalResult.Success) {
            state.copy(previewResult = "= ${eval.formatted}", error = null)
        } else {
            state.copy(previewResult = "")
        }
    }
}
