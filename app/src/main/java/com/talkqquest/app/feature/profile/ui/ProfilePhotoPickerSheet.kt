package com.talkqquest.app.feature.profile.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.content.Intent
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.talkqquest.app.core.designsystem.figma
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.ModalDimOverlay
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.feature.profile.viewmodel.ProfilePhotoPickerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfilePhotoPickerSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onPhotoConfirmed: (Uri) -> Unit,
) {
    if (!visible) return
    val viewModel: ProfilePhotoPickerViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    // ModalDimOverlay dims the host window's bars. Capture the pre-sheet navigation bar
    // state so this full-window dialog can own only that OS area without dimming it.
    val hostWindow = context.findActivity()?.window
    val originalNavigationBarColor = remember(hostWindow) { hostWindow?.navigationBarColor }
    val originalLightNavigationBar = remember(hostWindow) {
        hostWindow?.let { WindowCompat.getInsetsController(it, it.decorView).isAppearanceLightNavigationBars }
    }
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var hasRequestedPermission by remember { mutableStateOf(false) }
    var sheetVisible by remember { mutableStateOf(true) }
    var hasPhotoPermission by remember { mutableStateOf(context.hasPhotoReadPermission()) }
    var isConfirming by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        hasRequestedPermission = true
        val callbackGranted = permissions.values.any { it }
        hasPhotoPermission = callbackGranted && context.hasPhotoReadPermission()
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPhotoPermission = context.hasPhotoReadPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(hasPhotoPermission) {
        if (hasPhotoPermission) viewModel.loadPhotos()
    }
    fun closeSheet() {
        if (!sheetVisible) return
        sheetVisible = false
        scope.launch {
            delay(220)
            onDismiss()
        }
    }
    Dialog(
        onDismissRequest = ::closeSheet,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BackHandler(onBack = ::closeSheet)
        // The dialog window must not supply its own dim: ModalDimOverlay is the shared scrim.
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        DisposableEffect(dialogWindow, originalNavigationBarColor, originalLightNavigationBar) {
            if (dialogWindow == null || originalNavigationBarColor == null || originalLightNavigationBar == null) {
                onDispose { }
            } else {
                val previousNavigationBarColor = dialogWindow.navigationBarColor
                val dialogInsetsController = WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView)
                val previousLightNavigationBar = dialogInsetsController.isAppearanceLightNavigationBars

                dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                dialogWindow.navigationBarColor = originalNavigationBarColor
                dialogInsetsController.isAppearanceLightNavigationBars = originalLightNavigationBar

                onDispose {
                    dialogWindow.navigationBarColor = previousNavigationBarColor
                    dialogInsetsController.isAppearanceLightNavigationBars = previousLightNavigationBar
                }
            }
        }
        Box(Modifier.fillMaxSize()) {
            ModalDimOverlay(visible = sheetVisible, onDismiss = ::closeSheet)
            AnimatedVisibility(
                visible = sheetVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                ) + fadeIn(tween(180)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                ) + fadeOut(tween(140)),
            ) {
                ProfilePhotoPickerContent(
                    hasPhotoPermission = hasPhotoPermission,
                    permissionRequested = hasRequestedPermission,
                    failedToLoad = uiState.failedToLoad,
                    isLoading = uiState.isLoading,
                    photos = uiState.photos,
                    selectedUri = selectedUri,
                    onDismiss = ::closeSheet,
                    onRequestPermission = {
                        hasRequestedPermission = true
                        permissionLauncher.launch(context.photoPermissions())
                    },
                    onOpenSettings = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
                    },
                    onSelect = { selectedUri = it },
                    onConfirm = {
                        val uri = selectedUri
                        if (uri != null && !isConfirming) {
                            isConfirming = true
                            onPhotoConfirmed(uri)
                            closeSheet()
                        }
                    },
                    onRetry = viewModel::loadPhotos,
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoPickerContent(
    hasPhotoPermission: Boolean,
    permissionRequested: Boolean,
    failedToLoad: Boolean,
    isLoading: Boolean,
    photos: List<Uri>,
    selectedUri: Uri?,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelect: (Uri) -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(White),
    ) {
        val cellSize = (maxWidth - 8.dp) / 3
        val gridHeight = cellSize * 2 + 4.dp
        Column(Modifier.fillMaxWidth()) {
        // 16dp 스페이서와 HeadingM의 30dp 줄 상자 덕분에 핸들→제목, 제목→그리드 사이의
        // 보이는 간격이 대칭을 이룬다. 44dp 헤더가 HeadingM의 30dp 줄 상자를 가운데 정렬하므로,
        // 보이는 각 간격은 공유 스페이서에 7dp를 더한 값이 된다.
        val opticalGap = 16.dp
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(4.dp)) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Gray300),
            )
        }
        Spacer(Modifier.height(opticalGap))
        Box(Modifier.fillMaxWidth().height(44.dp)) {
            Text(
                "프로필 사진 선택",
                style = TqType.HeadingM.figma(),
                color = Gray900,
                modifier = Modifier.align(Alignment.Center),
            )
            Box(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).size(44.dp).clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss,
                ), contentAlignment = Alignment.Center,
            ) { Icon(Icons.Default.Close, contentDescription = "닫기", tint = Gray700) }
        }
        Spacer(Modifier.height(opticalGap))
        Box(Modifier.fillMaxWidth().height(gridHeight)) {
            when {
                !hasPhotoPermission -> PermissionContent(permissionRequested, onRequestPermission, onOpenSettings)
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary600)
                failedToLoad -> FailedPhotoContent(onRetry)
                photos.isEmpty() -> Text(
                    "선택할 수 있는 사진이 없어요.", style = TqType.BodyM.figma(), color = Gray500,
                    modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center,
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(photos, key = { it.toString() }) { uri ->
                        PhotoCell(uri, uri == selectedUri, onSelect)
                    }
                }
            }
        }
        val enabled = selectedUri != null
        Box(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                .navigationBarsPadding().fillMaxWidth().height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) Primary600 else Gray300)
                .clickable(enabled = enabled, onClick = onConfirm),
            contentAlignment = Alignment.Center,
        ) { Text("선택하기", style = TqType.BodyL.figma().copy(fontWeight = FontWeight.SemiBold), color = White) }
        }
    }
}

@Composable
private fun FailedPhotoContent(onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("사진을 불러오지 못했어요.", style = TqType.BodyM.figma(), color = Gray500)
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.height(44.dp).clip(RoundedCornerShape(12.dp)).background(Primary600)
                .clickable(onClick = onRetry).padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) { Text("다시 시도", style = TqType.BodyM.figma().copy(fontWeight = FontWeight.SemiBold), color = White) }
    }
}

@Composable
private fun PermissionContent(
    permissionRequested: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            if (permissionRequested) "사진 접근 권한이 허용되지 않았어요.\n설정에서 권한을 허용한 뒤 다시 시도해 주세요."
            else "사진을 불러오려면 접근 권한이 필요해요.",
            style = TqType.BodyM.figma(), color = Gray500, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.height(44.dp).clip(RoundedCornerShape(12.dp)).background(Primary600)
                .clickable(onClick = if (permissionRequested) onOpenSettings else onRequestPermission).padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) { Text(if (permissionRequested) "설정으로 이동" else "권한 허용하기", style = TqType.BodyM.figma().copy(fontWeight = FontWeight.SemiBold), color = White) }
    }
}

@Composable
private fun PhotoCell(uri: Uri, selected: Boolean, onSelect: (Uri) -> Unit) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(2.dp))
            .border(if (selected) 2.dp else 0.dp, Primary600, RoundedCornerShape(2.dp))
            .clickable { onSelect(uri) },
    ) {
        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        if (selected) Box(
            Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp).background(Primary600, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Default.Check, contentDescription = "선택됨", tint = White, modifier = Modifier.size(16.dp)) }
    }
}

private fun android.content.Context.hasPhotoReadPermission(): Boolean {
    val imageGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    val selectedGranted = Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
    val legacyGranted = Build.VERSION.SDK_INT < 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    return imageGranted || selectedGranted || legacyGranted
}

private fun android.content.Context.photoPermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
