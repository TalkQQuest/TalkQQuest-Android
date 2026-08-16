package com.talkqquest.app.feature.profile.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** MediaStore만 담당해 Compose에서 직접 기기 사진을 조회하지 않도록 분리한다. */
class ProfilePhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun recentPhotos(limit: Int = 180): List<Uri> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE,
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC, " +
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val uris = mutableListOf<Uri>()
        context.contentResolver.query(
            collection,
            projection,
            "${MediaStore.Images.Media.MIME_TYPE} LIKE ?",
            arrayOf("image/%"),
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext() && uris.size < limit) {
                uris += Uri.withAppendedPath(collection, cursor.getLong(idColumn).toString())
            }
        }
        uris
    }
}
