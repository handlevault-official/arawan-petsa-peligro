package com.example.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class SurvivalStatus(
  val title: String,
  val taglishHint: String,
  val emoji: String,
) {
  SAFE(
    title = "Safe • Luwag Pa",
    taglishHint = "Kaya pang huminga! Stick to your daily cap so you reach payday comfortably.",
    emoji = "✅",
  ),
  TIGHT(
    title = "Tight • Ingat",
    taglishHint = "Medyo manipis na. Prioritize pagkain at pamasahe, iwas muna sa budol.",
    emoji = "⚠️",
  ),
  CRITICAL(
    title = "Critical • Petsa De Peligro",
    taglishHint = "Survival mode ON! Turn on Tipid Cuts and stick strictly to essentials.",
    emoji = "🚨",
  ),
}

enum class ExpenseCategory(
  val label: String,
  val shortTag: String,
) {
  PAGKAIN("Pagkain", "Pagkain"),
  PAMASAHE_LOAD("Pamasahe/Load", "Pamasahe/Load"),
  BILLS("Bills", "Bills"),
  IBA_PA("Iba Pa", "Iba Pa"),
}

enum class DailyCheckInStatus {
  NONE,
  MET,
  EXCEEDED,
}

data class TipidCut(
  val id: String,
  val title: String,
  val subtitle: String,
  val dailySavings: Double,
  val isEnabled: Boolean = false,
)

data class UtangItem(
  val id: String,
  val title: String,
  val amount: Double,
  val isRecurring: Boolean = false,
)

data class ExpenseEntry(
  val id: String,
  val amount: Double,
  val category: ExpenseCategory,
  val note: String,
  val timestampMillis: Long,
)

data class BudgetUiState(
  val remainingBalance: Double = 3500.0,
  val balanceInputText: String = "3500",
  val startingBalance: Double = 3500.0,
  val lockedEmergencyFund: Double = 0.0,
  val lockedEmergencyInputText: String = "",
  val targetSavings: Double = 0.0,
  val targetSavingsInputText: String = "",
  val hardDailyLimit: Double = 200.0,
  val hardDailyLimitInputText: String = "200",
  val paydayEpochDay: Long = DateHelper.todayEpochDay() + 7L,
  val tipidCuts: List<TipidCut> = defaultTipidCuts(),
  val utangItems: List<UtangItem> = emptyList(),
  val expenses: List<ExpenseEntry> = emptyList(),
  val roundUpEnabled: Boolean = false,
  val roundUpIncrement: Int = 10,
  val paisaPisoJar: Double = 0.0,
  val checkInSavedTotal: Double = 0.0,
  val streakCount: Int = 0,
  val lastCheckInEpochDay: Long = -1L,
  val lastCheckInStatus: DailyCheckInStatus = DailyCheckInStatus.NONE,
  val aiCoachEnabled: Boolean = true,
  val reminderEnabled: Boolean = false,
  val wisdomQuote: String = defaultWisdomQuotes().random(Random(System.currentTimeMillis())),
  val isDarkMode: Boolean = false,
  val isLoaded: Boolean = false,
) {
  val todayEpochDay: Long
    get() = DateHelper.todayEpochDay()

  val daysRemaining: Int
    get() = max(1, (paydayEpochDay - todayEpochDay).toInt())

  val totalUtang: Double
    get() = utangItems.sumOf { it.amount }

  val dailySavingsNeeded: Double
    get() = (targetSavings / daysRemaining).coerceAtLeast(0.0)

  /**
   * Real spendable balance after subtracting Untouchable Emergency Fund, Utang/Bills obligations, and Target Savings.
   */
  val spendableBalance: Double
    get() = (remainingBalance - lockedEmergencyFund - totalUtang - targetSavings).coerceAtLeast(0.0)

  val isSavingsGoalExceedingBalance: Boolean
    get() = (lockedEmergencyFund + totalUtang + targetSavings) > remainingBalance

  val baseDailyAllowance: Double
    get() = (spendableBalance / daysRemaining).coerceAtLeast(0.0)

  val tipidDailyBoost: Double
    get() = tipidCuts.filter { it.isEnabled }.sumOf { it.dailySavings }

  val totalTipidSavings: Double
    get() = tipidDailyBoost * daysRemaining

  val extraSurvivalDays: Int
    get() {
      if (tipidDailyBoost <= 0.0 || spendableBalance <= 0.0) return 0
      val denominator = max(150.0, baseDailyAllowance)
      return (totalTipidSavings / denominator).toInt()
    }

  val weeklyBurnRate: Double
    get() = baseDailyAllowance * 7.0

  val totalSpent: Double
    get() = expenses.sumOf { it.amount }

  val effectiveCriticalThreshold: Double
    get() = if (hardDailyLimit > 0.0) hardDailyLimit else 200.0

  val isBelowHardLimit: Boolean
    get() = hardDailyLimit > 0.0 && baseDailyAllowance < hardDailyLimit

  val status: SurvivalStatus
    get() = when {
      remainingBalance <= 0.0 || spendableBalance <= 0.0 || baseDailyAllowance < effectiveCriticalThreshold -> SurvivalStatus.CRITICAL
      baseDailyAllowance < 500.0 -> SurvivalStatus.TIGHT
      else -> SurvivalStatus.SAFE
    }

  val survivalGaugeProgress: Float
    get() = (baseDailyAllowance / 700.0).toFloat().coerceIn(0.03f, 1f)

  val balanceRemainingRatio: Float
    get() {
      val base = max(startingBalance, remainingBalance).coerceAtLeast(1.0)
      return (remainingBalance / base).toFloat().coerceIn(0f, 1f)
    }

  val checkedInToday: Boolean
    get() = lastCheckInEpochDay == todayEpochDay

  val todayCheckInStatus: DailyCheckInStatus
    get() = if (checkedInToday) lastCheckInStatus else DailyCheckInStatus.NONE

  val totalSavingsAccumulated: Double
    get() = paisaPisoJar + checkInSavedTotal

  val savingsGoalProgressRatio: Float
    get() = if (targetSavings <= 0.0) 0f else (totalSavingsAccumulated / targetSavings).toFloat().coerceIn(0f, 1f)

  /**
   * Observed daily spend rate based on logged expenses, or daily allowance if no expenses logged yet.
   */
  val observedDailySpendRate: Double
    get() {
      if (expenses.isEmpty()) return baseDailyAllowance
      val distinctDays = expenses.map { DateHelper.millisToEpochDay(it.timestampMillis) }.distinct().size.coerceAtLeast(1)
      return totalSpent / distinctDays
    }

  /**
   * Projected balance by payday based on current spending trend (after locked fund & utang).
   */
  val projectedPaydayBalance: Double
    get() {
      val availableBeforeGoal = (remainingBalance - lockedEmergencyFund - totalUtang).coerceAtLeast(0.0)
      return availableBeforeGoal - (observedDailySpendRate * daysRemaining)
    }

  val projectedZeroDayOffset: Int?
    get() {
      if (observedDailySpendRate <= 0.0) return null
      val availableBeforeGoal = (remainingBalance - lockedEmergencyFund - totalUtang).coerceAtLeast(0.0)
      val daysToZero = availableBeforeGoal / observedDailySpendRate
      return if (daysToZero < daysRemaining) max(0, daysToZero.toInt()) else null
    }

  fun last7DaysSpendSeries(): List<Pair<String, Double>> {
    val today = todayEpochDay
    val grouped = expenses.groupBy { DateHelper.millisToEpochDay(it.timestampMillis) }
    return (6 downTo 0).map { offset ->
      val day = today - offset
      val label = DateHelper.formatShortWeekday(day)
      val sum = grouped[day]?.sumOf { it.amount } ?: 0.0
      label to sum
    }
  }

  fun categoryBreakdown(): List<Triple<ExpenseCategory, Double, Float>> {
    val total = totalSpent
    return ExpenseCategory.entries.map { cat ->
      val sum = expenses.filter { it.category == cat }.sumOf { it.amount }
      val ratio = if (total > 0.0) (sum / total).toFloat() else 0f
      Triple(cat, sum, ratio)
    }
  }

  fun expenseEpochDays(): Set<Long> {
    return expenses.map { DateHelper.millisToEpochDay(it.timestampMillis) }.toSet()
  }
}

fun computeRoundUpChange(amount: Double, increment: Int): Double {
  if (amount <= 0.0 || increment <= 0) return 0.0
  val rem = amount % increment.toDouble()
  return if (rem <= 0.0001) 0.0 else (increment.toDouble() - rem)
}

fun defaultTipidCuts(): List<TipidCut> = listOf(
  TipidCut(
    id = "iced_coffee",
    title = "Skip Iced Coffee / Milk Tea",
    subtitle = "3-in-1 or brewed sa bahay muna",
    dailySavings = 140.0,
    isEnabled = false,
  ),
  TipidCut(
    id = "cook_home",
    title = "Cook at Home Instead of Delivery",
    subtitle = "Iwas delivery fee & markup",
    dailySavings = 220.0,
    isEnabled = false,
  ),
  TipidCut(
    id = "impulse_buys",
    title = "Cut Impulse Buys / Checkout",
    subtitle = "Hold muna ang add-to-cart",
    dailySavings = 150.0,
    isEnabled = false,
  ),
  TipidCut(
    id = "commute_jeep",
    title = "Jeep / Lakad Over Ride-Hailing",
    subtitle = "Tipid pamasahe hanggang sahod",
    dailySavings = 180.0,
    isEnabled = false,
  ),
  TipidCut(
    id = "baon_lunch",
    title = "Baon Lunch & Water Tumbler",
    subtitle = "No bottled drinks or fast food",
    dailySavings = 120.0,
    isEnabled = false,
  ),
)

fun defaultWisdomQuotes(): List<String> = listOf(
  "Kahit maliit ang natira, basta disiplinado sa arawan, aabot ka sa sahod nang walang bagong utang!",
  "I-pause ang add-to-cart ng 48 hours—kung kailangan pa rin after two days, saka pag-isipan.",
  "Tubig sa tumbler + baon sa trabaho = instant ₱150+ tipid araw-araw para sa Emergency Fund.",
  "Hindi nakakahiya mag-tipid sa Petsa De Peligro; ang nakakahiya ay ang ma-zero bago mag-sahod.",
  "Treat your savings like a non-negotiable bill: itabi muna bago gumastos.",
  "Sa bawat barya sa Paisa-Piso Jar, lumalaki ang peace of mind mo for rainy days.",
  "Kung kaya lakarin ang short distance, lakarin na—tipid sa pamasahe, healthy pa!",
  "Mag-luto ng isang ulam na pang-dalawang kainan para bawas gas, oras, at gastos.",
  "Check your e-wallet bago mag-order ng delivery; madalas ang delivery fee ay pang-isang meal na.",
  "Small daily wins build big financial freedom. Keep your streak alive today!",
)

object PesoFormatter {
  private val decimalFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
  }

  private val exactFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
  }

  fun format(amount: Double, showDecimals: Boolean = false): String {
    val safe = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount.coerceAtLeast(0.0)
    val formatted = if (showDecimals) exactFormat.format(safe) else decimalFormat.format(safe)
    return "₱$formatted"
  }

  fun formatSigned(amount: Double): String {
    val prefix = if (amount < 0.0) "-₱" else "₱"
    val absVal = kotlin.math.abs(if (amount.isNaN() || amount.isInfinite()) 0.0 else amount)
    return "$prefix${decimalFormat.format(absVal)}"
  }
}

object DateHelper {
  private const val MILLIS_PER_DAY = 86_400_000L

  fun todayEpochDay(): Long {
    val cal = Calendar.getInstance()
    return calendarToEpochDay(cal)
  }

  fun millisToEpochDay(timestampMillis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    return calendarToEpochDay(cal)
  }

  private fun calendarToEpochDay(cal: Calendar): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
      clear()
      set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return utc.timeInMillis / MILLIS_PER_DAY
  }

  fun epochDayToUtcPickerMillis(epochDay: Long): Long = epochDay * MILLIS_PER_DAY

  fun utcPickerMillisToEpochDay(utcMillis: Long): Long = utcMillis / MILLIS_PER_DAY

  fun formatEpochDay(epochDay: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(epochDay * MILLIS_PER_DAY))
  }

  fun formatShortWeekday(epochDay: Long): String {
    val formatter = SimpleDateFormat("EEE", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(epochDay * MILLIS_PER_DAY))
  }

  fun formatMonthDay(epochDay: Long): String {
    val formatter = SimpleDateFormat("MMM d", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(epochDay * MILLIS_PER_DAY))
  }

  fun currentMonthHeaderAndDays(): Pair<String, List<Pair<Int, Long>>> {
    val now = Calendar.getInstance()
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.US).format(now.time)
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val year = now.get(Calendar.YEAR)
    val month = now.get(Calendar.MONTH)
    val days = (1..daysInMonth).map { dayOfMonth ->
      val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(year, month, dayOfMonth, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
      }
      dayOfMonth to (utc.timeInMillis / MILLIS_PER_DAY)
    }
    return monthLabel to days
  }

  fun formatTime(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("MMM d • h:mm a", Locale.US)
    return formatter.format(Date(timestampMillis))
  }

  fun formatCsvTimestamp(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    return formatter.format(Date(timestampMillis))
  }
}
