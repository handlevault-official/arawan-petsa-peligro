package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.BudgetUiState
import com.example.model.DailyCheckInStatus
import com.example.model.DateHelper
import com.example.model.ExpenseCategory
import com.example.model.ExpenseEntry
import com.example.model.UtangItem
import com.example.model.defaultTipidCuts
import com.example.model.defaultWisdomQuotes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.random.Random

private val Context.budgetDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "arawan_budget_prefs"
)

class BudgetRepository(private val context: Context) {

  private object Keys {
    val REMAINING_BALANCE = doublePreferencesKey("remaining_balance")
    val STARTING_BALANCE = doublePreferencesKey("starting_balance")
    val LOCKED_EMERGENCY_FUND = doublePreferencesKey("locked_emergency_fund")
    val TARGET_SAVINGS = doublePreferencesKey("target_savings")
    val HARD_DAILY_LIMIT = doublePreferencesKey("hard_daily_limit")
    val PAYDAY_EPOCH_DAY = longPreferencesKey("payday_epoch_day")
    val ACTIVE_CUTS_JSON = stringPreferencesKey("active_cuts_json")
    val UTANG_ITEMS_JSON = stringPreferencesKey("utang_items_json")
    val EXPENSES_JSON = stringPreferencesKey("expenses_json")
    val ROUND_UP_ENABLED = booleanPreferencesKey("round_up_enabled")
    val ROUND_UP_INCREMENT = intPreferencesKey("round_up_increment")
    val PAISA_PISO_JAR = doublePreferencesKey("paisa_piso_jar")
    val CHECK_IN_SAVED_TOTAL = doublePreferencesKey("check_in_saved_total")
    val STREAK_COUNT = intPreferencesKey("streak_count")
    val LAST_CHECK_IN_DAY = longPreferencesKey("last_check_in_day")
    val LAST_CHECK_IN_STATUS = stringPreferencesKey("last_check_in_status")
    val AI_COACH_ENABLED = booleanPreferencesKey("ai_coach_enabled")
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
  }

  val stateFlow: Flow<BudgetUiState> = context.budgetDataStore.data.map { prefs ->
    val today = DateHelper.todayEpochDay()
    val defaultPayday = today + 7L

    val remaining = prefs[Keys.REMAINING_BALANCE] ?: 3500.0
    val starting = prefs[Keys.STARTING_BALANCE] ?: 3500.0
    val lockedFund = prefs[Keys.LOCKED_EMERGENCY_FUND] ?: 0.0
    val targetSavings = prefs[Keys.TARGET_SAVINGS] ?: 0.0
    val hardDailyLimit = prefs[Keys.HARD_DAILY_LIMIT] ?: 200.0
    val payday = (prefs[Keys.PAYDAY_EPOCH_DAY] ?: defaultPayday).coerceAtLeast(today)
    val roundUpEnabled = prefs[Keys.ROUND_UP_ENABLED] ?: false
    val roundUpIncrement = prefs[Keys.ROUND_UP_INCREMENT] ?: 10
    val paisaPisoJar = prefs[Keys.PAISA_PISO_JAR] ?: 0.0
    val checkInSavedTotal = prefs[Keys.CHECK_IN_SAVED_TOTAL] ?: 0.0
    val streakCount = prefs[Keys.STREAK_COUNT] ?: 0
    val lastCheckInDay = prefs[Keys.LAST_CHECK_IN_DAY] ?: -1L
    val lastCheckInStatus = DailyCheckInStatus.entries.firstOrNull {
      it.name == (prefs[Keys.LAST_CHECK_IN_STATUS] ?: DailyCheckInStatus.NONE.name)
    } ?: DailyCheckInStatus.NONE
    val aiCoachEnabled = prefs[Keys.AI_COACH_ENABLED] ?: true
    val reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false
    val isDark = prefs[Keys.IS_DARK_MODE] ?: false

    val activeIds = parseStringSet(prefs[Keys.ACTIVE_CUTS_JSON])
    val cuts = defaultTipidCuts().map { cut ->
      cut.copy(isEnabled = activeIds.contains(cut.id))
    }

    val utangItems = parseUtangItems(prefs[Keys.UTANG_ITEMS_JSON])
    val expenses = parseExpenses(prefs[Keys.EXPENSES_JSON])

    BudgetUiState(
      remainingBalance = remaining,
      balanceInputText = formatEditable(remaining),
      startingBalance = starting,
      lockedEmergencyFund = lockedFund,
      lockedEmergencyInputText = if (lockedFund <= 0.0) "" else formatEditable(lockedFund),
      targetSavings = targetSavings,
      targetSavingsInputText = if (targetSavings <= 0.0) "" else formatEditable(targetSavings),
      hardDailyLimit = hardDailyLimit,
      hardDailyLimitInputText = if (hardDailyLimit <= 0.0) "" else formatEditable(hardDailyLimit),
      paydayEpochDay = payday,
      tipidCuts = cuts,
      utangItems = utangItems,
      expenses = expenses,
      roundUpEnabled = roundUpEnabled,
      roundUpIncrement = roundUpIncrement,
      paisaPisoJar = paisaPisoJar,
      checkInSavedTotal = checkInSavedTotal,
      streakCount = streakCount,
      lastCheckInEpochDay = lastCheckInDay,
      lastCheckInStatus = lastCheckInStatus,
      aiCoachEnabled = aiCoachEnabled,
      reminderEnabled = reminderEnabled,
      wisdomQuote = defaultWisdomQuotes().random(Random(System.currentTimeMillis())),
      isDarkMode = isDark,
      isLoaded = true,
    )
  }

  private fun formatEditable(value: Double): String {
    return if (value % 1.0 == 0.0) {
      value.toLong().toString()
    } else {
      String.format(Locale.US, "%.2f", value)
    }
  }

  suspend fun saveState(state: BudgetUiState) {
    context.budgetDataStore.edit { prefs ->
      prefs[Keys.REMAINING_BALANCE] = state.remainingBalance
      prefs[Keys.STARTING_BALANCE] = state.startingBalance
      prefs[Keys.LOCKED_EMERGENCY_FUND] = state.lockedEmergencyFund
      prefs[Keys.TARGET_SAVINGS] = state.targetSavings
      prefs[Keys.HARD_DAILY_LIMIT] = state.hardDailyLimit
      prefs[Keys.PAYDAY_EPOCH_DAY] = state.paydayEpochDay
      prefs[Keys.ROUND_UP_ENABLED] = state.roundUpEnabled
      prefs[Keys.ROUND_UP_INCREMENT] = state.roundUpIncrement
      prefs[Keys.PAISA_PISO_JAR] = state.paisaPisoJar
      prefs[Keys.CHECK_IN_SAVED_TOTAL] = state.checkInSavedTotal
      prefs[Keys.STREAK_COUNT] = state.streakCount
      prefs[Keys.LAST_CHECK_IN_DAY] = state.lastCheckInEpochDay
      prefs[Keys.LAST_CHECK_IN_STATUS] = state.lastCheckInStatus.name
      prefs[Keys.AI_COACH_ENABLED] = state.aiCoachEnabled
      prefs[Keys.REMINDER_ENABLED] = state.reminderEnabled
      prefs[Keys.IS_DARK_MODE] = state.isDarkMode
      prefs[Keys.ACTIVE_CUTS_JSON] = encodeStringList(
        state.tipidCuts.filter { it.isEnabled }.map { it.id }
      )
      prefs[Keys.UTANG_ITEMS_JSON] = encodeUtangItems(state.utangItems)
      prefs[Keys.EXPENSES_JSON] = encodeExpenses(state.expenses)
    }
  }

  suspend fun reset(keepDarkMode: Boolean) {
    context.budgetDataStore.edit { prefs ->
      prefs.clear()
      prefs[Keys.IS_DARK_MODE] = keepDarkMode
    }
  }

  private fun encodeStringList(items: List<String>): String {
    val array = JSONArray()
    items.forEach { array.put(it) }
    return array.toString()
  }

  private fun parseStringSet(raw: String?): Set<String> {
    if (raw.isNullOrBlank()) return emptySet()
    return try {
      val array = JSONArray(raw)
      buildSet {
        for (i in 0 until array.length()) {
          add(array.getString(i))
        }
      }
    } catch (_: Exception) {
      emptySet()
    }
  }

  private fun encodeUtangItems(items: List<UtangItem>): String {
    val array = JSONArray()
    items.forEach { item ->
      val obj = JSONObject().apply {
        put("id", item.id)
        put("title", item.title)
        put("amount", item.amount)
        put("isRecurring", item.isRecurring)
      }
      array.put(obj)
    }
    return array.toString()
  }

  private fun parseUtangItems(raw: String?): List<UtangItem> {
    if (raw.isNullOrBlank()) return emptyList()
    return try {
      val array = JSONArray(raw)
      buildList {
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          add(
            UtangItem(
              id = obj.optString("id", "$i"),
              title = obj.optString("title", ""),
              amount = obj.optDouble("amount", 0.0),
              isRecurring = obj.optBoolean("isRecurring", false),
            )
          )
        }
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun encodeExpenses(expenses: List<ExpenseEntry>): String {
    val array = JSONArray()
    expenses.forEach { entry ->
      val obj = JSONObject().apply {
        put("id", entry.id)
        put("amount", entry.amount)
        put("category", entry.category.name)
        put("note", entry.note)
        put("timestampMillis", entry.timestampMillis)
      }
      array.put(obj)
    }
    return array.toString()
  }

  private fun parseExpenses(raw: String?): List<ExpenseEntry> {
    if (raw.isNullOrBlank()) return emptyList()
    return try {
      val array = JSONArray(raw)
      buildList {
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val categoryName = obj.optString("category", ExpenseCategory.IBA_PA.name)
          val category = ExpenseCategory.entries.firstOrNull { it.name == categoryName }
            ?: ExpenseCategory.IBA_PA
          add(
            ExpenseEntry(
              id = obj.optString("id", "$i"),
              amount = obj.optDouble("amount", 0.0),
              category = category,
              note = obj.optString("note", ""),
              timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
            )
          )
        }
      }
    } catch (_: Exception) {
      emptyList()
    }
  }
}
