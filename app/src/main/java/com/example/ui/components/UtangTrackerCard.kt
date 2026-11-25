package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BudgetUiState
import com.example.model.PesoFormatter
import com.example.ui.theme.LocalSurvivalPalette

@Composable
fun UtangTrackerCard(
  state: BudgetUiState,
  onAddUtang: (String, Double, Boolean) -> Unit,
  onRemoveUtang: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalSurvivalPalette.current
  var titleText by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var isRecurring by remember { mutableStateOf(false) }

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
          modifier = Modifier.weight(1f),
        ) {
          Surface(
            color = palette.criticalContainer,
            shape = RoundedCornerShape(10.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.AccountBalanceWallet,
              contentDescription = null,
              tint = palette.onCriticalContainer,
              modifier = Modifier
                .padding(8.dp)
                .size(20.dp),
            )
          }
          Column {
            Text(
              text = "Utang & Bills Tracker",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Deducted for your real daily allowance",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        if (state.totalUtang > 0.0) {
          Surface(
            color = palette.criticalContainer,
            shape = RoundedCornerShape(50),
          ) {
            Text(
              text = "-${PesoFormatter.format(state.totalUtang)}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = palette.onCriticalContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = titleText,
          onValueChange = { titleText = it },
          modifier = Modifier
            .weight(1.3f)
            .testTag("utang_title_input"),
          label = { Text("Utang / Bill") },
          placeholder = { Text("e.g. Meralco, Ate") },
          singleLine = true,
          shape = MaterialTheme.shapes.small,
        )
        OutlinedTextField(
          value = amountText,
          onValueChange = { raw ->
            val filtered = buildString {
              var dotSeen = false
              for (ch in raw) {
                if (ch.isDigit()) append(ch)
                else if (ch == '.' && !dotSeen) {
                  dotSeen = true
                  append(ch)
                }
              }
            }
            amountText = filtered
          },
          modifier = Modifier
            .weight(1f)
            .testTag("utang_amount_input"),
          label = { Text("Amount (₱)") },
          prefix = { Text("₱") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          shape = MaterialTheme.shapes.small,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        FilterChip(
          selected = isRecurring,
          onClick = { isRecurring = !isRecurring },
          label = { Text("Recurring bill") },
        )

        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
        Button(
          onClick = {
            if (titleText.isNotBlank() && parsedAmount > 0.0) {
              onAddUtang(titleText, parsedAmount, isRecurring)
              titleText = ""
              amountText = ""
              isRecurring = false
            }
          },
          enabled = titleText.isNotBlank() && parsedAmount > 0.0,
          shape = MaterialTheme.shapes.small,
          modifier = Modifier.testTag("add_utang_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Obligation")
        }
      }

      if (state.utangItems.isEmpty()) {
        Text(
          text = "No upcoming utang or bills listed. Add obligations above to see your true spendable allowance.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          state.utangItems.forEach { item ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
              shape = MaterialTheme.shapes.small,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = if (item.isRecurring) "Recurring obligation" else "Upcoming obligation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Text(
                  text = "-${PesoFormatter.format(item.amount)}",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = palette.criticalColor,
                )
                IconButton(
                  onClick = { onRemoveUtang(item.id) },
                  modifier = Modifier.testTag("remove_utang_${item.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Mark utang paid or remove",
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
