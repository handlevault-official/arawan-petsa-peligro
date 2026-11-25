package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BudgetRepository
import com.example.ui.ArawanBudgetScreen
import com.example.ui.BudgetViewModel
import com.example.ui.theme.ArawanBudgetTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val repository = BudgetRepository(applicationContext)

    setContent {
      val viewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModel.Factory(repository)
      )
      val state by viewModel.uiState.collectAsStateWithLifecycle()

      ArawanBudgetTheme(darkTheme = state.isDarkMode) {
        ArawanBudgetScreen(
          state = state,
          summaryPreview = viewModel.buildSurvivalSummary(),
          expensesCsv = viewModel.buildExpensesCsv(),
          onBalanceInputChange = viewModel::onBalanceInputChange,
          onAdjustBalance = viewModel::adjustBalance,
          onLockedFundInputChange = viewModel::onLockedFundInputChange,
          onSelectQuickLockedFund = viewModel::selectQuickLockedFund,
          onTargetSavingsInputChange = viewModel::onTargetSavingsInputChange,
          onSelectQuickSavings = viewModel::selectQuickSavings,
          onHardDailyLimitInputChange = viewModel::onHardDailyLimitInputChange,
          onSelectQuickHardLimit = viewModel::selectQuickHardLimit,
          onSelectQuickDays = viewModel::selectQuickDays,
          onSelectPaydayMillis = viewModel::selectPaydayFromUtcMillis,
          onCheckInToday = viewModel::checkInToday,
          onToggleTipidCut = viewModel::toggleTipidCut,
          onAddUtang = viewModel::addUtangItem,
          onRemoveUtang = viewModel::removeUtangItem,
          onToggleRoundUp = viewModel::toggleRoundUpSavings,
          onSelectRoundUpIncrement = viewModel::selectRoundUpIncrement,
          onLogExpense = viewModel::logExpense,
          onUndoExpense = viewModel::undoExpense,
          onRefreshWisdom = viewModel::refreshWisdomQuote,
          onToggleAiCoach = viewModel::toggleAiCoach,
          onSetReminderEnabled = viewModel::setReminderEnabled,
          onToggleDarkMode = viewModel::toggleDarkMode,
          onResetPlan = viewModel::resetPlan,
        )
      }
    }
  }
}
