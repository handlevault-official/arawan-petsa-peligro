package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BudgetUiState
import com.example.model.DateHelper
import com.example.model.PesoFormatter
import com.example.ui.theme.LocalSurvivalPalette
import kotlin.math.max

@Composable
fun ForecastAndSavingsCard(
  state: BudgetUiState,
  modifier: Modifier = Modifier,
) {
  val palette = LocalSurvivalPalette.current
  val goalProgress by animateFloatAsState(
    targetValue = state.savingsGoalProgressRatio,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "goalProgress",
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. Savings Progress Bar
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Savings Progress",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Surface(
            color = palette.safeContainer,
            shape = RoundedCornerShape(50),
          ) {
            Text(
              text = if (state.targetSavings > 0.0) {
                "${(goalProgress * 100).toInt()}% of goal"
              } else {
                "Jar: ${PesoFormatter.format(state.totalSavingsAccumulated)}"
              },
              style = MaterialTheme.typography.labelSmall,
              color = palette.onSafeContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        }

        val track = MaterialTheme.colorScheme.surfaceVariant
        val barColor = palette.safeColor
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(50))
            .testTag("savings_progress_bar")
        ) {
          drawRoundRect(
            color = track,
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
          )
          val fillWidth = if (state.targetSavings > 0.0) {
            size.width * goalProgress.coerceAtLeast(0.02f)
          } else if (state.totalSavingsAccumulated > 0.0) {
            size.width * 0.25f
          } else {
            0f
          }
          if (fillWidth > 0f) {
            drawRoundRect(
              color = barColor,
              size = Size(fillWidth, size.height),
              cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
            )
          }
        }

        Text(
          text = if (state.targetSavings > 0.0) {
            "${PesoFormatter.format(state.totalSavingsAccumulated)} set aside (Paisa-Piso + Check-ins) out of ${PesoFormatter.format(state.targetSavings)} goal"
          } else {
            "Set a Target Savings goal above or enable Paisa-Piso round-ups to fill your progress bar."
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // 2. Payday Forecast Banner
      val projected = state.projectedPaydayBalance
      val zeroDay = state.projectedZeroDayOffset
      val isNegativeForecast = projected < 0.0 || zeroDay != null

      Surface(
        color = if (isNegativeForecast) palette.criticalContainer else palette.safeContainer.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("payday_forecast_banner"),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = if (isNegativeForecast) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
            contentDescription = null,
            tint = if (isNegativeForecast) palette.onCriticalContainer else palette.onSafeContainer,
            modifier = Modifier.size(24.dp),
          )
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = if (zeroDay != null) {
                "Forecast: Funds may hit ₱0 in $zeroDay day(s)!"
              } else {
                "Payday Forecast (${DateHelper.formatEpochDay(state.paydayEpochDay)})"
              },
              style = MaterialTheme.typography.titleMedium,
              color = if (isNegativeForecast) palette.onCriticalContainer else palette.onSafeContainer,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "At ${PesoFormatter.format(state.observedDailySpendRate)}/day trend, projected balance on payday is ${PesoFormatter.formatSigned(projected)}.",
              style = MaterialTheme.typography.bodyMedium,
              color = if (isNegativeForecast) palette.onCriticalContainer else palette.onSafeContainer,
            )
          }
        }
      }

      // 3. Savings Projection Trajectory Chart (Current Balance vs Target Savings Goal)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.ShowChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Savings Projection Until Payday",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        val lineColor = if (isNegativeForecast) palette.criticalColor else palette.safeColor
        val goalColor = MaterialTheme.colorScheme.secondary
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        val startBalance = (state.remainingBalance - state.lockedEmergencyFund - state.totalUtang).coerceAtLeast(0.0)
        val dailyBurn = state.observedDailySpendRate
        val goalLineVal = state.targetSavings

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .testTag("savings_projection_chart")
        ) {
          val padH = 12.dp.toPx()
          val padV = 14.dp.toPx()
          val chartW = size.width - padH * 2
          val chartH = size.height - padV * 2
          val days = state.daysRemaining.coerceAtLeast(1)

          val maxVal = max(max(startBalance, goalLineVal), 500.0).toFloat()

          // Baseline & grid
          drawLine(
            color = gridColor,
            start = Offset(padH, padV + chartH),
            end = Offset(padH + chartW, padV + chartH),
            strokeWidth = 1.5.dp.toPx(),
          )
          drawLine(
            color = gridColor,
            start = Offset(padH, padV + chartH / 2f),
            end = Offset(padH + chartW, padV + chartH / 2f),
            strokeWidth = 1.dp.toPx(),
          )

          // Target Savings Goal dashed line
          if (goalLineVal > 0.0) {
            val goalY = padV + chartH * (1f - (goalLineVal.toFloat() / maxVal).coerceIn(0f, 1f))
            drawLine(
              color = goalColor,
              start = Offset(padH, goalY),
              end = Offset(padH + chartW, goalY),
              strokeWidth = 2.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
            )
          }

          // Projected balance curve
          val points = (0..days).map { d ->
            val x = padH + chartW * (d.toFloat() / days.toFloat())
            val projectedAtD = (startBalance - dailyBurn * d).coerceAtLeast(0.0).toFloat()
            val y = padV + chartH * (1f - (projectedAtD / maxVal).coerceIn(0f, 1f))
            Offset(x, y)
          }

          if (points.size >= 2) {
            val linePath = Path().apply {
              moveTo(points.first().x, points.first().y)
              for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2f
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
              }
            }
            val fillPath = Path().apply {
              addPath(linePath)
              lineTo(points.last().x, padV + chartH)
              lineTo(points.first().x, padV + chartH)
              close()
            }
            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0.02f)),
                startY = padV,
                endY = padV + chartH,
              ),
            )
            drawPath(
              path = linePath,
              color = lineColor,
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            drawCircle(color = lineColor, radius = 4.5.dp.toPx(), center = points.first())
            drawCircle(color = lineColor, radius = 4.5.dp.toPx(), center = points.last())
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Today: ${PesoFormatter.format(startBalance)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          if (goalLineVal > 0.0) {
            Text(
              text = "--- Goal: ${PesoFormatter.format(goalLineVal)}",
              style = MaterialTheme.typography.labelSmall,
              color = goalColor,
            )
          }
          Text(
            text = "Payday: ${PesoFormatter.format(projected.coerceAtLeast(0.0))}",
            style = MaterialTheme.typography.labelSmall,
            color = lineColor,
          )
        }
      }
    }
  }
}
