package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.model.DateHelper
import com.example.model.ExpenseCategory
import com.example.model.PesoFormatter
import com.example.model.computeRoundUpChange
import com.example.ui.theme.LocalSurvivalPalette

@Composable
fun ExpenseLoggerCard(
  state: BudgetUiState,
  onLogExpense: (Double, ExpenseCategory, String) -> Unit,
  onUndoExpense: (String) -> Unit,
  onExportCsv: () -> Unit,
  onToggleRoundUp: () -> Unit,
  onSelectRoundUpIncrement: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalSurvivalPalette.current
  var amountText by remember { mutableStateOf("") }
  var noteText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ExpenseCategory.PAGKAIN) }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Quick Expense Logger",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Subtracts & recalculates allowance instantly",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (state.totalSpent > 0.0) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(50),
          ) {
            Text(
              text = "Spent: ${PesoFormatter.format(state.totalSpent)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
          }
        }
      }

      // Category Tags
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ExpenseCategory.entries.forEach { category ->
          val isSelected = selectedCategory == category
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = category },
            leadingIcon = {
              Icon(
                imageVector = categoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            label = { Text(category.label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.testTag("category_${category.name}"),
          )
        }
      }

      // Amount + Note + Submit
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
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
            .testTag("expense_amount_input"),
          label = { Text("Log Spend (₱)") },
          prefix = { Text("₱") },
          placeholder = { Text("85") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          shape = MaterialTheme.shapes.small,
        )

        OutlinedTextField(
          value = noteText,
          onValueChange = { noteText = it },
          modifier = Modifier
            .weight(1.2f)
            .testTag("expense_note_input"),
          label = { Text("Note (optional)") },
          placeholder = { Text("e.g. Siomai rice") },
          singleLine = true,
          shape = MaterialTheme.shapes.small,
        )
      }

      val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
      val previewChange = if (state.roundUpEnabled && parsedAmount > 0.0) {
        computeRoundUpChange(parsedAmount, state.roundUpIncrement)
      } else {
        0.0
      }

      // Paisa-Piso Round-Up Savings Toggle
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = MaterialTheme.shapes.small,
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
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Paisa-Piso Round-Up Savings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = if (previewChange > 0.0) {
                  "Will save +${PesoFormatter.format(previewChange)} loose change to Jar"
                } else {
                  "Jar Total: ${PesoFormatter.format(state.paisaPisoJar)} • Rounds to nearest ₱${state.roundUpIncrement}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (previewChange > 0.0) palette.safeColor else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = state.roundUpEnabled,
              onCheckedChange = { onToggleRoundUp() },
              modifier = Modifier.testTag("round_up_toggle"),
            )
          }
          if (state.roundUpEnabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(10, 50).forEach { inc ->
                FilterChip(
                  selected = state.roundUpIncrement == inc,
                  onClick = { onSelectRoundUpIncrement(inc) },
                  label = { Text("Nearest ₱$inc") },
                  modifier = Modifier.testTag("round_up_$inc"),
                )
              }
            }
          }
        }
      }

      Button(
        onClick = {
          if (parsedAmount > 0.0) {
            onLogExpense(parsedAmount, selectedCategory, noteText)
            amountText = ""
            noteText = ""
          }
        },
        enabled = parsedAmount > 0.0,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("log_spend_button"),
        shape = MaterialTheme.shapes.small,
      ) {
        Icon(
          imageVector = Icons.Filled.AddCircle,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (parsedAmount > 0.0) {
            "Log ${PesoFormatter.format(parsedAmount)} (${selectedCategory.shortTag})"
          } else {
            "Log Spend (₱)"
          },
          style = MaterialTheme.typography.labelLarge,
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

      if (state.expenses.isEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
          )
          Text(
            text = "Wala pang gastos logged today. Enter any spend above to track your remaining runway.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Recent Spends (${state.expenses.size})",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
              onClick = onExportCsv,
              modifier = Modifier.testTag("export_csv_button"),
              shape = MaterialTheme.shapes.small,
            ) {
              Icon(
                imageVector = Icons.Filled.TableView,
                contentDescription = "Export expense history as CSV",
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Export CSV", style = MaterialTheme.typography.labelMedium)
            }
          }
          state.expenses.take(8).forEach { entry ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = MaterialTheme.shapes.small,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = categoryIcon(entry.category),
                  contentDescription = entry.category.label,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (entry.note.isNotBlank()) {
                      "${entry.category.label} • ${entry.note}"
                    } else {
                      entry.category.label
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = DateHelper.formatTime(entry.timestampMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Text(
                  text = "-${PesoFormatter.format(entry.amount)}",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = palette.criticalColor,
                )
                IconButton(
                  onClick = { onUndoExpense(entry.id) },
                  modifier = Modifier.testTag("undo_expense_${entry.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Undo expense entry",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }

      ExpenseAnalyticsSection(state = state)
    }
  }
}

private fun categoryIcon(category: ExpenseCategory) = when (category) {
  ExpenseCategory.PAGKAIN -> Icons.Filled.Restaurant
  ExpenseCategory.PAMASAHE_LOAD -> Icons.Filled.DirectionsTransit
  ExpenseCategory.BILLS -> Icons.Filled.ReceiptLong
  ExpenseCategory.IBA_PA -> Icons.Filled.Category
}
