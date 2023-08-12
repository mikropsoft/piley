package com.dk.piley.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dk.piley.R
import com.dk.piley.compose.PreviewMainScreen
import com.dk.piley.model.user.NightMode
import com.dk.piley.model.user.PileMode
import com.dk.piley.model.user.User
import com.dk.piley.ui.common.ContentAlertDialog
import com.dk.piley.ui.common.LocalDim
import com.dk.piley.ui.common.TitleTopAppBar
import com.dk.piley.ui.nav.Screen
import com.dk.piley.ui.profile.AppInfo
import com.dk.piley.ui.theme.PileyTheme
import com.dk.piley.util.IndefiniteProgressBar
import com.dk.piley.util.navigateClearBackstack

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()

    // snackbar handler
    viewState.message?.let { message ->
        LaunchedEffect(message, snackbarHostState) {
            snackbarHostState.showSnackbar(message)
            // reset message
            viewModel.resetMessage()
        }
    }

    if (viewState.userDeleted) {
        LaunchedEffect(true) {
            navController.navigateClearBackstack(Screen.SignIn.route)
        }
    }

    SettingsScreen(
        modifier = modifier,
        viewState = viewState,
        onNightModeChange = { viewModel.updateNightMode(it) },
        onDynamicColorChange = { viewModel.updateDynamicColorEnabled(it) },
        onPileModeChange = { viewModel.updateDefaultPileMode(it) },
        onResetPileModes = { viewModel.onResetPileModes() },
        onAutoHideKeyboardChange = { viewModel.updateHideKeyboardEnabled(it) },
        onReminderDelayChange = { viewModel.updateReminderDelay(it) },
        onBackupFrequencyChange = { viewModel.updateBackupFrequency(it) },
        onPullBackupPeriodChange = { viewModel.updatePullBackupPeriod(it) },
        onEditUser = { result -> viewModel.updateUser(result) },
        onDeleteUser = { password -> viewModel.deleteUser(password) },
        onCloseSettings = { navController.popBackStack() },
        onStartTutorial = { navController.navigateClearBackstack(Screen.Intro.route) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewState: SettingsViewState,
    onNightModeChange: (NightMode) -> Unit = {},
    onDynamicColorChange: (Boolean) -> Unit = {},
    onPileModeChange: (PileMode) -> Unit = {},
    onResetPileModes: () -> Unit = {},
    onAutoHideKeyboardChange: (Boolean) -> Unit = {},
    onReminderDelayChange: (Int) -> Unit = {},
    onBackupFrequencyChange: (Int) -> Unit = {},
    onPullBackupPeriodChange: (Int) -> Unit = {},
    onEditUser: (EditUserResult) -> Unit = {},
    onDeleteUser: (String) -> Unit = {},
    onCloseSettings: () -> Unit = {},
    onStartTutorial: () -> Unit = {}
) {
    val dim = LocalDim.current
    val nightModeValues = stringArrayResource(R.array.night_modes).toList()
    val pileModeValues = stringArrayResource(R.array.pile_modes).toList()
    val scrollState = rememberScrollState()
    var editUserDialogOpen by remember { mutableStateOf(false) }
    var deleteUserDialogOpen by remember { mutableStateOf(false) }

    if (editUserDialogOpen) {
        ContentAlertDialog(onDismiss = { editUserDialogOpen = false }) {
            EditUserContent(
                existingName = viewState.user.name,
                onConfirm = { result ->
                    editUserDialogOpen = false
                    onEditUser(result)
                },
                onCancel = { editUserDialogOpen = false }
            )
        }
    }

    if (deleteUserDialogOpen) {
        ContentAlertDialog(onDismiss = { deleteUserDialogOpen = false }) {
            DeleteUserContent(
                onConfirm = { password ->
                    deleteUserDialogOpen = false
                    onDeleteUser(password)
                },
                onCancel = {
                    deleteUserDialogOpen = false
                }
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        IndefiniteProgressBar(visible = viewState.loading)
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            TitleTopAppBar(
                textValue = stringResource(R.string.settings_screen_title),
                justTitle = true,
                onButtonClick = onCloseSettings,
                contentDescription = "close the task detail"
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(dim.medium)
            ) {
                SettingsSection(
                    title = stringResource(R.string.settings_section_appearance_title),
                    icon = Icons.Filled.FormatPaint
                ) {
                    DropdownSettingsItem(
                        title = stringResource(R.string.night_mode_enabled_setting_title),
                        description = stringResource(R.string.night_mode_enabled_setting_description),
                        optionLabel = stringResource(R.string.night_mode_enabled_setting_option_label),
                        selectedValue = nightModeValues[viewState.user.nightMode.value],
                        values = nightModeValues,
                        onValueChange = {
                            onNightModeChange(NightMode.fromValue(nightModeValues.indexOf(it)))
                        }
                    )
                    SwitchSettingsItem(
                        title = stringResource(R.string.dynamic_color_enabled_setting_title),
                        description = stringResource(R.string.dynamic_color_enabled_setting_description),
                        value = viewState.user.dynamicColorOn,
                        onValueChange = onDynamicColorChange
                    )
                }
                Divider()
                SettingsSection(
                    title = stringResource(R.string.settings_section_piles_title),
                    icon = Icons.Filled.ViewAgenda
                ) {
                    DropdownSettingsItem(
                        title = stringResource(R.string.default_pile_mode_setting_title),
                        description = stringResource(R.string.default_pile_mode_setting_description),
                        optionLabel = stringResource(R.string.default_pile_mode_setting_option_label),
                        selectedValue = pileModeValues[viewState.user.pileMode.value],
                        values = pileModeValues,
                        onValueChange = {
                            onPileModeChange(PileMode.fromValue(pileModeValues.indexOf(it)))
                        }
                    )
                    SettingsItem(
                        title = stringResource(R.string.reset_all_pile_modes_setting_title),
                        description = stringResource(R.string.reset_all_pile_modes_setting_description),
                        onClick = onResetPileModes
                    )
                    SwitchSettingsItem(
                        title = stringResource(R.string.automatically_hide_keyboard_setting_title),
                        description = stringResource(R.string.automatically_hide_keyboard_setting_description),
                        value = viewState.user.autoHideKeyboard,
                        onValueChange = onAutoHideKeyboardChange
                    )
                }
                Divider()
                SettingsSection(
                    title = stringResource(R.string.settings_section_notifications_title),
                    icon = Icons.Filled.Notifications
                ) {
                    SliderSettingsItem(
                        title = stringResource(R.string.reminder_delay_duration_setting_title),
                        description = stringResource(R.string.reminder_delay_duration_setting_description),
                        value = viewState.user.defaultReminderDelay,
                        range = Pair(15, 60),
                        steps = 2,
                        onValueChange = onReminderDelayChange
                    )
                }
                if (!viewState.user.isOffline) {
                    Divider()
                    SettingsSection(
                        title = stringResource(R.string.settings_section_backup_title),
                        icon = Icons.Filled.Backup
                    ) {
                        SliderSettingsItem(
                            title = stringResource(R.string.backup_frequency_setting_title),
                            description = stringResource(R.string.backup_frequency_setting_description),
                            value = viewState.user.defaultBackupFrequency,
                            range = Pair(1, 14),
                            steps = 14,
                            onValueChange = onBackupFrequencyChange
                        )
                        SliderSettingsItem(
                            title = stringResource(R.string.backup_pull_after_title),
                            description = stringResource(R.string.backup_pull_after_description),
                            value = viewState.user.loadBackupAfterDays,
                            range = Pair(0, 14),
                            steps = 15,
                            onValueChange = onPullBackupPeriodChange
                        )
                    }
                }
                Divider()
                SettingsSection(
                    title = stringResource(R.string.settings_section_user_title),
                    icon = Icons.Filled.Person
                ) {
                    SettingsItem(
                        title = stringResource(R.string.update_user_setting_title),
                        description = stringResource(R.string.update_user_setting_description),
                        onClick = { editUserDialogOpen = true }
                    )
                    SettingsItem(
                        title = stringResource(R.string.delete_user_setting_title),
                        description = stringResource(R.string.delete_user_setting_description),
                        onClick = { deleteUserDialogOpen = true }
                    )
                    SettingsItem(
                        title = stringResource(R.string.start_tutorial_setting_title),
                        description = stringResource(R.string.start_tutorial_setting_description),
                        onClick = { onStartTutorial() }
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AppInfo()
                }
            }
        }
    }
}

@PreviewMainScreen
@Preview(heightDp = 1000)
@Composable
fun SettingsScreenPreview() {
    PileyTheme {
        Surface {
            val state = SettingsViewState(User())
            SettingsScreen(viewState = state)
        }
    }
}