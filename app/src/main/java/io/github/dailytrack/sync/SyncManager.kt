/*
 * Copyright 2024 Soul Track Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.github.dailytrack.sync

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SyncManager(
    private val context: Context,
    private val syncRepository: SyncRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState

    data class SyncUiState(
        val isEnabled: Boolean = false,
        val status: SyncRepository.SyncStatus = SyncRepository.SyncStatus.DISABLED,
        val lastSyncTime: String? = null,
        val lastError: String? = null,
        val folderName: String? = null,
        val isSyncthingInstalled: Boolean = false,
        val deviceName: String = ""
    )

    init {
        scope.launch {
            syncRepository.syncStatus.collect { status ->
                _uiState.value = _uiState.value.copy(
                    status = status,
                    isEnabled = syncRepository.isSyncEnabled(),
                    lastSyncTime = syncRepository.lastSyncTime.value,
                    lastError = syncRepository.lastError.value
                )
            }
        }
        refreshState()
    }

    fun refreshState() {
        scope.launch {
            val folderUri = syncRepository.getSyncFolderUri()?.let { Uri.parse(it) }
            val folderName = folderUri?.let {
                try {
                    DocumentFile.fromTreeUri(context, it)?.name
                } catch (e: Exception) {
                    null
                }
            }

            _uiState.value = _uiState.value.copy(
                isEnabled = syncRepository.isSyncEnabled(),
                status = syncRepository.syncStatus.value,
                lastSyncTime = syncRepository.lastSyncTime.value,
                lastError = syncRepository.lastError.value,
                folderName = folderName,
                deviceName = syncRepository.deviceId
            )
        }
    }

    fun enableSync(folderUri: Uri) {
        syncRepository.enableSync(folderUri)
        refreshState()
        scope.launch {
            delay(500)
            syncRepository.performSync()
            refreshState()
        }
    }

    fun disableSync() {
        syncRepository.disableSync()
        refreshState()
    }

    fun manualSync() {
        scope.launch {
            syncRepository.performSync()
            refreshState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(lastError = null)
    }

    fun onDestroy() {
        scope.cancel()
    }
}
