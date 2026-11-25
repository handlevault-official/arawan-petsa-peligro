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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalSurvivalPalette

@Composable
fun SummaryAndSupportCard(
  summaryPreview: String,
  onCopySummary: () -> Unit,
  onShareSummary: () -> Unit,
  onOpenKofi: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalSurvivalPalette.current

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Shareable Survival Summary Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Shareable Survival Summary",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
          shape = MaterialTheme.shapes.small,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = summaryPreview,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(14.dp),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onCopySummary,
            modifier = Modifier
              .weight(1f)
              .testTag("copy_summary_button"),
            shape = MaterialTheme.shapes.small,
          ) {
            Icon(
              imageVector = Icons.Filled.ContentCopy,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.copy_summary))
          }

          OutlinedButton(
            onClick = onShareSummary,
            modifier = Modifier.testTag("share_summary_button"),
            shape = MaterialTheme.shapes.small,
          ) {
            Icon(
              imageVector = Icons.Filled.Share,
              contentDescription = "Share summary",
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.share_summary))
          }
        }
      }
    }

    // "Pag May Pera Na" $2 Ko-fi Reminder Banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = palette.kofiBg),
      border = BorderStroke(1.dp, palette.kofiBorder),
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
            color = palette.kofiAccent.copy(alpha = 0.16f),
            shape = MaterialTheme.shapes.small,
          ) {
            Icon(
              imageVector = Icons.Filled.Coffee,
              contentDescription = null,
              tint = palette.kofiAccent,
              modifier = Modifier
                .padding(8.dp)
                .size(22.dp),
            )
          }
          Text(
            text = "Pag May Pera Na Reminder ☕",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Text(
          text = stringResource(R.string.kofi_message),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Button(
          onClick = onOpenKofi,
          colors = ButtonDefaults.buttonColors(
            containerColor = palette.kofiAccent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
          shape = MaterialTheme.shapes.small,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("kofi_button"),
        ) {
          Text(
            text = stringResource(R.string.kofi_cta),
            style = MaterialTheme.typography.labelLarge,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}
