package com.dk.piley.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dk.piley.ui.theme.PileyTheme

@Composable
fun TaskStats(modifier: Modifier = Modifier, doneCount: Int, deletedCount: Int, currentCount: Int) {
    Column(modifier = modifier) {
        androidx.compose.material3.Text(
            text = "Statistics",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp),
            textAlign = TextAlign.Start
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatsColumn("Done", doneCount.toString())
            StatsColumn("Current", currentCount.toString(), MaterialTheme.typography.headlineLarge)
            StatsColumn("Deleted", deletedCount.toString())
        }
    }
}

@Composable
fun StatsColumn(
    description: String,
    content: String,
    contentStyle: TextStyle = MaterialTheme.typography.headlineMedium,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.material3.Text(
            text = description,
            color = MaterialTheme.colorScheme.onBackground
        )
        androidx.compose.material3.Text(
            text = content,
            style = contentStyle,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview
@Composable
fun TaskStatsPreview() {
    PileyTheme(useDarkTheme = true) {
        TaskStats(modifier = Modifier.fillMaxWidth(), 2, 3, 1)
    }
}