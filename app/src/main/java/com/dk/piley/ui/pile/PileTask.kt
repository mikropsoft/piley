package com.dk.piley.ui.pile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dk.piley.model.task.Task
import com.dk.piley.ui.theme.PileyTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PileTask(
    modifier: Modifier,
    dismissState: DismissState,
    task: Task,
    onClick: (task: Task) -> Unit = {}
) {
    SwipeToDismiss(
        state = dismissState,
        modifier = modifier.clickable { onClick(task) },
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        dismissThresholds = { getThreshold(it) },
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.LightGray
                    DismissValue.DismissedToEnd -> Color.Green
                    DismissValue.DismissedToStart -> Color.Red
                }
            )
            val alignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
            }
            val icon = when (direction) {
                DismissDirection.StartToEnd -> Icons.Default.Done
                DismissDirection.EndToStart -> Icons.Default.Delete
            }
            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    icon,
                    contentDescription = "Localized description",
                    modifier = Modifier.scale(scale),
                    tint = color
                )
            }
        },
        dismissContent = {
            PileEntry(
                modifier = Modifier
                    .defaultMinSize(minHeight = 20.dp)
                    .fillMaxWidth(),
                taskText = task.title
            )
        }
    )
}

@Composable
fun PileEntry(modifier: Modifier = Modifier, taskText: String) {
    androidx.compose.material3.Card(modifier = modifier.padding(horizontal = 8.dp)) {
        androidx.compose.material3.Text(
            text = taskText,
            modifier = modifier
                .padding(all = 16.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun getThreshold(direction: DismissDirection): ThresholdConfig {
    return FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.5f)
}

@Preview(showBackground = true)
@Composable
fun PileEntryPreview() {
    PileyTheme(useDarkTheme = true) {
        PileEntry(modifier = Modifier.fillMaxWidth(), taskText = "Hey there")
    }
}