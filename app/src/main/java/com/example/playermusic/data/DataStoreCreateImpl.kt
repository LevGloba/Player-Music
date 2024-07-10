package com.example.playermusic.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataStoreCreateImpl @Inject constructor(
    @ApplicationContext private val context: Context
): DataStoreCreate {
    private val Context.dataSetting: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun returnDataStore(v: RetunDataStore): DataStore<Preferences> {
        return when(v) {
            RetunDataStore.ReturnDataStoreSetting -> context.dataSetting
        }
    }
}

interface DataStoreCreate {
    fun returnDataStore(v: RetunDataStore): DataStore<Preferences>
}

sealed class RetunDataStore {
    data object ReturnDataStoreSetting: RetunDataStore()
}
