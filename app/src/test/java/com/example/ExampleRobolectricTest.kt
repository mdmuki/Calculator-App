package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.MathEvaluator
import com.example.data.local.CalculatorPreferences
import com.example.ui.CalculatorTab
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Calculator", appName)
  }

  @Test
  fun `test basic math operations`() {
    val eval1 = MathEvaluator.evaluate("15 + 25")
    assertTrue(eval1 is MathEvaluator.EvalResult.Success)
    assertEquals("40", (eval1 as MathEvaluator.EvalResult.Success).formatted)

    val eval2 = MathEvaluator.evaluate("100 - 30 * 2")
    assertTrue(eval2 is MathEvaluator.EvalResult.Success)
    assertEquals("40", (eval2 as MathEvaluator.EvalResult.Success).formatted)

    val eval3 = MathEvaluator.evaluate("50 + 10%")
    assertTrue(eval3 is MathEvaluator.EvalResult.Success)
    assertEquals("50.1", (eval3 as MathEvaluator.EvalResult.Success).formatted)
  }

  @Test
  fun `test scientific operations`() {
    val evalSin = MathEvaluator.evaluate("sin(30)", isDegreeMode = true)
    assertTrue(evalSin is MathEvaluator.EvalResult.Success)
    assertEquals("0.5", (evalSin as MathEvaluator.EvalResult.Success).formatted)

    val evalSqrt = MathEvaluator.evaluate("sqrt(144)")
    assertTrue(evalSqrt is MathEvaluator.EvalResult.Success)
    assertEquals("12", (evalSqrt as MathEvaluator.EvalResult.Success).formatted)

    val evalFact = MathEvaluator.evaluate("5!")
    assertTrue(evalFact is MathEvaluator.EvalResult.Success)
    assertEquals("120", (evalFact as MathEvaluator.EvalResult.Success).formatted)
  }

  @Test
  fun `test calculator state persistence across sessions`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = CalculatorPreferences(context)

    prefs.saveState(
      expression = "25 × 4 + 10",
      previewResult = "= 110",
      finalResult = "110",
      isDegreeMode = false,
      isSecondFunction = true,
      selectedTab = CalculatorTab.SCIENTIFIC,
      themeMode = ThemeMode.DARK
    )

    val loaded = prefs.loadState()
    assertEquals("25 × 4 + 10", loaded.expression)
    assertEquals("= 110", loaded.previewResult)
    assertEquals("110", loaded.finalResult)
    assertEquals(false, loaded.isDegreeMode)
    assertEquals(true, loaded.isSecondFunction)
    assertEquals(CalculatorTab.SCIENTIFIC, loaded.selectedTab)
    assertEquals(ThemeMode.DARK, loaded.themeMode)
  }
}
