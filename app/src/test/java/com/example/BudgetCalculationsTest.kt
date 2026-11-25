package com.example

import com.example.model.BudgetUiState
import com.example.model.DateHelper
import com.example.model.PesoFormatter
import com.example.model.SurvivalStatus
import com.example.model.UtangItem
import com.example.model.computeRoundUpChange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetCalculationsTest {

  @Test
  fun `daily allowance and weekly burn rate compute accurately`() {
    val state = BudgetUiState(
      remainingBalance = 3500.0,
      paydayEpochDay = DateHelper.todayEpochDay() + 7L,
    )
    assertEquals(7, state.daysRemaining)
    assertEquals(500.0, state.baseDailyAllowance, 0.01)
    assertEquals(3500.0, state.weeklyBurnRate, 0.01)
    assertEquals(SurvivalStatus.SAFE, state.status)
  }

  @Test
  fun `petsa de peligro status switches between tight and critical`() {
    val tight = BudgetUiState(
      remainingBalance = 1500.0,
      paydayEpochDay = DateHelper.todayEpochDay() + 5L,
    )
    assertEquals(SurvivalStatus.TIGHT, tight.status)

    val critical = BudgetUiState(
      remainingBalance = 700.0,
      paydayEpochDay = DateHelper.todayEpochDay() + 5L,
    )
    assertEquals(SurvivalStatus.CRITICAL, critical.status)
  }

  @Test
  fun `tipid cuts increase daily cushion and extra survival days`() {
    val base = BudgetUiState(
      remainingBalance = 1400.0,
      paydayEpochDay = DateHelper.todayEpochDay() + 7L,
    )
    val withCuts = base.copy(
      tipidCuts = base.tipidCuts.map {
        if (it.id == "iced_coffee") it.copy(isEnabled = true) else it
      }
    )
    assertEquals(140.0, withCuts.tipidDailyBoost, 0.01)
    assertEquals(980.0, withCuts.totalTipidSavings, 0.01)
    assertTrue(withCuts.extraSurvivalDays >= 4)
    assertEquals("₱1,400", PesoFormatter.format(1400.0))
  }

  @Test
  fun `target savings calculates daily set-aside and remaining allowance`() {
    val state = BudgetUiState(
      remainingBalance = 3500.0,
      targetSavings = 700.0,
      paydayEpochDay = DateHelper.todayEpochDay() + 7L,
    )
    assertEquals(100.0, state.dailySavingsNeeded, 0.01)
    assertEquals(2800.0, state.spendableBalance, 0.01)
    assertEquals(400.0, state.baseDailyAllowance, 0.01)
    assertEquals(SurvivalStatus.TIGHT, state.status)
  }

  @Test
  fun `hard daily limit triggers critical warning earlier when allowance falls below it`() {
    val state = BudgetUiState(
      remainingBalance = 1400.0,
      hardDailyLimit = 300.0,
      paydayEpochDay = DateHelper.todayEpochDay() + 5L,
    )
    assertEquals(280.0, state.baseDailyAllowance, 0.01)
    assertEquals(true, state.isBelowHardLimit)
    assertEquals(SurvivalStatus.CRITICAL, state.status)
  }

  @Test
  fun `locked emergency fund and utang reduce real daily allowance`() {
    val state = BudgetUiState(
      remainingBalance = 4000.0,
      lockedEmergencyFund = 1000.0,
      utangItems = listOf(
        UtangItem(id = "1", title = "Meralco", amount = 500.0, isRecurring = true)
      ),
      paydayEpochDay = DateHelper.todayEpochDay() + 5L,
    )
    assertEquals(2500.0, state.spendableBalance, 0.01)
    assertEquals(500.0, state.baseDailyAllowance, 0.01)
  }

  @Test
  fun `round up change calculates loose change to nearest increment`() {
    assertEquals(5.0, computeRoundUpChange(145.0, 10), 0.01)
    assertEquals(0.0, computeRoundUpChange(150.0, 10), 0.01)
    assertEquals(5.0, computeRoundUpChange(145.0, 50), 0.01)
    assertEquals(35.0, computeRoundUpChange(115.0, 50), 0.01)
  }
}
