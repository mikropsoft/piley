package com.dk.piley.ui.task

import android.Manifest
import android.os.Build
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dk.piley.R
import com.dk.piley.compose.PreviewMainScreen
import com.dk.piley.model.task.TaskStatus
import com.dk.piley.ui.common.EditDescriptionField
import com.dk.piley.ui.common.TitleTopAppBar
import com.dk.piley.ui.common.TwoButtonRow
import com.dk.piley.ui.theme.PileyTheme
import com.dk.piley.util.RequestNotificationPermissionDialog
import com.dk.piley.util.defaultPadding
import com.dk.piley.util.previewUpcomingTasksList
import com.dk.piley.util.toLocalDateTime
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * Task detail screen
 *
 * @param navController generic nav controller
 * @param viewModel task detail view model
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TaskDetailScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()

    // navigate out of view if task completed or deleted
    if ((viewState.task.status == TaskStatus.DONE && !viewState.task.isRecurring) || viewState.task.status == TaskStatus.DELETED) {
        LaunchedEffect(true) {
            navController.popBackStack()
        }
    }

    TaskDetailScreen(
        viewState = viewState,
        onDeleteTask = { viewModel.deleteTask() },
        onCompleteTask = { viewModel.completeTask() },
        onAddReminder = { viewModel.addReminder(it) },
        onCancelReminder = { viewModel.cancelReminder() },
        onClose = { navController.popBackStack() },
        onEditDesc = { viewModel.editDescription(it) },
        onEditTitle = { viewModel.editTitle(it) },
        onSelectPile = { viewModel.selectPile(it) }
    )
}

/**
 * Task detail screen content
 *
 * @param modifier generic modifier
 * @param viewState task detail view state
 * @param onDeleteTask on delete task click
 * @param onCompleteTask on complete task click
 * @param onClose on close task detail screen
 * @param onEditDesc on edit task description
 * @param onEditTitle on edit task title
 * @param onAddReminder on add reminder click
 * @param onCancelReminder on cancel reminder action
 * @param permissionState notification permission state
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    viewState: TaskDetailViewState,
    onDeleteTask: () -> Unit = {},
    onCompleteTask: () -> Unit = {},
    onClose: () -> Unit = {},
    onEditDesc: (String) -> Unit = {},
    onEditTitle: (String) -> Unit = {},
    onAddReminder: (ReminderState) -> Unit = {},
    onCancelReminder: () -> Unit = {},
    onSelectPile: (Int) -> Unit = {},
    permissionState: PermissionState? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    },
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val scrollState = rememberScrollState()

    // notification permission
    var rationaleOpen by remember { mutableStateOf(false) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && bottomSheetScaffoldState.bottomSheetState.hasExpandedState) {
        RequestNotificationPermissionDialog(rationaleOpen) {
            rationaleOpen = false
        }
    }

    AddReminderDrawer(
        bottomSheetScaffoldState = bottomSheetScaffoldState,
        onAddReminder = onAddReminder,
        onDeleteReminder = onCancelReminder,
        initialDate = viewState.task.reminder?.toLocalDateTime(),
        isRecurring = viewState.task.isRecurring,
        recurringFrequency = viewState.task.recurringFrequency,
        recurringTimeRange = viewState.task.recurringTimeRange,
        permissionState = permissionState
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .defaultPadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                TitleTopAppBar(
                    textValue = viewState.titleTextValue,
                    canDeleteOrEdit = true,
                    onEdit = onEditTitle,
                    contentDescription = "close the task detail",
                    onButtonClick = onClose
                )
                EditDescriptionField(
                    value = viewState.descriptionTextValue,
                    onChange = {
                        onEditDesc(it)
                    }
                )
                ReminderInfo(
                    reminderDateTimeText = viewState.reminderDateTimeText,
                    onAddReminder = { scope.launch { bottomSheetScaffoldState.bottomSheetState.expand() } },
                    isRecurring = viewState.task.isRecurring,
                    recurringTimeRange = viewState.task.recurringTimeRange,
                    recurringFrequency = viewState.task.recurringFrequency
                )
                TaskInfo(
                    modifier = Modifier.fillMaxWidth(),
                    task = viewState.task
                )
                SelectedPile(
                    pileNames = viewState.piles.map { it.second },
                    selectedPileIndex = viewState.selectedPileIndex,
                    onSelect = onSelectPile
                )
            }
            TwoButtonRow(
                modifier = Modifier.weight(1f, false),
                onLeftClick = onCompleteTask,
                onRightClick = onDeleteTask,
                leftText = stringResource(R.string.complete_task_button),
                rightText = stringResource(R.string.delete_task_button),
                arrangement = Arrangement.SpaceEvenly,
                rightColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                leftColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@PreviewMainScreen
@Composable
fun TaskDetailScreenPreview() {
    PileyTheme {
        Surface {
            val state = TaskDetailViewState(
                task = previewUpcomingTasksList[1].second,
                titleTextValue = "Hello",
                piles = listOf(Pair(0, "pile1")),
                selectedPileIndex = 0
            )
            TaskDetailScreen(viewState = state, permissionState = null)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@PreviewMainScreen
@Composable
fun TaskDetailScreenPreviewRecurring() {
    PileyTheme {
        Surface {
            val state = TaskDetailViewState(
                task = previewUpcomingTasksList[0].second,
                titleTextValue = "Hello",
                piles = listOf(Pair(0, "pile1")),
                selectedPileIndex = 0
            )
            TaskDetailScreen(viewState = state, permissionState = null)
        }
    }
}