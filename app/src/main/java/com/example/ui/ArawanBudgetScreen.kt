package com.example.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BudgetUiState
import com.example.model.ExpenseCategory
import com.example.model.PesoFormatter
import com.example.notifications.ReminderHelper
import com.example.ui.components.ExpenseLoggerCard
import com.example.ui.components.ForecastAndSavingsCard
import com.example.ui.components.LockedEmergencyBucketCard
import com.example.ui.components.SummaryAndSupportCard
import com.example.ui.components.SurvivalHeroCard
import com.example.ui.components.TipidCutsCard
import com.example.ui.components.UtangTrackerCard
import com.example.ui.components.WisdomAndCoachCard
import kotlinx.coroutines.launch

private const val KOFI_URL = "https://ko-fi.com/handlevaultofficial"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArawanBudgetScreen(
  state: BudgetUiState,
  summaryPreview: String,
  expensesCsv: String,
  onBalanceInputChange: (String) -> Unit,
  onAdjustBalance: (Double) -> Unit,
  onLockedFundInputChange: (String) -> Unit,
  onSelectQuickLockedFund: (Double) -> Unit,
  onTargetSavingsInputChange: (String) -> Unit,
  onSelectQuickSavings: (Double) -> Unit,
  onHardDailyLimitInputChange: (String) -> Unit,
  onSelectQuickHardLimit: (Double) -> Unit,
  onSelectQuickDays: (Int) -> Unit,
  onSelectPaydayMillis: (Long) -> Unit,
  onCheckInToday: (Boolean) -> Unit,
  onToggleTipidCut: (String) -> Unit,
  onAddUtang: (String, Double, Boolean) -> Unit,
  onRemoveUtang: (String) -> Unit,
  onToggleRoundUp: () -> Unit,
  onSelectRoundUpIncrement: (Int) -> Unit,
  onLogExpense: (Double, ExpenseCategory, String) -> Double,
  onUndoExpense: (String) -> Unit,
  onRefreshWisdom: () -> Unit,
  onToggleAiCoach: () -> Unit,
  onSetReminderEnabled: (Boolean) -> Unit,
  onToggleDarkMode: () -> Unit,
  onResetPlan: () -> Unit,
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  var showResetDialog by remember { mutableStateOf(false) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      onSetReminderEnabled(true)
      ReminderHelper.scheduleDailyReminder(context, enabled = true)
      scope.launch {
        snackbarHostState.showSnackbar("Daily 8 PM reminder enabled!")
      }
    } else {
      onSetReminderEnabled(false)
      scope.launch {
        snackbarHostState.showSnackbar("Notification permission denied. Enable it in Settings to get reminders.")
      }
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                  .padding(7.dp)
                  .size(20.dp),
              )
            }
            Column {
              Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = stringResource(R.string.subtitle_tagline),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = onToggleDarkMode,
            modifier = Modifier.testTag("theme_toggle_button"),
          ) {
            Icon(
              imageVector = if (state.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
              contentDescription = stringResource(R.string.cd_toggle_theme),
            )
          }
          IconButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.testTag("reset_plan_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.RestartAlt,
              contentDescription = stringResource(R.string.cd_reset_plan),
            )
          }
        },
      )
    },
  ) { innerPadding ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      val isWide = maxWidth >= 840.dp

      val handleLogExpense: (Double, ExpenseCategory, String) -> Unit = { amount, category, note ->
        val jarAdded = onLogExpense(amount, category, note)
        scope.launch {
          val msg = if (jarAdded > 0.0) {
            "Logged ${PesoFormatter.format(amount)} + saved ${PesoFormatter.format(jarAdded)} to Paisa-Piso Jar!"
          } else {
            "Logged ${PesoFormatter.format(amount)} under ${category.label}"
          }
          snackbarHostState.showSnackbar(msg)
        }
      }

      val handleToggleReminder: (Boolean) -> Unit = { enable ->
        if (!enable) {
          onSetReminderEnabled(false)
          ReminderHelper.scheduleDailyReminder(context, enabled = false)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          !ReminderHelper.hasNotificationPermission(context)
        ) {
          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
          onSetReminderEnabled(true)
          ReminderHelper.scheduleDailyReminder(context, enabled = true)
          scope.launch {
            snackbarHostState.showSnackbar("Daily 8 PM expense reminder scheduled!")
          }
        }
      }

      val handleSendTestReminder: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          !ReminderHelper.hasNotificationPermission(context)
        ) {
          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
          ReminderHelper.showDailyReminder(context)
          scope.launch {
            snackbarHostState.showSnackbar("Reminder notification sent!")
          }
        }
      }

      val handleCopySummary: () -> Unit = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Arawan Survival Summary", summaryPreview))
        scope.launch {
          snackbarHostState.showSnackbar("Survival summary copied! Ready to paste sa Notes or Messenger.")
        }
      }

      val handleExportCsv: () -> Unit = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Arawan Expenses CSV", expensesCsv))
        scope.launch {
          snackbarHostState.showSnackbar("Expense history CSV copied! Paste into Excel or Google Sheets.")
        }
      }

      val handleShareSummary: () -> Unit = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, summaryPreview)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Survival Summary"))
      }

      val handleOpenKofi: () -> Unit = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(KOFI_URL))
        context.startActivity(intent)
      }

      if (isWide) {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              verticalAlignment = Alignment.Top,
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
              ) {
                LockedEmergencyBucketCard(
                  state = state,
                  onLockedFundInputChange = onLockedFundInputChange,
                  onSelectQuickLockedFund = onSelectQuickLockedFund,
                )
                SurvivalHeroCard(
                  state = state,
                  onBalanceInputChange = onBalanceInputChange,
                  onAdjustBalance = onAdjustBalance,
                  onTargetSavingsInputChange = onTargetSavingsInputChange,
                  onSelectQuickSavings = onSelectQuickSavings,
                  onHardDailyLimitInputChange = onHardDailyLimitInputChange,
                  onSelectQuickHardLimit = onSelectQuickHardLimit,
                  onSelectQuickDays = onSelectQuickDays,
                  onSelectPaydayMillis = onSelectPaydayMillis,
                  onCheckInToday = onCheckInToday,
                )
                ForecastAndSavingsCard(state = state)
                WisdomAndCoachCard(
                  state = state,
                  onRefreshQuote = onRefreshWisdom,
                  onToggleAiCoach = onToggleAiCoach,
                  onToggleReminder = handleToggleReminder,
                  onSendTestReminder = handleSendTestReminder,
                )
              }
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
              ) {
                TipidCutsCard(
                  state = state,
                  onToggleCut = onToggleTipidCut,
                )
                UtangTrackerCard(
                  state = state,
                  onAddUtang = onAddUtang,
                  onRemoveUtang = onRemoveUtang,
                )
                ExpenseLoggerCard(
                  state = state,
                  onLogExpense = handleLogExpense,
                  onUndoExpense = onUndoExpense,
                  onExportCsv = handleExportCsv,
                  onToggleRoundUp = onToggleRoundUp,
                  onSelectRoundUpIncrement = onSelectRoundUpIncrement,
                )
                SummaryAndSupportCard(
                  summaryPreview = summaryPreview,
                  onCopySummary = handleCopySummary,
                  onShareSummary = handleShareSummary,
                  onOpenKofi = handleOpenKofi,
                )
              }
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 620.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          item {
            LockedEmergencyBucketCard(
              state = state,
              onLockedFundInputChange = onLockedFundInputChange,
              onSelectQuickLockedFund = onSelectQuickLockedFund,
            )
          }
          item {
            SurvivalHeroCard(
              state = state,
              onBalanceInputChange = onBalanceInputChange,
              onAdjustBalance = onAdjustBalance,
              onTargetSavingsInputChange = onTargetSavingsInputChange,
              onSelectQuickSavings = onSelectQuickSavings,
              onHardDailyLimitInputChange = onHardDailyLimitInputChange,
              onSelectQuickHardLimit = onSelectQuickHardLimit,
              onSelectQuickDays = onSelectQuickDays,
              onSelectPaydayMillis = onSelectPaydayMillis,
              onCheckInToday = onCheckInToday,
            )
          }
          item {
            ForecastAndSavingsCard(state = state)
          }
          item {
            WisdomAndCoachCard(
              state = state,
              onRefreshQuote = onRefreshWisdom,
              onToggleAiCoach = onToggleAiCoach,
              onToggleReminder = handleToggleReminder,
              onSendTestReminder = handleSendTestReminder,
            )
          }
          item {
            TipidCutsCard(
              state = state,
              onToggleCut = onToggleTipidCut,
            )
          }
          item {
            UtangTrackerCard(
              state = state,
              onAddUtang = onAddUtang,
              onRemoveUtang = onRemoveUtang,
            )
          }
          item {
            ExpenseLoggerCard(
              state = state,
              onLogExpense = handleLogExpense,
              onUndoExpense = onUndoExpense,
              onExportCsv = handleExportCsv,
              onToggleRoundUp = onToggleRoundUp,
              onSelectRoundUpIncrement = onSelectRoundUpIncrement,
            )
          }
          item {
            SummaryAndSupportCard(
              summaryPreview = summaryPreview,
              onCopySummary = handleCopySummary,
              onShareSummary = handleShareSummary,
              onOpenKofi = handleOpenKofi,
            )
          }
          item {
            Spacer(modifier = Modifier.height(12.dp))
          }
        }
      }
    }
  }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text(stringResource(R.string.reset_title)) },
      text = { Text(stringResource(R.string.reset_body)) },
      confirmButton = {
        TextButton(
          onClick = {
            showResetDialog = false
            ReminderHelper.scheduleDailyReminder(context, enabled = false)
            onResetPlan()
            scope.launch {
              snackbarHostState.showSnackbar("Survival plan reset to defaults.")
            }
          },
          modifier = Modifier.testTag("confirm_reset_button"),
        ) {
          Text(
            text = stringResource(R.string.reset_confirm),
            color = MaterialTheme.colorScheme.error,
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text(stringResource(R.string.cancel))
        }
      },
    )
  }
}
