package com.dk.piley.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dk.piley.R
import com.dk.piley.compose.PreviewMainScreen
import com.dk.piley.ui.common.OutlineCard
import com.dk.piley.ui.common.TitleHeader
import com.dk.piley.ui.nav.Screen
import com.dk.piley.ui.theme.PileyTheme
import com.dk.piley.util.AlertDialogHelper
import com.dk.piley.util.IndefiniteProgressBar
import com.dk.piley.util.navigateClearBackstack
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDateTime


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()
    val context = LocalContext.current

    if (viewState.signedOutState == SignOutState.SIGNED_OUT) {
        LaunchedEffect(true) {
            navController.navigateClearBackstack(Screen.SignIn.route)
        }
    }
    if (viewState.toastMessage != null) {
        LaunchedEffect(key1 = viewState.toastMessage) {
            Toast.makeText(context, viewState.toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.setToastMessage(null)
        }
    }
    ProfileScreen(
        modifier = modifier,
        viewState = viewState,
        setSignOutState = { viewModel.setSignedOutState(it) },
        onClickSettings = { navController.navigate(Screen.Settings.route) },
        onBackup = { viewModel.attemptBackup() },
        onSignOut = {
            viewModel.signOut()
        },
        onSignOutWithError = {
            viewModel.signOutAfterError()
        }
    )
}

@Composable
private fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewState: ProfileViewState,
    setSignOutState: (state: SignOutState) -> Unit = {},
    onClickSettings: () -> Unit = {},
    onBackup: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSignOutWithError: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    if (viewState.signedOutState == SignOutState.SIGNED_OUT_ERROR) {
        AlertDialogHelper(
            title = stringResource(R.string.backup_error_dialog_title),
            description = stringResource(R.string.backup_error_dialog_description),
            confirmText = stringResource(R.string.backup_error_dialog_confirm_button),
            onConfirm = onSignOutWithError,
            onDismiss = { setSignOutState(SignOutState.SIGNED_IN) }
        )
    }
    Box(modifier = modifier.fillMaxSize()) {
        IndefiniteProgressBar(visible = viewState.signedOutState == SignOutState.SIGNING_OUT || viewState.showProgressBar)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClickSettings) {
                    Icon(
                        Icons.Filled.Settings,
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = "go to settings"
                    )
                }
                if (viewState.userIsOffline) {
                    Spacer(modifier = Modifier.size(0.dp))
                } else {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.Filled.Logout,
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = "sign out"
                        )
                    }
                }
            }
            UserInfo(name = viewState.userName)
            Spacer(modifier = Modifier.size(16.dp))
            OutlineCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                TitleHeader(
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    title = stringResource(R.string.user_statistics_section_title),
                    icon = Icons.Default.BarChart
                )
                TaskStats(
                    doneCount = viewState.doneTasks,
                    deletedCount = viewState.deletedTasks,
                    currentCount = viewState.currentTasks,
                    averageTaskDuration = viewState.averageTaskDurationInHours,
                    biggestPile = viewState.biggestPileName,
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            OutlineCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                TitleHeader(
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    title = stringResource(R.string.upcoming_tasks_section_title),
                    icon = Icons.Default.Upcoming
                )
                UpcomingTasksList(
                    modifier = Modifier.fillMaxWidth(),
                    pileNameTaskList = viewState.upcomingTaskList
                )
            }
            if (!viewState.userIsOffline) {
                Spacer(modifier = Modifier.size(16.dp))
                OutlineCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TitleHeader(
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                        title = stringResource(R.string.backup_section_title),
                        icon = Icons.Default.Cloud
                    )
                    BackupInfo(lastBackup = viewState.lastBackup, onClickBackup = onBackup)
                }
            }
            Box(contentAlignment = Alignment.BottomCenter) { // TODO fix this, stick to bottom
                AppInfo()
            }
        }
    }
}

@PreviewMainScreen
@Composable
fun ProfileScreenPreview() {
    AndroidThreeTen.init(LocalContext.current)
    PileyTheme {
        Surface {
            val state = ProfileViewState(
                userName = "Thomas",
                lastBackup = LocalDateTime.now(),
                doneTasks = 0,
                deletedTasks = 2,
                currentTasks = 3,
                upcomingTaskList = previewUpcomingTasksList
            )
            ProfileScreen(viewState = state)
        }
    }
}

@PreviewMainScreen
@Composable
fun ProfileScreenUserOfflinePreview() {
    AndroidThreeTen.init(LocalContext.current)
    PileyTheme {
        Surface {
            val state = ProfileViewState(
                userName = "Thomas",
                lastBackup = LocalDateTime.now(),
                doneTasks = 0,
                deletedTasks = 2,
                currentTasks = 3,
                upcomingTaskList = previewUpcomingTasksList,
                userIsOffline = true
            )
            ProfileScreen(viewState = state)
        }
    }
}