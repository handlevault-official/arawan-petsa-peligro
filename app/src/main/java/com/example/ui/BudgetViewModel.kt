package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BudgetRepository
import com.example.model.BudgetUiState
import com.example.model.DailyCheckInStatus
import com.example.model.DateHelper
import com.example.model.ExpenseCategory
import com.example.model.ExpenseEntry
import com.example.model.PesoFormatter
import com.example.model.UtangItem
import com.example.model.computeRoundUpChange
import com.example.model.defaultWisdomQuotes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.max

class BudgetViewModel(
  private val repository: BudgetRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(BudgetUiState())
  val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      val initial = repository.stateFlow.first()
      _uiState.value = initial
    }
  }

  private fun sanitizeDecimalInput(rawText: String): String {
    return buildString {
      var dotSeen = false
      for (ch in rawText) {
        if (ch.isDigit()) {
          append(ch)
        } else if (ch == '.' && !dotSeen) {
          dotSeen = true
          append(ch)
        }
      }
    }
  }

  fun onBalanceInputChange(rawText: String) {
    val filtered = sanitizeDecimalInput(rawText)
    val parsed = filtered.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    updateAndPersist { current ->
      current.copy(
        balanceInputText = filtered,
        remainingBalance = parsed,
        startingBalance = max(current.startingBalance, parsed + current.totalSpent),
      )
    }
  }

  fun adjustBalance(delta: Double) {
    updateAndPersist { current ->
      val next = (current.remainingBalance + delta).coerceAtLeast(0.0)
      current.copy(
        remainingBalance = next,
        balanceInputText = formatEditableNumber(next),
        startingBalance = max(current.startingBalance, next + current.totalSpent),
      )
    }
  }

  fun onLockedFundInputChange(rawText: String) {
    val filtered = sanitizeDecimalInput(rawText)
    val parsed = filtered.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    updateAndPersist { current ->
      current.copy(
        lockedEmergencyInputText = filtered,
        lockedEmergencyFund = parsed,
      )
    }
  }

  fun selectQuickLockedFund(amount: Double) {
    val safe = amount.coerceAtLeast(0.0)
    updateAndPersist { current ->
      current.copy(
        lockedEmergencyFund = safe,
        lockedEmergencyInputText = if (safe == 0.0) "" else formatEditableNumber(safe),
      )
    }
  }

  fun onTargetSavingsInputChange(rawText: String) {
    val filtered = sanitizeDecimalInput(rawText)
    val parsed = filtered.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    updateAndPersist { current ->
      current.copy(
        targetSavingsInputText = filtered,
        targetSavings = parsed,
      )
    }
  }

  fun selectQuickSavings(amount: Double) {
    val safe = amount.coerceAtLeast(0.0)
    updateAndPersist { current ->
      current.copy(
        targetSavings = safe,
        targetSavingsInputText = if (safe == 0.0) "" else formatEditableNumber(safe),
      )
    }
  }

  fun onHardDailyLimitInputChange(rawText: String) {
    val filtered = sanitizeDecimalInput(rawText)
    val parsed = filtered.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    updateAndPersist { current ->
      current.copy(
        hardDailyLimitInputText = filtered,
        hardDailyLimit = parsed,
      )
    }
  }

  fun selectQuickHardLimit(limit: Double) {
    val safe = limit.coerceAtLeast(0.0)
    updateAndPersist { current ->
      current.copy(
        hardDailyLimit = safe,
        hardDailyLimitInputText = if (safe == 0.0) "" else formatEditableNumber(safe),
      )
    }
  }

  fun selectQuickDays(days: Int) {
    val targetEpoch = DateHelper.todayEpochDay() + days.toLong().coerceAtLeast(1L)
    updateAndPersist { current ->
      current.copy(paydayEpochDay = targetEpoch)
    }
  }

  fun selectPaydayFromUtcMillis(utcMillis: Long) {
    val pickedEpoch = DateHelper.utcPickerMillisToEpochDay(utcMillis)
    val clamped = max(DateHelper.todayEpochDay(), pickedEpoch)
    updateAndPersist { current ->
      current.copy(paydayEpochDay = clamped)
    }
  }

  fun toggleTipidCut(cutId: String) {
    updateAndPersist { current ->
      current.copy(
        tipidCuts = current.tipidCuts.map { cut ->
          if (cut.id == cutId) cut.copy(isEnabled = !cut.isEnabled) else cut
        }
      )
    }
  }

  fun addUtangItem(title: String, amount: Double, isRecurring: Boolean) {
    if (title.isBlank() || amount <= 0.0) return
    val item = UtangItem(
      id = UUID.randomUUID().toString(),
      title = title.trim(),
      amount = amount,
      isRecurring = isRecurring,
    )
    updateAndPersist { current ->
      current.copy(utangItems = listOf(item) + current.utangItems)
    }
  }

  fun removeUtangItem(itemId: String) {
    updateAndPersist { current ->
      current.copy(utangItems = current.utangItems.filterNot { it.id == itemId })
    }
  }

  fun toggleRoundUpSavings() {
    updateAndPersist { current ->
      current.copy(roundUpEnabled = !current.roundUpEnabled)
    }
  }

  fun selectRoundUpIncrement(increment: Int) {
    updateAndPersist { current ->
      current.copy(roundUpIncrement = increment)
    }
  }

  fun logExpense(amount: Double, category: ExpenseCategory, note: String): Double {
    if (amount <= 0.0) return 0.0
    val entry = ExpenseEntry(
      id = UUID.randomUUID().toString(),
      amount = amount,
      category = category,
      note = note.trim(),
      timestampMillis = System.currentTimeMillis(),
    )
    val current = _uiState.value
    val roundUpChange = if (current.roundUpEnabled) {
      computeRoundUpChange(amount, current.roundUpIncrement)
    } else {
      0.0
    }
    updateAndPersist { state ->
      val nextBalance = (state.remainingBalance - amount - roundUpChange).coerceAtLeast(0.0)
      state.copy(
        remainingBalance = nextBalance,
        balanceInputText = formatEditableNumber(nextBalance),
        paisaPisoJar = state.paisaPisoJar + roundUpChange,
        expenses = listOf(entry) + state.expenses,
      )
    }
    return roundUpChange
  }

  fun undoExpense(expenseId: String) {
    updateAndPersist { current ->
      val target = current.expenses.firstOrNull { it.id == expenseId } ?: return@updateAndPersist current
      val restoredBalance = current.remainingBalance + target.amount
      current.copy(
        remainingBalance = restoredBalance,
        balanceInputText = formatEditableNumber(restoredBalance),
        expenses = current.expenses.filterNot { it.id == expenseId },
      )
    }
  }

  fun checkInToday(metAllowance: Boolean) {
    val today = DateHelper.todayEpochDay()
    updateAndPersist { current ->
      if (current.lastCheckInEpochDay == today &&
        ((metAllowance && current.lastCheckInStatus == DailyCheckInStatus.MET) ||
          (!metAllowance && current.lastCheckInStatus == DailyCheckInStatus.EXCEEDED))
      ) {
        return@updateAndPersist current
      }

      val nextStatus = if (metAllowance) DailyCheckInStatus.MET else DailyCheckInStatus.EXCEEDED
      val wasMetToday = current.lastCheckInEpochDay == today && current.lastCheckInStatus == DailyCheckInStatus.MET

      val nextStreak = if (metAllowance) {
        if (current.lastCheckInEpochDay == today - 1L || current.lastCheckInEpochDay == today) {
          if (wasMetToday) current.streakCount else current.streakCount + 1
        } else {
          1
        }
      } else {
        0
      }

      val savedDelta = when {
        metAllowance && !wasMetToday -> current.dailySavingsNeeded
        !metAllowance && wasMetToday -> -current.dailySavingsNeeded
        else -> 0.0
      }

      current.copy(
        streakCount = nextStreak,
        lastCheckInEpochDay = today,
        lastCheckInStatus = nextStatus,
        checkInSavedTotal = (current.checkInSavedTotal + savedDelta).coerceAtLeast(0.0),
      )
    }
  }

  fun refreshWisdomQuote() {
    val quotes = defaultWisdomQuotes()
    val current = _uiState.value.wisdomQuote
    val next = quotes.filterNot { it == current }.randomOrNull() ?: quotes.first()
    _uiState.value = _uiState.value.copy(wisdomQuote = next)
  }

  fun toggleAiCoach() {
    updateAndPersist { current ->
      current.copy(aiCoachEnabled = !current.aiCoachEnabled)
    }
  }

  fun setReminderEnabled(enabled: Boolean) {
    updateAndPersist { current ->
      current.copy(reminderEnabled = enabled)
    }
  }

  fun toggleDarkMode() {
    updateAndPersist { current ->
      current.copy(isDarkMode = !current.isDarkMode)
    }
  }

  fun resetPlan() {
    val keepDark = _uiState.value.isDarkMode
    viewModelScope.launch {
      repository.reset(keepDarkMode = keepDark)
      _uiState.value = BudgetUiState(isDarkMode = keepDark, isLoaded = true)
    }
  }

  fun buildSurvivalSummary(): String {
    val s = _uiState.value
    val activeCuts = s.tipidCuts.filter { it.isEnabled }
    val cutsLine = if (activeCuts.isEmpty()) {
      "Tipid Cuts: None active yet"
    } else {
      "Tipid Cuts (${PesoFormatter.format(s.tipidDailyBoost)}/day saved): " +
        activeCuts.joinToString(", ") { it.title }
    }
    val extraDaysLine = if (s.extraSurvivalDays > 0) {
      "\nExtra Survival Gained: +${s.extraSurvivalDays} day(s)"
    } else {
      ""
    }
    return buildString {
      appendLine("🛡️ ARAWAN BUDGET • PETSA DE PELIGRO SUMMARY")
      appendLine("Status: ${s.status.emoji} ${s.status.title} (Streak: ${s.streakCount}d)")
      appendLine("Total Balance: ${PesoFormatter.format(s.remainingBalance, showDecimals = true)}")
      if (s.lockedEmergencyFund > 0.0) {
        appendLine("Locked Emergency Fund (Untouchable): ${PesoFormatter.format(s.lockedEmergencyFund)}")
      }
      if (s.totalUtang > 0.0) {
        appendLine("Utang/Bills Reserved: ${PesoFormatter.format(s.totalUtang)}")
      }
      appendLine("Days Left Until Sahod: ${s.daysRemaining} day(s) (${DateHelper.formatEpochDay(s.paydayEpochDay)})")
      appendLine("Real Safe Daily Allowance: ${PesoFormatter.format(s.baseDailyAllowance, showDecimals = true)}/day")
      if (s.targetSavings > 0.0) {
        appendLine("Target Savings Goal: ${PesoFormatter.format(s.targetSavings, showDecimals = true)} (Set aside ${PesoFormatter.format(s.dailySavingsNeeded, showDecimals = true)}/day)")
      }
      if (s.paisaPisoJar > 0.0) {
        appendLine("Paisa-Piso Loose Change Jar: ${PesoFormatter.format(s.paisaPisoJar, showDecimals = true)}")
      }
      if (s.hardDailyLimit > 0.0) {
        appendLine("Hard Daily Limit: ${PesoFormatter.format(s.hardDailyLimit)}/day${if (s.isBelowHardLimit) " (BELOW LIMIT!)" else ""}")
      }
      appendLine("Weekly Burn Pace: ${PesoFormatter.format(s.weeklyBurnRate)}/week")
      appendLine(cutsLine + extraDaysLine)
      if (s.totalSpent > 0) {
        appendLine("Total Logged Spend: ${PesoFormatter.format(s.totalSpent, showDecimals = true)}")
      }
      append("Kaya natin 'to hanggang next sahod! 💪")
    }
  }

  fun buildExpensesCsv(): String {
    val expenses = _uiState.value.expenses
    return buildString {
      appendLine("Date,Category,Note,Amount (PHP)")
      expenses.forEach { entry ->
        val date = csvEscape(DateHelper.formatCsvTimestamp(entry.timestampMillis))
        val category = csvEscape(entry.category.label)
        val note = csvEscape(entry.note)
        val amount = String.format(Locale.US, "%.2f", entry.amount)
        appendLine("$date,$category,$note,$amount")
      }
    }.trimEnd()
  }

  private fun csvEscape(value: String): String {
    val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n')
    if (!needsQuotes) return value
    return "\"${value.replace("\"", "\"\"")}\""
  }

  private fun updateAndPersist(transform: (BudgetUiState) -> BudgetUiState) {
    val updated = transform(_uiState.value)
    _uiState.value = updated
    viewModelScope.launch {
      repository.saveState(updated)
    }
  }

  private fun formatEditableNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
      value.toLong().toString()
    } else {
      String.format(Locale.US, "%.2f", value)
    }
  }

  class Factory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return BudgetViewModel(repository) as T
    }
  }
}
