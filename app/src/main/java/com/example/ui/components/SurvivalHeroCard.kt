package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BudgetUiState
import com.example.model.DailyCheckInStatus
import com.example.model.DateHelper
import com.example.model.PesoFormatter
import com.example.model.SurvivalStatus
import com.example.ui.theme.LocalSurvivalPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurvivalHeroCard(
  state: BudgetUiState,
  onBalanceInputChange: (String) -> Unit,
  onAdjustBalance: (Double) -> Unit,
  onTargetSavingsInputChange: (String) -> Unit,
  onSelectQuickSavings: (Double) -> Unit,
  onHardDailyLimitInputChange: (String) -> Unit,
  onSelectQuickHardLimit: (Double) -> Unit,
  onSelectQuickDays: (Int) -> Unit,
  onSelectPaydayMillis: (Long) -> Unit,
  onCheckInToday: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalSurvivalPalette.current
  var showDatePicker by remember { mutableStateOf(false) }

  val targetStatusColor = when (state.status) {
    SurvivalStatus.SAFE -> palette.safeColor
    SurvivalStatus.TIGHT -> palette.tightColor
    SurvivalStatus.CRITICAL -> palette.criticalColor
  }
  val targetStatusBg = when (state.status) {
    SurvivalStatus.SAFE -> palette.safeContainer
    SurvivalStatus.TIGHT -> palette.tightContainer
    SurvivalStatus.CRITICAL -> palette.criticalContainer
  }
  val targetOnStatusBg = when (state.status) {
    SurvivalStatus.SAFE -> palette.onSafeContainer
    SurvivalStatus.TIGHT -> palette.onTightContainer
    SurvivalStatus.CRITICAL -> palette.onCriticalContainer
  }

  val statusColor by animateColorAsState(
    targetValue = targetStatusColor,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "statusColor",
  )
  val statusBg by animateColorAsState(
    targetValue = targetStatusBg,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "statusBg",
  )
  val gaugeProgress by animateFloatAsState(
    targetValue = state.survivalGaugeProgress,
    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
    label = "gaugeProgress",
  )
  val runwayRatio by animateFloatAsState(
    targetValue = state.balanceRemainingRatio,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "runwayRatio",
  )

  val isCriticalGlow = state.status == SurvivalStatus.CRITICAL
  val cardBorderColor by animateColorAsState(
    targetValue = if (isCriticalGlow) palette.criticalColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "cardBorderColor",
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .then(
        if (isCriticalGlow) {
          Modifier.shadow(
            elevation = 12.dp,
            shape = MaterialTheme.shapes.large,
            ambientColor = palette.criticalColor,
            spotColor = palette.criticalColor,
          )
        } else {
          Modifier
        }
      ),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(if (isCriticalGlow) 1.8.dp else 1.dp, cardBorderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Status Badge + Days Left Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          color = statusBg,
          shape = RoundedCornerShape(50),
          modifier = Modifier.testTag("status_badge"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = when (state.status) {
                SurvivalStatus.SAFE -> Icons.Filled.CheckCircle
                SurvivalStatus.TIGHT -> Icons.Filled.Warning
                SurvivalStatus.CRITICAL -> Icons.Filled.Error
              },
              contentDescription = null,
              tint = targetOnStatusBg,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = state.status.title,
              style = MaterialTheme.typography.labelLarge,
              color = targetOnStatusBg,
            )
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            color = palette.tightContainer,
            shape = RoundedCornerShape(50),
            modifier = Modifier.testTag("streak_badge"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = "Allowance streak",
                tint = palette.onTightContainer,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = "${state.streakCount}d streak",
                style = MaterialTheme.typography.labelSmall,
                color = palette.onTightContainer,
              )
            }
          }

          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(50),
            modifier = Modifier.testTag("paisa_piso_badge"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Savings,
                contentDescription = "Paisa-Piso jar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = PesoFormatter.format(state.paisaPisoJar),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
      }

      // Main Safe Daily Allowance + Circular Survival Arc
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .drawBehind {
            if (isCriticalGlow) {
              val glowColor = palette.criticalColor
              drawRoundRect(
                color = glowColor.copy(alpha = 0.16f),
                topLeft = Offset(-4.dp.toPx(), -4.dp.toPx()),
                size = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
                cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
              )
            }
          }
          .clip(MaterialTheme.shapes.medium)
          .background(
            Brush.linearGradient(
              colors = listOf(
                statusColor.copy(alpha = if (isCriticalGlow) 0.22f else 0.12f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
              )
            )
          )
          .then(
            if (isCriticalGlow) {
              Modifier.border(
                width = 1.5.dp,
                color = palette.criticalColor.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.medium,
              )
            } else {
              Modifier
            }
          )
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "SAFE ARAWAN ALLOWANCE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "${PesoFormatter.format(state.baseDailyAllowance)}/day",
            style = MaterialTheme.typography.displayMedium,
            color = if (isCriticalGlow) palette.criticalColor else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("daily_allowance_text"),
          )
          if (state.isBelowHardLimit) {
            Text(
              text = "Below your ${PesoFormatter.format(state.hardDailyLimit)}/day hard limit!",
              style = MaterialTheme.typography.labelMedium,
              color = palette.criticalColor,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("hard_limit_warning_text"),
            )
          }
          if (state.targetSavings > 0.0) {
            Text(
              text = "Set aside ${PesoFormatter.format(state.dailySavingsNeeded)}/day to save ${PesoFormatter.format(state.targetSavings)}",
              style = MaterialTheme.typography.labelMedium,
              color = if (state.isSavingsGoalExceedingBalance) palette.criticalColor else MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.testTag("daily_savings_needed_text"),
            )
          }
          if (state.tipidDailyBoost > 0.0) {
            Text(
              text = "+${PesoFormatter.format(state.tipidDailyBoost)}/day freed via Tipid Cuts",
              style = MaterialTheme.typography.labelMedium,
              color = palette.safeColor,
              fontWeight = FontWeight.SemiBold,
            )
          } else if (state.targetSavings <= 0.0 && !state.isBelowHardLimit) {
            Text(
              text = state.status.taglishHint,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Custom Canvas Radial Survival Gauge
        Box(
          modifier = Modifier.size(76.dp),
          contentAlignment = Alignment.Center,
        ) {
          val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
          Canvas(modifier = Modifier.size(72.dp)) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
              color = trackColor,
              startAngle = 135f,
              sweepAngle = 270f,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
              color = statusColor,
              startAngle = 135f,
              sweepAngle = 270f * gaugeProgress,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${state.daysRemaining}",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = if (state.daysRemaining == 1) "DAY" else "DAYS",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Horizontal Runway Progress Bar + Weekly Burn Rate
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Weekly Burn Pace: ${PesoFormatter.format(state.weeklyBurnRate)}/wk",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("weekly_burn_text"),
          )
          Text(
            text = "${(runwayRatio * 100).toInt()}% stash left",
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
          )
        }
        val barTrack = MaterialTheme.colorScheme.surfaceVariant
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
        ) {
          drawRoundRect(
            color = barTrack,
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
          )
          drawRoundRect(
            color = statusColor,
            size = Size(width = size.width * runwayRatio.coerceAtLeast(0.02f), height = size.height),
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
          )
        }
        if (state.lockedEmergencyFund > 0.0 || state.totalUtang > 0.0) {
          Text(
            text = "Real spendable balance: ${PesoFormatter.format(state.spendableBalance)} (after untouchable fund, utang & goal)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Daily Check-In Bar
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Daily Check-In",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = when (state.todayCheckInStatus) {
                DailyCheckInStatus.MET -> "Logged: Met Allowance ✓"
                DailyCheckInStatus.EXCEEDED -> "Logged: Exceeded today"
                DailyCheckInStatus.NONE -> "Did you stick to today's limit?"
              },
              style = MaterialTheme.typography.labelSmall,
              color = when (state.todayCheckInStatus) {
                DailyCheckInStatus.MET -> palette.safeColor
                DailyCheckInStatus.EXCEEDED -> palette.criticalColor
                DailyCheckInStatus.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
              },
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = { onCheckInToday(true) },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (state.todayCheckInStatus == DailyCheckInStatus.MET) palette.safeColor else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (state.todayCheckInStatus == DailyCheckInStatus.MET) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              shape = MaterialTheme.shapes.small,
              modifier = Modifier
                .weight(1f)
                .testTag("checkin_met_button"),
            ) {
              Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Met Allowance", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
              onClick = { onCheckInToday(false) },
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (state.todayCheckInStatus == DailyCheckInStatus.EXCEEDED) palette.criticalContainer else Color.Transparent,
                contentColor = if (state.todayCheckInStatus == DailyCheckInStatus.EXCEEDED) palette.onCriticalContainer else MaterialTheme.colorScheme.onSurface,
              ),
              shape = MaterialTheme.shapes.small,
              modifier = Modifier
                .weight(1f)
                .testTag("checkin_exceeded_button"),
            ) {
              Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Exceeded", style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }

      // Remaining Balance Input + Quick Adjust Chips
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Remaining Cash / E-Wallet (₱)",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
          value = state.balanceInputText,
          onValueChange = onBalanceInputChange,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("balance_input"),
          prefix = {
            Text(
              text = "₱ ",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.primary,
            )
          },
          textStyle = MaterialTheme.typography.titleLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
          ),
          placeholder = { Text("0") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          shape = MaterialTheme.shapes.small,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
          ),
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(-100.0 to "-₱100", 100.0 to "+₱100", 500.0 to "+₱500", 1000.0 to "+₱1,000").forEach { (delta, label) ->
            AssistChip(
              onClick = { onAdjustBalance(delta) },
              label = { Text(label, style = MaterialTheme.typography.labelMedium) },
              modifier = Modifier.testTag("adjust_${delta.toInt()}"),
            )
          }
        }
      }

      // Target Savings Goal Input + Daily Set-Aside Pill
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Target Savings by Payday (₱)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          if (state.targetSavings > 0.0) {
            Surface(
              color = if (state.isSavingsGoalExceedingBalance) palette.criticalContainer else palette.safeContainer,
              shape = RoundedCornerShape(50),
            ) {
              Text(
                text = if (state.isSavingsGoalExceedingBalance) {
                  "Exceeds balance"
                } else {
                  "Save ${PesoFormatter.format(state.dailySavingsNeeded)}/day"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (state.isSavingsGoalExceedingBalance) palette.onCriticalContainer else palette.onSafeContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }

        OutlinedTextField(
          value = state.targetSavingsInputText,
          onValueChange = onTargetSavingsInputChange,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("target_savings_input"),
          prefix = {
            Text(
              text = "₱ ",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          },
          placeholder = { Text("0 (optional savings goal)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          shape = MaterialTheme.shapes.small,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
          ),
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(0.0 to "No Goal", 300.0 to "₱300", 500.0 to "₱500", 1000.0 to "₱1,000").forEach { (goal, label) ->
            FilterChip(
              selected = state.targetSavings == goal,
              onClick = { onSelectQuickSavings(goal) },
              label = { Text(label, style = MaterialTheme.typography.labelMedium) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("quick_savings_${goal.toInt()}"),
            )
          }
        }
      }

      // Hard Daily Limit Input + Quick Presets
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Hard Daily Limit (₱/day)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          if (state.hardDailyLimit > 0.0) {
            Surface(
              color = if (state.isBelowHardLimit) palette.criticalContainer else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(50),
            ) {
              Text(
                text = if (state.isBelowHardLimit) "CRITICAL • Below limit" else "Safe above limit",
                style = MaterialTheme.typography.labelSmall,
                color = if (state.isBelowHardLimit) palette.onCriticalContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }

        OutlinedTextField(
          value = state.hardDailyLimitInputText,
          onValueChange = onHardDailyLimitInputChange,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("hard_daily_limit_input"),
          prefix = {
            Text(
              text = "₱ ",
              style = MaterialTheme.typography.titleMedium,
              color = if (state.isBelowHardLimit) palette.criticalColor else MaterialTheme.colorScheme.primary,
            )
          },
          placeholder = { Text("200 (early Critical alert threshold)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          shape = MaterialTheme.shapes.small,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (state.isBelowHardLimit) palette.criticalColor else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (state.isBelowHardLimit) palette.criticalColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline,
          ),
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(200.0 to "₱200", 300.0 to "₱300", 400.0 to "₱400", 500.0 to "₱500").forEach { (limit, label) ->
            FilterChip(
              selected = state.hardDailyLimit == limit,
              onClick = { onSelectQuickHardLimit(limit) },
              label = { Text(label, style = MaterialTheme.typography.labelMedium) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = if (state.isBelowHardLimit && state.hardDailyLimit == limit) {
                  palette.criticalContainer
                } else {
                  MaterialTheme.colorScheme.primaryContainer
                },
                selectedLabelColor = if (state.isBelowHardLimit && state.hardDailyLimit == limit) {
                  palette.onCriticalContainer
                } else {
                  MaterialTheme.colorScheme.onPrimaryContainer
                },
              ),
              modifier = Modifier.testTag("quick_hard_limit_${limit.toInt()}"),
            )
          }
        }
      }

      // Payday / Allowance Date Picker + Quick Days Left Pills
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Next Payday / Allowance",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Target: ${DateHelper.formatEpochDay(state.paydayEpochDay)}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.testTag("pick_payday_button"),
            shape = MaterialTheme.shapes.small,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
          ) {
            Icon(
              imageVector = Icons.Filled.CalendarMonth,
              contentDescription = "Pick next payday date",
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pick Date")
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(3, 5, 7, 10, 15).forEach { days ->
            val selected = state.daysRemaining == days
            FilterChip(
              selected = selected,
              onClick = { onSelectQuickDays(days) },
              label = { Text("$days days") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("quick_days_$days"),
            )
          }
        }
      }
    }
  }

  if (showDatePicker) {
    val todayMillis = DateHelper.epochDayToUtcPickerMillis(DateHelper.todayEpochDay())
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = DateHelper.epochDayToUtcPickerMillis(state.paydayEpochDay),
      selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
          return utcTimeMillis >= todayMillis
        }
      },
    )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let(onSelectPaydayMillis)
            showDatePicker = false
          }
        ) {
          Text("Set Payday")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel")
        }
      },
    ) {
      DatePicker(state = datePickerState)
    }
  }
}
