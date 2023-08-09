package com.dk.piley.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class StatefulViewModel<T>(
    initialViewState: T,
) : ViewModel() {
    var state = MutableStateFlow(initialViewState)
        private set

    suspend fun collectState(flow: Flow<T>) {
        flow.collect { state.value = it }
    }
}