package com.dk.piley.ui.pile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.dk.piley.backup.BackupManager
import com.dk.piley.common.StatefulViewModel
import com.dk.piley.model.pile.Pile
import com.dk.piley.model.pile.PileRepository
import com.dk.piley.model.task.Task
import com.dk.piley.model.task.TaskRepository
import com.dk.piley.model.task.TaskStatus
import com.dk.piley.model.user.UserRepository
import com.dk.piley.ui.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Pile view model
 *
 * @property taskRepository task repository instance
 * @property pileRepository pile repository instance
 * @property userRepository user repository instance
 * @property backupManager backup manager instance
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PileViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val pileRepository: PileRepository,
    private val userRepository: UserRepository,
    private val backupManager: BackupManager,
    savedStateHandle: SavedStateHandle
) : StatefulViewModel<PileViewState>(PileViewState()) {

    private val _selectedPileIndex = MutableStateFlow(-1)
    val selectedPileIndex: StateFlow<Int>
        get() = _selectedPileIndex

    private var differsFromSelected = false

    init {
        viewModelScope.launch {
            // perform a recurring backup if necessary
            backupManager.performBackupIfNecessary()
            // restart all alarms that got canceled
            taskRepository.restartAlarms()
            // get piles and start updating state
            userRepository.getSignedInUserNotNullFlow().flatMapLatest { user ->
                pileRepository.getPilesWithTasks().flatMapLatest { pilesWithTasks ->
                    selectedPileIndex.map { index ->
                        val idTitleList = pilesWithTasks.map {
                            Pair(
                                it.pile.pileId,
                                it.pile.name
                            )
                        }
                        // initial default pile selection
                        if (!differsFromSelected) {
                            // set initial selected pile if navigated with pile ID
                            val navigationSelectedPileId =
                                savedStateHandle.get<Long>(Screen.Pile.argument)
                            navigationSelectedPileId?.let { pileId ->
                                // if pile id navigation argument passed, navigate to that pile
                                if (pileId != -1L) {
                                    // calculate index of selected pile
                                    val pileIndex = idTitleList.indexOfFirst { it.first == pileId }
                                    onPileChanged(pileIndex, selectedPileIndex.value == pileIndex)
                                } else {
                                    val pileIndex =
                                        idTitleList.indexOfFirst { it.first == user.selectedPileId }
                                    if (pileIndex != -1) {
                                        onPileChanged(pileIndex, false)
                                    }
                                }
                            }
                        }
                        // set index if needed
                        val selectedPileId =
                            idTitleList.getOrNull(index)?.first ?: user.selectedPileId
                        // start mapping pile to view state
                        pilesWithTasks
                            .find { it.pile.pileId == selectedPileId }
                            ?.let { pileWithTasks ->
                                state.value.copy(
                                    pile = pileWithTasks.pile,
                                    // only show non-completed tasks and non-deleted recurring tasks
                                    tasks = pileWithTasks.tasks.filter { task ->
                                        (task.status == TaskStatus.DEFAULT)
                                                || (task.isRecurring && task.status != TaskStatus.DELETED)
                                    },
                                    autoHideEnabled = user.autoHideKeyboard,
                                    pileIdTitleList = idTitleList,
                                    noTasksYet = pileWithTasks.tasks.isEmpty(),
                                )
                            }
                    }
                }
            }.collect { viewState ->
                if (viewState != null) {
                    state.value = viewState
                }
            }
        }
    }

    /**
     * Add a new task to the pile
     *
     * @param text task title
     */
    fun add(text: String) {
        viewModelScope.launch {
            taskRepository.insertTaskWithStatus(
                Task(
                    title = text.trim(),
                    pileId = state.value.pile.pileId,
                    createdAt = Instant.now(),
                    modifiedAt = Instant.now()
                )
            )
        }
    }

    /**
     * Complete task
     *
     * @param task task entity
     */
    fun done(task: Task) {
        viewModelScope.launch {
            taskRepository.insertTaskWithStatus(task.copy(status = TaskStatus.DONE))
        }
    }

    /**
     * Delete task
     *
     * @param task task entity
     */
    fun delete(task: Task) {
        viewModelScope.launch {
            taskRepository.insertTaskWithStatus(task.copy(status = TaskStatus.DELETED))
        }
    }

    /**
     * On pile selection changed
     *
     * @param index selected pile index
     * @param setDiffersFromSelected whether pile differs from currently selected pile
     */
    fun onPileChanged(index: Int, setDiffersFromSelected: Boolean = true) {
        differsFromSelected = setDiffersFromSelected
        _selectedPileIndex.update { index }
    }

    /**
     * Set user message
     *
     * @param message message text
     */
    fun setMessage(message: String?) {
        state.update { it.copy(message = message) }
    }

    /**
     * Set whether recurring tasks should be shown
     *
     * @param shown the visibility value
     */
    fun setShowRecurring(shown: Boolean) {
        state.update { it.copy(showRecurring = shown) }
    }
}

data class PileViewState(
    val pile: Pile = Pile(),
    val tasks: List<Task>? = null,
    val autoHideEnabled: Boolean = true,
    val pileIdTitleList: List<Pair<Long, String>> = emptyList(),
    val noTasksYet: Boolean = false,
    val message: String? = null,
    val showRecurring: Boolean = false
)
