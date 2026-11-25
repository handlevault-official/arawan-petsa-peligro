package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BudgetUiState
import com.example.model.PesoFormatter
import com.example.ui.theme.LocalSurvivalPalette

@Composable
fun TipidCutsCard(
  state: BudgetUiState,
  onToggleCut: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalSurvivalPalette.current

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(10.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Bolt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier
                .padding(8.dp)
                .size(20.dp),
            )
          }
          Column {
            Text(
              text = "Emergency Tipid Cuts",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Toggle cuts to stretch your runway",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      AnimatedVisibility(visible = state.tipidDailyBoost > 0.0) {
        Surface(
          color = palette.safeContainer,
          shape = MaterialTheme.shapes.small,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("tipid_boost_banner"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Filled.Savings,
                contentDescription = null,
                tint = palette.onSafeContainer,
                modifier = Modifier.size(20.dp),
              )
              Column {
                Text(
                  text = "Daily Cushion: +${PesoFormatter.format(state.tipidDailyBoost)}/day",
                  style = MaterialTheme.typography.titleMedium,
                  color = palette.onSafeContainer,
                )
                Text(
                  text = "${PesoFormatter.format(state.totalTipidSavings)} total saved over ${state.daysRemaining} days",
                  style = MaterialTheme.typography.bodyMedium,
                  color = palette.onSafeContainer.copy(alpha = 0.85f),
                )
              }
            }

            if (state.extraSurvivalDays > 0) {
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                color = palette.safeColor,
                shape = RoundedCornerShape(50),
              ) {
                Text(
                  text = "+${state.extraSurvivalDays}d runway",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }
          }
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.tipidCuts.forEach { cut ->
          val rowBg = if (cut.isEnabled) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          }
          Surface(
            color = rowBg,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(
              width = 1.dp,
              color = if (cut.isEnabled) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
              } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
              },
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clip(MaterialTheme.shapes.small)
              .clickable { onToggleCut(cut.id) }
              .testTag("tipid_cut_${cut.id}"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  text = cut.title,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "+${PesoFormatter.format(cut.dailySavings)}/day",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.safeColor,
                  )
                  Text(
                    text = "• ${cut.subtitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              Switch(
                checked = cut.isEnabled,
                onCheckedChange = { onToggleCut(cut.id) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                  checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
              )
            }
          }
        }
      }
    }
  }
}
