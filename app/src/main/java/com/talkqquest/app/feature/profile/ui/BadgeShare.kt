package com.talkqquest.app.feature.profile.ui

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.talkqquest.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

private const val ShareCardWidth = 1080
private const val ShareCardHeight = 1350
private const val FileProviderAuthoritySuffix = ".fileprovider"

/** Creates the share image off the main thread, then opens Android's system share sheet. */
suspend fun shareEarnedBadge(context: Context, badge: ProfileBadgeUi) {
    val cardFile = withContext(Dispatchers.IO) {
        runCatching { createBadgeShareCard(context.applicationContext, badge) }.getOrNull()
    }
    if (cardFile == null) {
        Toast.makeText(context, "배지를 공유하지 못했어요.", Toast.LENGTH_SHORT).show()
        return
    }

    val started = runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + FileProviderAuthoritySuffix,
            cardFile,
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "톡퀘스트에서 ‘${badge.name}’ 배지를 획득했어요!")
            clipData = ClipData.newRawUri("badge_share", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "배지 공유하기").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }.isSuccess

    if (!started) {
        Toast.makeText(context, "배지를 공유하지 못했어요.", Toast.LENGTH_SHORT).show()
    }
}

private fun createBadgeShareCard(context: Context, badge: ProfileBadgeUi): File {
    val bitmap = Bitmap.createBitmap(ShareCardWidth, ShareCardHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.rgb(248, 250, 252))

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.WHITE
    canvas.drawRoundRect(RectF(72f, 90f, 1008f, 1260f), 52f, 52f, paint)

    paint.color = Color.rgb(99, 83, 240)
    canvas.drawRoundRect(RectF(420f, 164f, 660f, 220f), 28f, 28f, paint)

    val badgeBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_profile_badge_unlocked)
        ?: error("Unlocked badge drawable is unavailable")
    canvas.drawBitmap(badgeBitmap, null, Rect(300, 272, 780, 752), paint)
    badgeBitmap.recycle()

    val semibold = ResourcesCompat.getFont(context, R.font.pretendard_semibold) ?: Typeface.DEFAULT_BOLD
    val medium = ResourcesCompat.getFont(context, R.font.pretendard_medium) ?: Typeface.DEFAULT
    drawCenteredText(
        canvas = canvas,
        text = "톡퀘스트에서 획득한 배지",
        typeface = medium,
        textSize = 34f,
        color = Color.rgb(100, 116, 139),
        top = 778,
        maxLines = 1,
    )
    drawCenteredText(
        canvas = canvas,
        text = badge.name,
        typeface = semibold,
        textSize = 58f,
        color = Color.rgb(30, 41, 59),
        top = 842,
        maxLines = 2,
    )
    drawCenteredText(
        canvas = canvas,
        text = badge.description.orEmpty(),
        typeface = medium,
        textSize = 38f,
        color = Color.rgb(71, 85, 105),
        top = 992,
        maxLines = 3,
    )
    drawCenteredText(
        canvas = canvas,
        text = "획득일  ${badge.earnedAt.displayShareDate()}",
        typeface = medium,
        textSize = 32f,
        color = Color.rgb(100, 116, 139),
        top = 1138,
        maxLines = 1,
    )

    val directory = File(context.cacheDir, "shared_badges").apply { mkdirs() }
    check(directory.isDirectory) { "Could not create badge share cache directory" }
    val safeId = badge.id.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9_-]"), "_").ifBlank { "badge" }
    val outputFile = File(directory, "badge_$safeId.png")
    FileOutputStream(outputFile).use { output ->
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Could not encode badge share card" }
    }
    bitmap.recycle()
    return outputFile
}

private fun drawCenteredText(
    canvas: Canvas,
    text: String,
    typeface: Typeface,
    textSize: Float,
    color: Int,
    top: Int,
    maxLines: Int,
) {
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSize
        this.color = color
    }
    val layout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, 792)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setIncludePad(false)
        .setEllipsize(TextUtils.TruncateAt.END)
        .setMaxLines(maxLines)
        .build()
    canvas.save()
    canvas.translate((ShareCardWidth - layout.width) / 2f, top.toFloat())
    layout.draw(canvas)
    canvas.restore()
}

private fun String?.displayShareDate(): String = this
    ?.substringBefore("T")
    ?.replace("-", ".")
    ?.takeIf { it.isNotBlank() }
    ?: "-"
