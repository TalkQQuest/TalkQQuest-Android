package com.talkqquest.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.notificationDataStore by preferencesDataStore(name = "talkqquest_notification_prefs")

@Singleton
class NotificationDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val deletedIdsKey = stringSetPreferencesKey("deleted_notification_ids")

    suspend fun deletedNotificationIds(): Set<String> =
        context.notificationDataStore.data.first()[deletedIdsKey].orEmpty()

    suspend fun addDeletedNotificationIds(ids: Collection<String>) {
        if (ids.isEmpty()) return
        context.notificationDataStore.edit { preferences ->
            preferences[deletedIdsKey] = preferences[deletedIdsKey].orEmpty() + ids
        }
    }
}
