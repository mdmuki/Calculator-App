package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

object MathEvaluator {

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    /**
     * Evaluates a mathematical expression string.
     * @param expression The mathematical expression (e.g., "5 + 3 × sin(30)")
     * @param isDegreeMode True if trig functions use Degrees, False for Radians
     */
    fun evaluate(expression: String, isDegreeMode: Boolean = true): EvalResult {
        if (expression.isBlank()) {
            return EvalResult.Error("Empty expression")
        }

        return try {
            val sanitized = sanitizeExpression(expression)
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) {
                return EvalResult.Error("Empty expression")
            }
            val parser = Parser(tokens, isDegreeMode)
            val result = parser.parse()
            if (result.isNaN()) {
                EvalResult.Error("Invalid input")
            } else if (result.isInfinite()) {
                EvalResult.Error("Cannot divide by 0")
            } else {
                EvalResult.Success(result, formatResult(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: Exception) {
            EvalResult.Error("Invalid format")
        }
    }

    private fun sanitizeExpression(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("π", "PI")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("Ans", "")
            .trim()
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Round nearly-zero values caused by trig approximations (e.g. sin(pi) or cos(pi/2))
        val normalized = if (abs(value) < 1e-14) 0.0 else value

        val absVal = abs(normalized)
        if (absVal != 0.0 && (absVal >= 1e12 || absVal < 1e-7)) {
            // Use scientific notation
            val df = java.text.DecimalFormat("0.######E0")
            df.roundingMode = java.math.RoundingMode.HALF_UP
            return df.format(normalized).replace("E", "e")
        }

        val bd = BigDecimal(normalized.toString())
            .setScale(10, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        return bd.toPlainString()
    }

    // Tokenizer
    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val op: Char) : Token()
        data class Function(val name: String) : Token()
        object OpenParen : Token()
        object CloseParen : Token()
        object Factorial : Token()
        object Percent : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]

            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    var hasDot = false
                    while (i < len && (expr[i].isDigit() || (expr[i] == '.' && !hasDot))) {
                        if (expr[i] == '.') hasDot = true
                        sb.append(expr[i])
                        i++
                    }
                    val numStr = sb.toString()
                    val num = if (numStr == ".") 0.0 else numStr.toDouble()
                    tokens.add(Token.Number(num))
                }
                c == '(' -> {
                    // Check implicit multiplication: e.g. 5(2) or )(
                    if (tokens.isNotEmpty()) {
                        val last = tokens.last()
                        if (last is Token.Number || last is Token.CloseParen || last is Token.Percent || last is Token.Factorial) {
                            tokens.add(Token.Operator('*'))
                        }
                    }
                    tokens.add(Token.OpenParen)
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.CloseParen)
                    i++
                }
                c == '!' -> {
                    tokens.add(Token.Factorial)
                    i++
                }
                c == '%' -> {
                    tokens.add(Token.Percent)
                    i++
                }
                c in "+-*/^" -> {
                    tokens.add(Token.Operator(c))
                    i++
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isLetter() || expr[i].isDigit())) {
                        sb.append(expr[i])
                        i++
                    }
                    val name = sb.toString()
                    when (name.lowercase()) {
                        "pi" -> {
                            if (tokens.isNotEmpty()) {
                                val last = tokens.last()
                                if (last is Token.Number || last is Token.CloseParen) {
                                    tokens.add(Token.Operator('*'))
                                }
                            }
                            tokens.add(Token.Number(Math.PI))
                        }
                        "e" -> {
                            if (tokens.isNotEmpty()) {
                                val last = tokens.last()
                                if (last is Token.Number || last is Token.CloseParen) {
                                    tokens.add(Token.Operator('*'))
                                }
                            }
                            tokens.add(Token.Number(Math.E))
                        }
                        else -> {
                            if (tokens.isNotEmpty()) {
                                val last = tokens.last()
                                if (last is Token.Number || last is Token.CloseParen) {
                                    tokens.add(Token.Operator('*'))
                                }
                            }
                            tokens.add(Token.Function(name.lowercase()))
                        }
                    }
                }
                else -> i++
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<Token>, private val isDegreeMode: Boolean) {
        private var pos = 0

        private fun peek(): Token? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): Token = tokens[pos++]

        fun parse(): Double {
            val res = parseExpression()
            if (pos < tokens.size) {
                throw IllegalArgumentException("Unexpected token")
            }
            return res
        }

        // Handles '+' and '-'
        private fun parseExpression(): Double {
            var left = parseTerm()

            while (true) {
                val token = peek()
                if (token is Token.Operator && (token.op == '+' || token.op == '-')) {
                    consume()
                    val right = parseTerm()
                    left = if (token.op == '+') left + right else left - right
                } else {
                    break
                }
            }
            return left
        }

        // Handles '*', '/'
        private fun parseTerm(): Double {
            var left = parsePower()

            while (true) {
                val token = peek()
                if (token is Token.Operator && (token.op == '*' || token.op == '/')) {
                    consume()
                    val right = parsePower()
                    if (token.op == '/') {
                        if (right == 0.0) throw ArithmeticException("Cannot divide by 0")
                        left /= right
                    } else {
                        left *= right
                    }
                } else {
                    break
                }
            }
            return left
        }

        // Handles '^' (power, right-associative)
        private fun parsePower(): Double {
            var base = parsePostfix()

            val token = peek()
            if (token is Token.Operator && token.op == '^') {
                consume()
                val exponent = parsePower() // right-associative
                base = base.pow(exponent)
            }
            return base
        }

        // Handles postfix operators: '!', '%'
        private fun parsePostfix(): Double {
            var value = parseFactor()

            while (true) {
                when (peek()) {
                    is Token.Factorial -> {
                        consume()
                        value = factorial(value)
                    }
                    is Token.Percent -> {
                        consume()
                        value /= 100.0
                    }
                    else -> break
                }
            }
            return value
        }

        // Handles unary operators, functions, numbers, parens
        private fun parseFactor(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

            // Unary + or -
            if (token is Token.Operator && (token.op == '+' || token.op == '-')) {
                consume()
                val factor = parseFactor()
                return if (token.op == '-') -factor else factor
            }

            // Function calls: sin, cos, tan, ln, log, sqrt, etc.
            if (token is Token.Function) {
                consume()
                val funcName = token.name
                val arg = parseFactor()
                return evaluateFunction(funcName, arg)
            }

            // Parentheses
            if (token is Token.OpenParen) {
                consume()
                val value = parseExpression()
                val next = peek()
                if (next is Token.CloseParen) {
                    consume()
                }
                return value
            }

            // Numbers
            if (token is Token.Number) {
                consume()
                return token.value
            }

            throw IllegalArgumentException("Invalid syntax at token: $token")
        }

        private fun evaluateFunction(func: String, x: Double): Double {
            return when (func) {
                "sin" -> {
                    val rad = if (isDegreeMode) Math.toRadians(x) else x
                    sin(rad)
                }
                "cos" -> {
                    val rad = if (isDegreeMode) Math.toRadians(x) else x
                    cos(rad)
                }
                "tan" -> {
                    val rad = if (isDegreeMode) Math.toRadians(x) else x
                    val cosVal = cos(rad)
                    if (abs(cosVal) < 1e-15) throw ArithmeticException("Undefined tan")
                    tan(rad)
                }
                "asin" -> {
                    if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error for asin")
                    val rad = asin(x)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "acos" -> {
                    if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error for acos")
                    val rad = acos(x)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "atan" -> {
                    val rad = atan(x)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "sinh" -> sinh(x)
                "cosh" -> cosh(x)
                "tanh" -> tanh(x)
                "ln" -> {
                    if (x <= 0) throw ArithmeticException("Domain error for ln")
                    ln(x)
                }
                "log", "log10" -> {
                    if (x <= 0) throw ArithmeticException("Domain error for log")
                    log10(x)
                }
                "sqrt" -> {
                    if (x < 0) throw ArithmeticException("Negative square root")
                    sqrt(x)
                }
                "cbrt" -> cbrt(x)
                "abs" -> abs(x)
                "exp" -> exp(x)
                else -> throw IllegalArgumentException("Unknown function: $func")
            }
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || floor(n) != n || n > 170) {
                throw ArithmeticException("Invalid factorial operand")
            }
            var res = 1.0
            val intVal = n.toLong()
            for (i in 2..intVal) {
                res *= i
            }
            return res
        }
    }
}
