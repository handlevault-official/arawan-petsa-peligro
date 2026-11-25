package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.BudgetUiState
import com.example.model.DateHelper
import com.example.model.ExpenseCategory
import com.example.model.PesoFormatter
import com.example.ui.theme.LocalSurvivalPalette
import kotlin.math.max

@Composable
fun ExpenseAnalyticsSection(
  state: BudgetUiState,
  modifier: Modifier = Modifier,
) {
  val palette = LocalSurvivalPalette.current
  val categoryColors = mapOf(
    ExpenseCategory.PAGKAIN to palette.safeColor,
    ExpenseCategory.PAMASAHE_LOAD to palette.tightColor,
    ExpenseCategory.BILLS to palette.criticalColor,
    ExpenseCategory.IBA_PA to MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

    // 1. Category Summary Progress Bar List
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "Where Your Money Goes",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      val breakdown = state.categoryBreakdown()
      val trackColor = MaterialTheme.colorScheme.surfaceVariant
      breakdown.forEach { (category, sum, ratio) ->
        val barColor = categoryColors[category] ?: palette.safeColor
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = category.label,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${PesoFormatter.format(sum)} (${(ratio * 100).toInt()}%)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(50))
          ) {
            drawRoundRect(
              color = trackColor,
              cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
            )
            if (ratio > 0f) {
              drawRoundRect(
                color = barColor,
                size = Size(size.width * ratio.coerceAtLeast(0.03f), size.height),
                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
              )
            }
          }
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

    // 2. 7-Day Daily Spending Trend Line Chart
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "7-Day Spending Trend",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      val series = state.last7DaysSpendSeries()
      val maxSpend = max(series.maxOfOrNull { it.second } ?: 0.0, 200.0).toFloat()
      val lineColor = MaterialTheme.colorScheme.primary
      val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(120.dp)
          .testTag("seven_day_line_chart")
      ) {
        val padH = 14.dp.toPx()
        val padV = 12.dp.toPx()
        val chartW = size.width - padH * 2
        val chartH = size.height - padV * 2

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

        val stepX = if (series.size > 1) chartW / (series.size - 1) else chartW
        val pts = series.mapIndexed { idx, (_, amt) ->
          val x = padH + stepX * idx
          val y = padV + chartH * (1f - (amt.toFloat() / maxSpend).coerceIn(0f, 1f))
          Offset(x, y)
        }

        if (pts.size >= 2) {
          val linePath = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) {
              lineTo(pts[i].x, pts[i].y)
            }
          }
          val areaPath = Path().apply {
            addPath(linePath)
            lineTo(pts.last().x, padV + chartH)
            lineTo(pts.first().x, padV + chartH)
            close()
          }
          drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
              colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0.02f)),
              startY = padV,
              endY = padV + chartH,
            ),
          )
          drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
          )
        }

        pts.forEach { pt ->
          drawCircle(color = lineColor, radius = 4.dp.toPx(), center = pt)
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        series.forEach { (label, _) ->
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

    // 3. Category Distribution Pie Chart
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "Spending Distribution Pie",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      val breakdown = state.categoryBreakdown()
      val total = state.totalSpent
      val emptyTrack = MaterialTheme.colorScheme.surfaceVariant

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Canvas(
          modifier = Modifier
            .size(104.dp)
            .testTag("category_pie_chart")
        ) {
          if (total <= 0.0) {
            drawArc(
              color = emptyTrack,
              startAngle = 0f,
              sweepAngle = 360f,
              useCenter = true,
            )
          } else {
            var startAngle = -90f
            breakdown.forEach { (cat, sum, _) ->
              if (sum > 0.0) {
                val sweep = ((sum / total) * 360.0).toFloat()
                drawArc(
                  color = categoryColors[cat] ?: Color.Gray,
                  startAngle = startAngle,
                  sweepAngle = sweep,
                  useCenter = true,
                )
                startAngle += sweep
              }
            }
          }
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          breakdown.forEach { (cat, sum, ratio) ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(categoryColors[cat] ?: Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = cat.label,
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
              Text(
                text = if (total > 0.0) "${(ratio * 100).toInt()}%" else PesoFormatter.format(sum),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

    // 4. Monthly Logging Consistency Calendar
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      val (monthTitle, daysList) = DateHelper.currentMonthHeaderAndDays()
      val activeDays = state.expenseEpochDays()
      val today = state.todayEpochDay

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Logging Calendar • $monthTitle",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "${activeDays.size} active day(s)",
          style = MaterialTheme.typography.labelSmall,
          color = palette.safeColor,
        )
      }

      Column(
        modifier = Modifier.testTag("expense_calendar_grid"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        daysList.chunked(7).forEach { week ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            week.forEach { (dayNum, epochDay) ->
              val hasLog = activeDays.contains(epochDay)
              val isToday = epochDay == today
              Surface(
                color = when {
                  isToday -> MaterialTheme.colorScheme.primaryContainer
                  hasLog -> palette.safeContainer.copy(alpha = 0.65f)
                  else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 5.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Text(
                    text = "$dayNum",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Box(
                    modifier = Modifier
                      .size(5.dp)
                      .clip(CircleShape)
                      .background(
                        if (hasLog) palette.safeColor else Color.Transparent
                      )
                  )
                }
              }
            }
            // Pad final week to 7 columns so cells align cleanly
            repeat(7 - week.size) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}
