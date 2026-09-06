package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculatorTab

enum class ButtonType {
    NUMBER,
    FUNCTION,
    OPERATOR,
    EQUALS,
    SCIENTIFIC
}

@Composable
fun CalculatorKeypad(
    selectedTab: CalculatorTab,
    isDegreeMode: Boolean,
    isSecondFunction: Boolean,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onParenthesis: () -> Unit,
    onSpecial: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    onToggleDegree: () -> Unit,
    onToggleSecondFunction: () -> Unit,
    modifier: Modifier = Modifier
) {
    // High Density Keypad Container with top rounded corners and shadow elevation
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                spotColor = Color.Black.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState == CalculatorTab.SCIENTIFIC) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "keypad_transition",
                modifier = Modifier.fillMaxWidth()
            ) { tab ->
                if (tab == CalculatorTab.BASIC) {
                    BasicKeypad(
                        onDigit = onDigit,
                        onDecimal = onDecimal,
                        onOperator = onOperator,
                        onParenthesis = onParenthesis,
                        onSpecial = onSpecial,
                        onBackspace = onBackspace,
                        onClear = onClear,
                        onEquals = onEquals
                    )
                } else {
                    ScientificKeypad(
                        isDegreeMode = isDegreeMode,
                        isSecondFunction = isSecondFunction,
                        onDigit = onDigit,
                        onDecimal = onDecimal,
                        onOperator = onOperator,
                        onFunction = onFunction,
                        onConstant = onConstant,
                        onParenthesis = onParenthesis,
                        onSpecial = onSpecial,
                        onBackspace = onBackspace,
                        onClear = onClear,
                        onEquals = onEquals,
                        onToggleDegree = onToggleDegree,
                        onToggleSecondFunction = onToggleSecondFunction
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtle Visual Indicator Pill
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            )
        }
    }
}

@Composable
private fun BasicKeypad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onParenthesis: () -> Unit,
    onSpecial: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: C, ( ), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeyButton(text = "C", type = ButtonType.FUNCTION, weight = 1f, testTag = "btn_ac") { onClear() }
            KeyButton(text = "( )", type = ButtonType.FUNCTION, weight = 1f, testTag = "btn_parens") { onParenthesis() }
            KeyButton(text = "%", type = ButtonType.FUNCTION, weight = 1f, testTag = "btn_percent") { onSpecial("%") }
            KeyButton(text = "÷", type = ButtonType.OPERATOR, weight = 1f, fontSize = 26.sp, testTag = "btn_div") { onOperator("÷") }
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeyButton(text = "7", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_7") { onDigit("7") }
            KeyButton(text = "8", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_8") { onDigit("8") }
            KeyButton(text = "9", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_9") { onDigit("9") }
            KeyButton(text = "×", type = ButtonType.OPERATOR, weight = 1f, fontSize = 26.sp, testTag = "btn_mul") { onOperator("×") }
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeyButton(text = "4", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_4") { onDigit("4") }
            KeyButton(text = "5", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_5") { onDigit("5") }
            KeyButton(text = "6", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_6") { onDigit("6") }
            KeyButton(text = "−", type = ButtonType.OPERATOR, weight = 1f, fontSize = 26.sp, testTag = "btn_sub") { onOperator("−") }
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeyButton(text = "1", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_1") { onDigit("1") }
            KeyButton(text = "2", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_2") { onDigit("2") }
            KeyButton(text = "3", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_3") { onDigit("3") }
            KeyButton(text = "+", type = ButtonType.OPERATOR, weight = 1f, fontSize = 26.sp, testTag = "btn_add") { onOperator("+") }
        }

        // Row 5: 0, ., ⌫, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeyButton(text = "0", type = ButtonType.NUMBER, weight = 1f, testTag = "btn_0") { onDigit("0") }
            KeyButton(text = ".", type = ButtonType.NUMBER, weight = 1f, fontSize = 28.sp, testTag = "btn_dot") { onDecimal() }
            KeyButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                type = ButtonType.NUMBER,
                weight = 1f,
                testTag = "btn_backspace"
            ) { onBackspace() }
            KeyButton(
                text = "=",
                type = ButtonType.EQUALS,
                weight = 1f,
                fontSize = 30.sp,
                testTag = "btn_equals"
            ) { onEquals() }
        }
    }
}

@Composable
private fun ScientificKeypad(
    isDegreeMode: Boolean,
    isSecondFunction: Boolean,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onParenthesis: () -> Unit,
    onSpecial: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    onToggleDegree: () -> Unit,
    onToggleSecondFunction: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Row 1 (Sci Control): 2nd, DEG/RAD, sin, cos, tan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            KeyButton(
                text = "2nd",
                type = ButtonType.SCIENTIFIC,
                weight = 1f,
                height = 46.dp,
                fontSize = 14.sp,
                isHighlighted = isSecondFunction,
                testTag = "btn_2nd"
            ) { onToggleSecondFunction() }

            KeyButton(
                text = if (isDegreeMode) "deg" else "rad",
                type = ButtonType.SCIENTIFIC,
                weight = 1f,
                height = 46.dp,
                fontSize = 14.sp,
                testTag = "btn_deg_rad"
            ) { onToggleDegree() }

            val sinText = if (isSecondFunction) "sin⁻¹" else "sin"
            val cosText = if (isSecondFunction) "cos⁻¹" else "cos"
            val tanText = if (isSecondFunction) "tan⁻¹" else "tan"

            KeyButton(text = sinText, type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 14.sp, testTag = "btn_sin") {
                onFunction(if (isSecondFunction) "asin" else "sin")
            }
            KeyButton(text = cosText, type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 14.sp, testTag = "btn_cos") {
                onFunction(if (isSecondFunction) "acos" else "cos")
            }
            KeyButton(text = tanText, type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 14.sp, testTag = "btn_tan") {
                onFunction(if (isSecondFunction) "atan" else "tan")
            }
        }

        // Row 2 (Power & Logs): xʸ, lg, ln, (, )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            val powText = if (isSecondFunction) "2ˣ" else "xʸ"
            KeyButton(text = powText, type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 14.sp, testTag = "btn_pow") {
                if (isSecondFunction) onFunction("2^") else onOperator("^")
            }
            val logText = if (isSecondFunction) "10ˣ" else "log"
            KeyButton(text = logText, type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 14.sp, testTag = "btn_log") {
                if (isSecondFunction) onFunction("10^") else onFunction("log")
            }
            KeyButton(text = "ln", type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 14.sp, testTag = "btn_ln") {
                if (isSecondFunction) onFunction("e^") else onFunction("ln")
            }
            KeyButton(text = "(", type = ButtonType.FUNCTION, weight = 1f, height = 46.dp, fontSize = 16.sp, testTag = "btn_sci_lparen") {
                onDigit("(")
            }
            KeyButton(text = ")", type = ButtonType.FUNCTION, weight = 1f, height = 46.dp, fontSize = 16.sp, testTag = "btn_sci_rparen") {
                onDigit(")")
            }
        }

        // Row 3 (Math Functions): √, x!, 1/x, π, e
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            val sqrtText = if (isSecondFunction) "x²" else "√"
            KeyButton(text = sqrtText, type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 15.sp, testTag = "btn_sqrt") {
                if (isSecondFunction) onSpecial("x²") else onFunction("sqrt")
            }
            KeyButton(text = "x!", type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 14.sp, testTag = "btn_fact") {
                onSpecial("!")
            }
            KeyButton(text = "1/x", type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 14.sp, testTag = "btn_inv") {
                onSpecial("1/x")
            }
            KeyButton(text = "π", type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 16.sp, testTag = "btn_pi") {
                onConstant("π")
            }
            KeyButton(text = "e", type = ButtonType.SCIENTIFIC, weight = 1f, height = 46.dp, fontSize = 16.sp, testTag = "btn_e") {
                onConstant("e")
            }
        }

        // Row 4 (Digits & Controls): C, %, ⌫, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            KeyButton(text = "C", type = ButtonType.FUNCTION, weight = 1.25f, height = 50.dp, fontSize = 18.sp, testTag = "btn_sci_ac") { onClear() }
            KeyButton(text = "%", type = ButtonType.FUNCTION, weight = 1.25f, height = 50.dp, fontSize = 18.sp, testTag = "btn_sci_pct") { onSpecial("%") }
            KeyButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                type = ButtonType.FUNCTION,
                weight = 1.25f,
                height = 50.dp,
                testTag = "btn_sci_bksp"
            ) { onBackspace() }
            KeyButton(text = "÷", type = ButtonType.OPERATOR, weight = 1.25f, height = 50.dp, fontSize = 24.sp, testTag = "btn_sci_div") { onOperator("÷") }
        }

        // Row 5: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            KeyButton(text = "7", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_7") { onDigit("7") }
            KeyButton(text = "8", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_8") { onDigit("8") }
            KeyButton(text = "9", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_9") { onDigit("9") }
            KeyButton(text = "×", type = ButtonType.OPERATOR, weight = 1f, height = 50.dp, fontSize = 24.sp, testTag = "btn_sci_mul") { onOperator("×") }
        }

        // Row 6: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            KeyButton(text = "4", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_4") { onDigit("4") }
            KeyButton(text = "5", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_5") { onDigit("5") }
            KeyButton(text = "6", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_6") { onDigit("6") }
            KeyButton(text = "−", type = ButtonType.OPERATOR, weight = 1f, height = 50.dp, fontSize = 24.sp, testTag = "btn_sci_sub") { onOperator("−") }
        }

        // Row 7: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            KeyButton(text = "1", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_1") { onDigit("1") }
            KeyButton(text = "2", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_2") { onDigit("2") }
            KeyButton(text = "3", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_3") { onDigit("3") }
            KeyButton(text = "+", type = ButtonType.OPERATOR, weight = 1f, height = 50.dp, fontSize = 24.sp, testTag = "btn_sci_add") { onOperator("+") }
        }

        // Row 8: ±, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            KeyButton(text = "±", type = ButtonType.FUNCTION, weight = 1f, height = 50.dp, fontSize = 18.sp, testTag = "btn_pm") { onSpecial("±") }
            KeyButton(text = "0", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 20.sp, testTag = "btn_sci_0") { onDigit("0") }
            KeyButton(text = ".", type = ButtonType.NUMBER, weight = 1f, height = 50.dp, fontSize = 24.sp, testTag = "btn_sci_dot") { onDecimal() }
            KeyButton(text = "=", type = ButtonType.EQUALS, weight = 1f, height = 50.dp, fontSize = 26.sp, testTag = "btn_sci_eq") { onEquals() }
        }
    }
}

@Composable
private fun RowScope.KeyButton(
    text: String? = null,
    icon: ImageVector? = null,
    type: ButtonType,
    weight: Float,
    isHighlighted: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    height: Dp = 60.dp,
    testTag: String,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val shape = RoundedCornerShape(24.dp)

    val (bgColor, contentColor) = when (type) {
        ButtonType.NUMBER -> {
            MaterialTheme.colorScheme.background to MaterialTheme.colorScheme.onBackground
        }
        ButtonType.FUNCTION -> {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
        ButtonType.OPERATOR -> {
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }
        ButtonType.EQUALS -> {
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        }
        ButtonType.SCIENTIFIC -> {
            if (isHighlighted) {
                MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
            }
        }
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(height)
            .clip(shape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = fontSize,
                    fontWeight = if (type == ButtonType.EQUALS) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}
