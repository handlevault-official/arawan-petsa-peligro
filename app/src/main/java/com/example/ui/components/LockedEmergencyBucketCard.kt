package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.BudgetUiState
import com.example.model.PesoFormatter
import com.example.ui.theme.LocalSurvivalPalette

@Composable
fun LockedEmergencyBucketCard(
  state: BudgetUiState,
  onLockedFundInputChange: (String) -> Unit,
  onSelectQuickLockedFund: (Double) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalSurvivalPalette.current

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (state.lockedEmergencyFund > 0.0) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
      } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
      },
    ),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
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
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Lock,
              contentDescription = "Locked Emergency Fund",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier
                .padding(7.dp)
                .size(18.dp),
            )
          }
          Column {
            Text(
              text = "Locked Emergency Fund",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Reserved & excluded from daily spend",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Surface(
          color = palette.tightContainer,
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = if (state.lockedEmergencyFund > 0.0) {
              "UNTOUCHABLE • ${PesoFormatter.format(state.lockedEmergencyFund)}"
            } else {
              "UNTOUCHABLE"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = palette.onTightContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      OutlinedTextField(
        value = state.lockedEmergencyInputText,
        onValueChange = onLockedFundInputChange,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("locked_fund_input"),
        prefix = { Text("₱ ") },
        placeholder = { Text("0 (reserve untouchable emergency fund)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = MaterialTheme.shapes.small,
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf(0.0 to "None", 500.0 to "₱500", 1000.0 to "₱1,000", 2000.0 to "₱2,000").forEach { (amt, label) ->
          FilterChip(
            selected = state.lockedEmergencyFund == amt,
            onClick = { onSelectQuickLockedFund(amt) },
            label = { Text(label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.testTag("quick_locked_${amt.toInt()}"),
          )
        }
      }
    }
  }
}
