package com.talkqquest.app.feature.onboarding.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import kotlinx.coroutines.delay

@Composable
fun OnboardingCompleteScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
) = FitDesign(compensateStatusBar = false) {
    LaunchedEffect(Unit) {
        delay(2100)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        OnboardingCompleteLogo(
            modifier = Modifier
                .offset(x = 113.dp, y = 300.dp)
                .size(width = 167.dp, height = 152.dp),
        )
        Text(
            text = "\uB531 \uB9DE\uB294 \uBBF8\uC158\uC744\n\uC900\uBE44\uD574\uB4DC\uB9B4\uAC8C\uC694",
            style = TqType.HeadingXL,
            color = Gray800,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 432.dp),
        )
    }
}

@Composable
private fun OnboardingCompleteLogo(
    modifier: Modifier = Modifier,
) {
    var playVariant by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1)
        playVariant = true
    }

    val progress by animateFloatAsState(
        targetValue = if (playVariant) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)),
        label = "onboardingCompleteVariant",
    )

    Canvas(modifier = modifier) {
        val scale = size.width / 156f
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero) {
            val mask = completeLogoMaskPath(progress)
            drawPath(path = completeLogoBodyPath(progress), color = Gray200)
            drawPath(path = completeLogoHeadPath(progress), color = Gray200)
            clipPath(mask) {
                drawPath(
                    path = completeWave59Path(progress),
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xE67264F8), Color(0xFF7264F8)),
                        startY = lerp(92.6444f, -20.3195f, progress),
                        endY = lerp(124.412f, 120f, progress),
                    ),
                )
                drawPath(
                    path = completeWave58Path(progress),
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xE67264F8), Color(0xFF7264F8)),
                        startY = lerp(102.644f, -5.44246f, progress),
                        endY = lerp(134.412f, 133.4f, progress),
                    ),
                )
            }
        }
    }
}

private fun completeLogoMaskPath(progress: Float): Path = Path().apply {
    addPath(completeLogoBodyPath(progress))
    addPath(completeLogoHeadPath(progress))
}

private fun completeLogoBodyPath(progress: Float): Path = Path().apply {
    moveTo(lerp(52.7889f, 52.7318f, progress), lerp(4.73937f, 4.8238f, progress))
    cubicTo(lerp(53.8707f, 53.8141f, progress), lerp(5.04771f, 5.13167f, progress), lerp(57.804f, 57.7499f, progress), lerp(6.81728f, 6.89948f, progress), lerp(59.0275f, 58.9742f, progress), lerp(7.3387f, 7.42034f, progress))
    lineTo(lerp(72.1005f, 72.0554f, progress), lerp(12.9041f, 12.9799f, progress))
    lineTo(lerp(97.9756f, 97.9468f, progress), lerp(23.9188f, 23.983f, progress))
    cubicTo(lerp(101.573f, 101.546f, progress), lerp(25.4585f, 25.5211f, progress), lerp(105.198f, 105.174f, progress), lerp(26.9647f, 27.0258f, progress), lerp(108.791f, 108.769f, progress), lerp(28.5117f, 28.5711f, progress))
    cubicTo(lerp(109.868f, 109.846f, progress), lerp(28.9753f, 29.0343f, progress), lerp(110.789f, 110.768f, progress), lerp(29.68f, 29.7385f, progress), lerp(111.166f, 111.147f, progress), lerp(30.8217f, 30.8799f, progress))
    cubicTo(lerp(111.313f, 111.295f, progress), lerp(31.242f, 31.3001f, progress), lerp(111.442f, 111.425f, progress), lerp(32.05f, 32.108f, progress), lerp(111.283f, 111.266f, progress), lerp(32.4809f, 32.5389f, progress))
    cubicTo(lerp(109.259f, 109.249f, progress), lerp(37.9243f, 37.9827f, progress), lerp(106.86f, 106.857f, progress), lerp(43.3261f, 43.385f, progress), lerp(104.633f, 104.636f, progress), lerp(48.6857f, 48.7449f, progress))
    cubicTo(lerp(102.611f, 102.62f, progress), lerp(53.55f, 53.6097f, progress), lerp(99.0015f, 99.0062f, progress), lerp(50.2422f, 50.3036f, progress), lerp(95.7039f, 95.7071f, progress), lerp(49.2844f, 49.3472f, progress))
    cubicTo(lerp(93.1797f, 93.1817f, progress), lerp(48.5368f, 48.6008f, progress), lerp(90.4889f, 90.4908f, progress), lerp(48.6217f, 48.6867f, progress), lerp(88.026f, 88.0288f, progress), lerp(49.5256f, 49.5916f, progress))
    cubicTo(lerp(82.7542f, 82.7591f, progress), lerp(51.4436f, 51.5115f, progress), lerp(81.4284f, 81.4379f, progress), lerp(55.2226f, 55.2907f, progress), lerp(79.4676f, 79.4829f, progress), lerp(59.9445f, 60.013f, progress))
    lineTo(lerp(76.0932f, 76.1186f, progress), lerp(68.0754f, 68.1445f, progress))
    lineTo(lerp(70.215f, 70.2577f, progress), lerp(82.151f, 82.2212f, progress))
    cubicTo(lerp(68.3197f, 68.3681f, progress), lerp(86.7214f, 86.792f, progress), lerp(66.4204f, 66.4748f, progress), lerp(91.6346f, 91.7055f, progress), lerp(64.3691f, 64.429f, progress), lerp(96.1125f, 96.1838f, progress))
    cubicTo(lerp(63.6531f, 63.7148f, progress), lerp(97.5431f, 97.6146f, progress), lerp(62.3024f, 62.3657f, progress), lerp(98.9258f, 98.9977f, progress), lerp(60.9444f, 61.0087f, progress), lerp(99.7482f, 99.8206f, progress))
    cubicTo(lerp(58.859f, 58.9247f, progress), lerp(101.021f, 101.094f, progress), lerp(56.3459f, 56.4118f, progress), lerp(101.391f, 101.465f, progress), lerp(53.9728f, 54.0378f, progress), lerp(100.773f, 100.849f, progress))
    cubicTo(lerp(51.4887f, 51.5526f, progress), lerp(100.134f, 100.211f, progress), lerp(49.448f, 49.5099f, progress), lerp(98.6911f, 98.7682f, progress), lerp(48.1504f, 48.2093f, progress), lerp(96.4522f, 96.5301f, progress))
    cubicTo(lerp(47.008f, 47.0644f, progress), lerp(94.5122f, 94.5908f, progress), lerp(46.5926f, 46.646f, progress), lerp(92.2296f, 92.3085f, progress), lerp(46.9791f, 47.0297f, progress), lerp(90.0173f, 90.0963f, progress))
    cubicTo(lerp(47.256f, 47.3046f, progress), lerp(88.4212f, 88.5002f, progress), lerp(48.1984f, 48.2446f, progress), lerp(86.4254f, 86.5042f, progress), lerp(48.8413f, 48.8856f, progress), lerp(84.896f, 84.9746f, progress))
    lineTo(lerp(51.3242f, 51.3612f, progress), lerp(78.9638f, 79.042f, progress))
    lineTo(lerp(59.0434f, 59.0576f, progress), lerp(60.4716f, 60.5484f, progress))
    lineTo(lerp(62.5118f, 62.5157f, progress), lerp(52.174f, 52.2501f, progress))
    cubicTo(lerp(63.0384f, 63.0408f, progress), lerp(50.914f, 50.99f, progress), lerp(63.6225f, 63.6233f, progress), lerp(49.6182f, 49.6941f, progress), lerp(64.0823f, 64.0815f, progress), lerp(48.353f, 48.4288f, progress))
    cubicTo(lerp(65.7727f, 65.7662f, progress), lerp(43.702f, 43.7776f, progress), lerp(64.465f, 64.4515f, progress), lerp(38.3151f, 38.3917f, progress), lerp(60.7207f, 60.7027f, progress), lerp(35.0214f, 35.0998f, progress))
    cubicTo(lerp(58.7922f, 58.7719f, progress), lerp(33.3251f, 33.4045f, progress), lerp(56.9798f, 56.9586f, progress), lerp(32.71f, 32.7902f, progress), lerp(54.686f, 54.6633f, progress), lerp(31.737f, 31.8182f, progress))
    lineTo(lerp(48.424f, 48.3974f, progress), lerp(29.0848f, 29.1687f, progress))
    cubicTo(lerp(47.053f, 47.0256f, progress), lerp(28.5044f, 28.589f, progress), lerp(45.6571f, 45.6288f, progress), lerp(27.9424f, 28.0277f, progress), lerp(44.3201f, 44.2909f, progress), lerp(27.3162f, 27.402f, progress))
    cubicTo(lerp(35.5906f, 35.5554f, progress), lerp(23.2272f, 23.317f, progress), lerp(34.8472f, 34.7965f, progress), lerp(11.0605f, 11.1516f, progress), lerp(43.0257f, 42.9693f, progress), lerp(6.03383f, 6.12213f, progress))
    cubicTo(lerp(46.0934f, 46.0349f, progress), lerp(4.14829f, 4.2355f, progress), lerp(49.3675f, 49.3088f, progress), lerp(3.79763f, 3.88354f, progress), lerp(52.7889f, 52.7318f, progress), lerp(4.73937f, 4.8238f, progress))
    close()
}

private fun completeLogoHeadPath(progress: Float): Path = Path().apply {
    moveTo(lerp(117.866f, 117.84f, progress), lerp(32.303f, 32.3214f, progress))
    cubicTo(lerp(119.94f, 119.915f, progress), lerp(33.0792f, 33.0968f, progress), lerp(121.983f, 121.96f, progress), lerp(34.0268f, 34.0436f, progress), lerp(124.024f, 124.002f, progress), lerp(34.8935f, 34.9093f, progress))
    cubicTo(lerp(125.391f, 125.37f, progress), lerp(35.4738f, 35.4891f, progress), lerp(126.885f, 126.865f, progress), lerp(35.9867f, 36.0014f, progress), lerp(128.181f, 128.162f, progress), lerp(36.7058f, 36.7199f, progress))
    cubicTo(lerp(128.626f, 128.608f, progress), lerp(36.9527f, 36.9667f, progress), lerp(129.081f, 129.063f, progress), lerp(37.2732f, 37.2869f, progress), lerp(129.423f, 129.406f, progress), lerp(37.6528f, 37.6664f, progress))
    cubicTo(lerp(129.97f, 129.953f, progress), lerp(38.2578f, 38.2711f, progress), lerp(130.181f, 130.165f, progress), lerp(39.1804f, 39.1937f, progress), lerp(130.126f, 130.111f, progress), lerp(39.9759f, 39.9891f, progress))
    cubicTo(lerp(130.066f, 130.052f, progress), lerp(40.8395f, 40.8527f, progress), lerp(123.973f, 123.977f, progress), lerp(55.4864f, 55.5017f, progress), lerp(123.172f, 123.178f, progress), lerp(57.1004f, 57.116f, progress))
    cubicTo(lerp(122.917f, 122.924f, progress), lerp(57.6153f, 57.631f, progress), lerp(122.658f, 122.666f, progress), lerp(58.0685f, 58.0842f, progress), lerp(122.206f, 122.214f, progress), lerp(58.436f, 58.4519f, progress))
    cubicTo(lerp(121.372f, 121.38f, progress), lerp(59.1142f, 59.1304f, progress), lerp(120.466f, 120.474f, progress), lerp(59.2343f, 59.251f, progress), lerp(119.425f, 119.433f, progress), lerp(59.1062f, 59.1232f, progress))
    cubicTo(lerp(117.464f, 117.471f, progress), lerp(58.5917f, 58.6095f, progress), lerp(115.396f, 115.401f, progress), lerp(57.4993f, 57.518f, progress), lerp(113.533f, 113.536f, progress), lerp(56.6767f, 56.6962f, progress))
    cubicTo(lerp(112.031f, 112.033f, progress), lerp(56.0139f, 56.034f, progress), lerp(110.414f, 110.416f, progress), lerp(55.4034f, 55.4242f, progress), lerp(108.978f, 108.978f, progress), lerp(54.6177f, 54.6391f, progress))
    cubicTo(lerp(108.508f, 108.508f, progress), lerp(54.3608f, 54.3824f, progress), lerp(108.045f, 108.045f, progress), lerp(54.0264f, 54.0482f, progress), lerp(107.679f, 107.678f, progress), lerp(53.6334f, 53.6554f, progress))
    cubicTo(lerp(107.182f, 107.18f, progress), lerp(53.1009f, 53.123f, progress), lerp(107.008f, 107.005f, progress), lerp(52.249f, 52.2713f, progress), lerp(107.056f, 107.052f, progress), lerp(51.5414f, 51.5637f, progress))
    cubicTo(lerp(107.133f, 107.127f, progress), lerp(50.4206f, 50.4429f, progress), lerp(113.1f, 113.078f, progress), lerp(36.0711f, 36.0914f, progress), lerp(114.114f, 114.089f, progress), lerp(34.1196f, 34.1395f, progress))
    cubicTo(lerp(114.348f, 114.323f, progress), lerp(33.6691f, 33.689f, progress), lerp(114.599f, 114.573f, progress), lerp(33.2943f, 33.314f, progress), lerp(114.992f, 114.966f, progress), lerp(32.969f, 32.9886f, progress))
    cubicTo(lerp(115.841f, 115.814f, progress), lerp(32.2673f, 32.2866f, progress), lerp(116.808f, 116.782f, progress), lerp(32.1859f, 32.2048f, progress), lerp(117.866f, 117.84f, progress), lerp(32.303f, 32.3214f, progress))
    close()
}

private fun completeWave59Path(progress: Float): Path = Path().apply {
    moveTo(lerp(48.9794f, 81.553f, progress), lerp(90.1032f, -31.5443f, progress))
    cubicTo(lerp(33.4961f, 53.2636f, progress), lerp(91.3071f, -26.2264f, progress), lerp(11.5146f, 13.1016f, progress), lerp(103.445f, 27.3882f, progress), lerp(11.5146f, 13.1016f, progress), lerp(103.445f, 27.3882f, progress))
    lineTo(lerp(11.5146f, 13.1016f, progress), lerp(124.412f, 120f, progress))
    lineTo(lerp(148.039f, 262.543f, progress), lerp(124.412f, 120f, progress))
    lineTo(lerp(148.039f, 262.543f, progress), lerp(92.6444f, -20.3194f, progress))
    cubicTo(lerp(148.039f, 262.543f, progress), lerp(92.6444f, -20.3194f, progress), lerp(123.271f, 217.29f, progress), lerp(107.904f, 47.0846f, progress), lerp(105.493f, 184.808f, progress), lerp(109.164f, 52.6467f, progress))
    cubicTo(lerp(82.261f, 142.361f, progress), lerp(110.809f, 59.9151f, progress), lerp(72.1994f, 123.978f, progress), lerp(88.2976f, -39.5194f, progress), lerp(48.9794f, 81.553f, progress), lerp(90.1032f, -31.5443f, progress))
    close()
}

private fun completeWave58Path(progress: Float): Path = Path().apply {
    moveTo(lerp(54.6943f, 74.0591f, progress), lerp(100.103f, -16.5491f, progress))
    cubicTo(lerp(39.211f, 44.692f, progress), lerp(101.307f, -11.2871f, progress), lerp(17.2295f, 3f, progress), lerp(113.445f, 41.763f, progress), lerp(17.2295f, 3f, progress), lerp(113.445f, 41.763f, progress))
    lineTo(lerp(17.2295f, 3f, progress), lerp(134.412f, 133.4f, progress))
    lineTo(lerp(153.754f, 261.944f, progress), lerp(134.412f, 133.4f, progress))
    lineTo(lerp(153.754f, 261.944f, progress), lerp(102.644f, -5.44237f, progress))
    cubicTo(lerp(153.754f, 261.944f, progress), lerp(102.644f, -5.44237f, progress), lerp(128.986f, 214.967f, progress), lerp(117.904f, 61.2522f, progress), lerp(111.208f, 181.248f, progress), lerp(119.164f, 66.7557f, progress))
    cubicTo(lerp(87.9759f, 137.184f, progress), lerp(120.809f, 73.9475f, progress), lerp(77.9143f, 118.1f, progress), lerp(98.2976f, -24.4403f, progress), lerp(54.6943f, 74.0591f, progress), lerp(100.103f, -16.5491f, progress))
    close()
}

private fun lerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun OnboardingCompletePreview() {
    TalkQQuestTheme {
        OnboardingCompleteScreen()
    }
}
