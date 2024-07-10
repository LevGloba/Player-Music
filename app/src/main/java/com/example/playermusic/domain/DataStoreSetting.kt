package com.example.playermusic.domain

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.playermusic.data.DataStoreCreate
import com.example.playermusic.data.RetunDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

private typealias returnDataStore = RetunDataStore.ReturnDataStoreSetting
class DataStoreSettingImpl @Inject constructor(private val dataStoreCreate: DataStoreCreate): DataStoreSetting {
    private val oldPath = "/storage/emulated/0/Music"
    private val setting = stringPreferencesKey("PATH")
    private val mutableSettings= MutableStateFlow(oldPath)

    override suspend fun read() {
        withContext(Dispatchers.IO){
            dataStoreCreate.returnDataStore(returnDataStore).data.map { preferences ->
               mutableSettings.update {
                   preferences[setting] ?: oldPath
               }
            }
        }
    }
    override suspend fun write(v: String) {
        withContext(Dispatchers.IO) {
            dataStoreCreate.returnDataStore(returnDataStore).edit { settings ->
                settings[setting] = v
                mutableSettings.update {
                    v
                }
            }
        }
    }

    override fun retungSettingFlow(): StateFlow<String> = mutableSettings.asStateFlow()
}

interface DataStoreSetting: ReadAndWrite {
    fun retungSettingFlow(): StateFlow<String>
}