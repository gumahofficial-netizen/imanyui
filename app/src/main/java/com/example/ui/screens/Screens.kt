package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import androidx.compose.ui.text.font.FontFamily
import java.util.Calendar
import com.example.data.repository.ImanyRepository
import kotlinx.coroutines.launch

private val GoldHex: Color @Composable get() = LocalAccentColor.current
private val QuranFontFamily: FontFamily @Composable get() = LocalQuranFontFamily.current

// -------------------------------------------------------------
// 1. SPLASH SCREEN
// -------------------------------------------------------------
@Composable
fun SplashScreen(onNavigateToOnboarding: () -> Unit) {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val opacity by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200),
        label = "logoOpacity"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        kotlinx.coroutines.delay(2200)
        onNavigateToOnboarding()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepJadeBackground, Color(0xFF040F0A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Star particle ambient backgrounds
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(
                color = EmeraldPrimary.copy(alpha = 0.15f),
                radius = 300f,
                center = center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale, alpha = opacity)
        ) {
            Image(
                imageVector = Icons.Default.Star, // Vector backup
                contentDescription = "Logo",
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0x1FDFB76C), CircleShape)
                    .padding(16.dp)
                    .border(2.dp, GoldHex, CircleShape),
                colorFilter = ColorFilter.tint(GoldHex)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "إيماني",
                color = GoldHex,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color(0x66DFB76C),
                        offset = Offset(0f, 4f),
                        blurRadius = 12f
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "دليلك الروحي اليومي للعبادة",
                color = TextWhite.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------
// 2. ONBOARDING SCREEN
// -------------------------------------------------------------
@Composable
fun OnboardingScreen(onNavigateToHome: () -> Unit) {
    val pages = listOf(
        Pair("مواقيت صلاة دقيقة", "حساب مواقيت الصلاة تلقائياً بناءً على موقعك الجغرافي الرياضي وبدون الحاجة لإنترنت."),
        Pair("تلاوات ومصاحف فاخرة", "استمع لأكثر من 230 قارئ متميز مع واجهة تشغيل خلفية ونظام مصحف ورقي بصري."),
        Pair("أذكار وتسبيح ذكي", "احتفظ بأورادك وأذكارك اليومية مع مسبحة إلكترونية تفاعلية تحفظ عدادك تلقائياً.")
    )
    
    var currentPage by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepJadeBackground, SurfaceDarkJade)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IslamicTitle(
                title = "تطبيق إيماني الأصيل",
                subtitle = "بوابة زجاجية لخشوع قلبك"
            )

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "onboardTransition"
            ) { pageIdx ->
                val page = pages[pageIdx]
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    backgroundColor = Color(0x1FDFB76C)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = if (pageIdx == 0) Icons.Default.MenuBook else if (pageIdx == 1) Icons.Default.MusicNote else Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = GoldHex,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = page.first,
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = page.second,
                                color = TextWhite.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Indicator Dots
            Row(horizontalArrangement = Arrangement.Center) {
                pages.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (idx == currentPage) 12.dp else 8.dp)
                            .background(
                                color = if (idx == currentPage) GoldHex else Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage < pages.size - 1) {
                    GlassButton(
                        text = "التالي",
                        onClick = { currentPage++ },
                        isPrimary = true
                    )
                    GlassButton(
                        text = "تخطي",
                        onClick = onNavigateToHome,
                        isPrimary = false
                    )
                } else {
                    GlassButton(
                        text = "ابدأ الاستخدام",
                        onClick = onNavigateToHome,
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. HOME SCREEN WITH PRAYER CALCULATOR
// -------------------------------------------------------------
@Composable
fun HomeScreen(
    prayerViewModel: PrayerViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val prayerTimesList by prayerViewModel.prayerTimes.collectAsState()
    val city by prayerViewModel.currentCity.collectAsState()
    val country by prayerViewModel.currentCountry.collectAsState()
    val hijri by prayerViewModel.hijriDate.collectAsState()
    val gregorian by prayerViewModel.gregorianDate.collectAsState()
    val hadith by prayerViewModel.dailyHadith.collectAsState()
    val randomDhikr by prayerViewModel.dailyDhikr.collectAsState()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Header / Greeting Row matching top-bar simulation
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إيماني",
                        color = GoldBright,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color(0x66D4AF37),
                                offset = Offset(0f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )
                    Text(
                        text = "السلام عليكم ورحمة الله وبركاته",
                        color = TextWhite.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
                
                // Standalone elegant refresh/notch-simulated button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x12FFFFFF))
                        .border(1.dp, Color(0x1BFFFFFF), CircleShape)
                        .clickable {
                            prayerViewModel.refreshHomeData()
                            Toast.makeText(context, "تم تجديد مواقيت الصلاة", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = GoldBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 1. Next Prayer Card (Integrated Glassmorphism)
        item {
            val calendar = Calendar.getInstance()
            val curHour = calendar.get(Calendar.HOUR_OF_DAY)
            val curMin = calendar.get(Calendar.MINUTE)
            val curMinutes = curHour * 60 + curMin

            // Dynamic Next Prayer Locator
            val nextPrayer = prayerTimesList.firstOrNull { p ->
                try {
                    val pTime = p.time.trim()
                    val parts = pTime.split(":")
                    if (parts.size == 2) {
                        val h = parts[0].trim().toInt()
                        val m = parts[1].trim().toInt()
                        (h * 60 + m) > curMinutes
                    } else false
                } catch(e: Exception) { false }
            } ?: prayerTimesList.firstOrNull { it.name == "الفجر" } ?: ImanyRepository.PrayerTime("العصر", "03:42")

            val isPM = try {
                val h = nextPrayer.time.split(":")[0].trim().toInt()
                h >= 12
            } catch (e: Exception) { true }
            val amPmIndicator = if (isPM) "م" else "ص"

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                borderColor = Color(0x26FFFFFF),
                backgroundColor = Color(0x0FFFFFFF)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Next Prayer Title and Location badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "صلاة ${nextPrayer.name} القادمة",
                            color = TextWhite.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0x1AD4AF37), RoundedCornerShape(50))
                                .border(1.dp, Color(0x33D4AF37), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${city}، ${country}",
                                color = GoldBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Next Prayer Time Display
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = nextPrayer.time,
                            color = Color.White,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = Color(0x33FFFFFF),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 6f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = amPmIndicator,
                            color = GoldBright,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x1BFFFFFF))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // All Prayer Times Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        prayerTimesList.take(6).forEach { prayer ->
                            val isActive = prayer.name == nextPrayer.name
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(GoldBright, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                
                                Text(
                                    text = prayer.name,
                                    color = if (isActive) GoldBright else TextWhite.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = prayer.time,
                                    color = if (isActive) GoldBright else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Dates Card (Side-by-side luxurious layout)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hijri Date Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF064E3B))
                        .border(1.dp, Color(0xFF065F46), RoundedCornerShape(20.dp))
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "هجري",
                            color = TextWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hijri,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Gregorian Date Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF064E3B))
                        .border(1.dp, Color(0xFF065F46), RoundedCornerShape(20.dp))
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ميلادي",
                            color = TextWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gregorian,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Hadith card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GoldHex.copy(alpha = 0.3f),
                backgroundColor = Color(0x0EFFFFFF)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldHex)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الحديث النبوي اليومي",
                            color = GoldHex,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hadith,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontFamily = QuranFontFamily,
                        lineHeight = 24.sp,
                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                    )
                }
            }
        }

        // 4. Random Daily Dhikr card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = EmeraldPrimary.copy(alpha = 0.3f),
                backgroundColor = Color(0x0EFFFFFF)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ورد قلبك وذكرك اليوم",
                            color = EmeraldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = randomDhikr,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontFamily = QuranFontFamily,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // 5. App Modules Navigation Grid Shortcuts
        item {
            IslamicTitle(title = "أدوات إيماني الذكية", subtitle = "تنقل سريع لأذكار العبادة والقرآن")
            Spacer(modifier = Modifier.height(8.dp))

            val shortcuts = listOf(
                Triple("القرآن الكريم", Icons.Default.MenuBook, "quran"),
                Triple("مصحف الصور", Icons.Default.LibraryBooks, "photo_quran"),
                Triple("التلاوات الصوتية", Icons.Default.MusicNote, "audio"),
                Triple("الأذكار اليومية", Icons.Default.AutoAwesome, "dhikr"),
                Triple("المسبحة الرقمية", Icons.Default.Fingerprint, "tasbih"),
                Triple("راديو البث المباشر", Icons.Default.Radio, "radio")
            )

            val iconColors = listOf(
                Color(0xFFD4AF37), // Quran
                Color(0xFF10B981), // Photo Quran
                Color(0xFF818CF8), // Audio
                Color(0xFFF43F5E), // Azkar
                Color(0xFFFB923C), // Tasbih
                Color(0xFF22D3EE)  // Radio
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(shortcuts) { item ->
                    val index = shortcuts.indexOf(item)
                    val color = iconColors[if (index != -1) index % iconColors.size else 0]
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0x0FFFFFFF))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                            .clickable { onNavigateToSection(item.third) }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Icon Container with 15% transparent color to match mock style exactly
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(color.copy(alpha = 0.15f))
                                    .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.second,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.first,
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. QURAN TEXTUAL READER
// -------------------------------------------------------------
@Composable
fun QuranScreen(
    quranViewModel: QuranViewModel,
    onNavigateToPhotoQuran: () -> Unit
) {
    val surahs by quranViewModel.surahs.collectAsState()
    val activeAyahs by quranViewModel.activeAyahs.collectAsState()
    val activeSurah by quranViewModel.activeSurah.collectAsState()
    val isLoading by quranViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var viewingSurahDetail by remember { mutableStateOf(false) }

    val filteredSurahs = surahs.filter {
        it.name.contains(searchQuery) || it.englishName.contains(searchQuery, ignoreCase = true)
    }

    if (viewingSurahDetail && activeSurah != null) {
        // Surah detail view
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewingSurahDetail = false }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldHex)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "سورة ${activeSurah!!.name}",
                        color = GoldHex,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${activeSurah!!.englishName} • ${activeSurah!!.ayat} آية",
                        color = TextWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldHex)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Basmala for non-Tawbah surahs
                    if (activeSurah!!.id != 9) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    color = GoldHex,
                                    fontSize = 24.sp,
                                    fontFamily = QuranFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    items(activeAyahs) { ayah ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0x33DFB76C), CircleShape)
                                            .size(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(ayah.number.toString(), color = GoldHex, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "الجزء ${ayah.juz}",
                                        color = TextWhite.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ayah.text,
                                    color = TextWhite,
                                    fontSize = 21.sp,
                                    fontFamily = QuranFontFamily,
                                    textAlign = TextAlign.Right,
                                    lineHeight = 38.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Quran list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IslamicTitle(title = "سور القرآن الكريم", subtitle = "قراءة الآيات برسم المصحف الشريف", alignment = Alignment.Start)
                GlassButton(text = "مصحف الصور", onClick = onNavigateToPhotoQuran, isPrimary = false)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن سورة بفحص الاسم...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldHex) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldHex,
                    unfocusedBorderColor = Color(0x33FFFFFF)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(6) { SkeletonLoadingItem() }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSurahs) { surah ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    quranViewModel.loadSurahText(surah)
                                    viewingSurahDetail = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0x1FDFB76C), CircleShape)
                                            .border(1.dp, GoldHex, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(surah.id.toString(), color = GoldHex, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(surah.englishName, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${if (surah.type == "Meccan") "مكية" else "مدنية"} • ${surah.ayat} آية",
                                            color = TextWhite.copy(alpha = 0.5f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Text(
                                    text = surah.name,
                                    color = GoldHex,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. PHOTO QURAN SCREEN WITH ZOOM & SWIPE & DARK FILTER
// -------------------------------------------------------------
@Composable
fun PhotoQuranScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var currentPage by remember { mutableIntStateOf(1) }
    var nightModeEnabled by remember { mutableStateOf(true) }
    
    // Zoom configurations
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val startSurahsPages = listOf(
        Pair("1. الفاتحة", 1),
        Pair("2. البقرة", 2),
        Pair("18. الكهف", 293),
        Pair("36. يس", 440),
        Pair("67. الملك", 562),
        Pair("112. الإخلاص", 604)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (nightModeEnabled) Color(0xFF030704) else Color(0xFFF9F7F5))
    ) {
        // Navigation Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Back", 
                        tint = if (nightModeEnabled) GoldHex else TextDarkGreen
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "المصحف الشريف المصور",
                        color = if (nightModeEnabled) GoldHex else TextDarkGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "صفحة رقم $currentPage / 604",
                        color = if (nightModeEnabled) TextWhite.copy(0.6f) else TextMutedDark,
                        fontSize = 12.sp
                    )
                }
            }

            Row {
                IconButton(onClick = { nightModeEnabled = !nightModeEnabled }) {
                    Icon(
                        imageVector = if (nightModeEnabled) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Night Mode",
                        tint = if (nightModeEnabled) GoldHex else TextDarkGreen
                    )
                }
                IconButton(onClick = {
                    scale = 1f
                    offset = Offset.Zero
                    Toast.makeText(context, "تم إعادة تعيين الأبعاد لقياسها الأصلي", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        Icons.Default.ZoomOutMap,
                        contentDescription = "Reset Zoom",
                        tint = if (nightModeEnabled) GoldHex else TextDarkGreen
                    )
                }
            }
        }

        // Surah Shortcuts Quick Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(startSurahsPages) { group ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (nightModeEnabled) Color(0x33DFB76C) else Color(0x1A0F9D58)
                        )
                        .border(
                            1.dp, 
                            if (nightModeEnabled) GoldHex else EmeraldPrimary, 
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            currentPage = group.second
                            scale = 1f
                            offset = Offset.Zero
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        group.first,
                        color = if (nightModeEnabled) GoldHex else TextDarkGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Display Area (Zoom and Pinchable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3.5f)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentAlignment = Alignment.Center
        ) {
            // Elegant image source using standard page indexing format quranPagesImage
            val imageUrl = "https://quran.yousefheiba.com/api/quranPagesImage?page=$currentPage"
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Quran Page $currentPage",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .blur(if (scale > 2f) 0.1.dp else 0.dp),
                contentScale = ContentScale.Fit,
                colorFilter = if (nightModeEnabled) {
                    // Vintage protective sepia dark tones
                    ColorFilter.tint(Color(0xFFEFE8D3).copy(0.9f), androidx.compose.ui.graphics.BlendMode.Multiply)
                } else null
            )
        }

        // Pager Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassButton(
                text = "الصفحة السابقة",
                onClick = {
                    if (currentPage > 1) {
                        currentPage--
                        scale = 1f
                        offset = Offset.Zero
                    }
                },
                enabled = currentPage > 1,
                isPrimary = false
            )

            Text(
                "$currentPage / 604",
                color = if (nightModeEnabled) GoldHex else TextDarkGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )

            GlassButton(
                text = "الصفحة التالية",
                onClick = {
                    if (currentPage < 604) {
                        currentPage++
                        scale = 1f
                        offset = Offset.Zero
                    }
                },
                enabled = currentPage < 604,
                isPrimary = true
            )
        }
    }
}

// -------------------------------------------------------------
// 6. AUDIO RECITERS & MP3 PLAYER DOCK
// -------------------------------------------------------------
@Composable
fun AudioRecitersScreen(audioViewModel: AudioViewModel) {
    val reciters by audioViewModel.reciters.collectAsState()
    val isLoading by audioViewModel.isLoading.collectAsState()
    val activeClips by audioViewModel.reciterAudios.collectAsState()
    val selectedReciter by audioViewModel.selectedReciter.collectAsState()

    val playState = audioViewModel.playState
    val isPlaying by playState.isPlayingFlow.collectAsState()
    val currentTitle by playState.currentTitleFlow.collectAsState()
    val currentSubtitle by playState.currentSubtitleFlow.collectAsState()
    val currentProgress by playState.currentProgressFlow.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showingClips by remember { mutableStateOf(false) }

    val filteredReciters = reciters.filter {
        it.name.contains(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (showingClips && selectedReciter != null) {
            // Reciter sound list
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { showingClips = false }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldHex)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("ترتيل القارئ ${selectedReciter!!.name}", color = GoldHex, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("تلاوة مرتلة جودة عالية MP3", color = TextWhite.copy(0.6f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldHex)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(activeClips) { clip ->
                        val isThisActive = playState.activeTrackUrl.value == clip.url
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { audioViewModel.playTrack(clip) },
                            borderColor = if (isThisActive) GoldHex else Color(0x1AFFFFFF),
                            backgroundColor = if (isThisActive) Color(0x1A0F9D58) else Color(0x0FFFFFFF)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isThisActive && isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                        contentDescription = null,
                                        tint = if (isThisActive) GoldHex else Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = clip.title ?: "سورة رقم ${clip.surahId}",
                                            color = if (isThisActive) GoldHex else TextWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = selectedReciter!!.name,
                                            color = TextWhite.copy(0.5f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                if (isThisActive) {
                                    Text("قيد التشغيل", color = GoldHex, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // List reciters
            IslamicTitle(title = "قائمة القراء والمجودين", subtitle = "اختر من بين 230 قارئ من العالم الإسلامي", alignment = Alignment.Start)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن قارئ باسمه...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldHex) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldHex,
                    unfocusedBorderColor = Color(0x33FFFFFF)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(6) { SkeletonLoadingItem() }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredReciters) { reciter ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audioViewModel.selectReciter(reciter)
                                    showingClips = true
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .background(
                                            Brush.linearGradient(listOf(EmeraldPrimary, EmeraldMedium)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (reciter.letter ?: reciter.name.take(1)),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(reciter.name, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "رواية حفص عن عاصم • جودة ممتازة",
                                        color = TextWhite.copy(0.5f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Spotify Glass Dock
        if (playState.activeTrackUrl.value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                borderColor = GoldHex,
                backgroundColor = Color(0xCC08120E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentTitle, color = GoldHex, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(currentSubtitle, color = TextWhite.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        Row {
                            IconButton(onClick = { playState.onPrevious?.invoke() }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White)
                            }
                            IconButton(onClick = { playState.onPlayPause?.invoke() }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = GoldHex,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            IconButton(onClick = { playState.onNext?.invoke() }) {
                                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                    if (!playState.isRadioActive.value) {
                        Slider(
                            value = currentProgress,
                            onValueChange = { playState.onSeek?.invoke(it) },
                            colors = SliderDefaults.colors(
                                thumbColor = GoldHex,
                                activeTrackColor = GoldHex,
                                inactiveTrackColor = Color.Gray
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. DHIKR & DUAS TABS
// -------------------------------------------------------------
@Composable
fun DhikrDuaScreen(dhikrDuaViewModel: DhikrDuaViewModel) {
    val azkarGroups by dhikrDuaViewModel.azkarGroups.collectAsState()
    val duas by dhikrDuaViewModel.duasList.collectAsState()
    val favorites by dhikrDuaViewModel.favoriteDuas.collectAsState()
    val isLoading by dhikrDuaViewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Azkar, 1 = Duas, 2 = Favorites
    var azkarCategoryIndex by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IslamicTitle(title = "الأذكار والأدعية", subtitle = "حصن المسلم وأوراد الحماية والخشوع")
        Spacer(modifier = Modifier.height(16.dp))

        // Tabs Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = GoldHex
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("الأذكار اليومية", modifier = Modifier.padding(12.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("الأدعية المختارة", modifier = Modifier.padding(12.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("المفضلة (${favorites.size})", modifier = Modifier.padding(12.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(5) { SkeletonLoadingItem() }
            }
        } else {
            when (selectedTab) {
                0 -> {
                    // Azkar Category lists
                    if (azkarGroups.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            azkarGroups.forEachIndexed { index, group ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (azkarCategoryIndex == index) EmeraldPrimary else Color(0x33FFFFFF))
                                        .clickable { azkarCategoryIndex = index }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        group.category, 
                                        color = if (azkarCategoryIndex == index) Color.White else TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected group Azkar list
                        val currentAzkarGroup = azkarGroups.getOrNull(azkarCategoryIndex)
                        if (currentAzkarGroup != null) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(currentAzkarGroup.items) { item ->
                                    var currentCount by remember(item.zekr) { mutableIntStateOf(item.count) }
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { if (currentCount > 0) currentCount-- }
                                    ) {
                                        Column {
                                            Text(
                                                text = item.zekr,
                                                fontFamily = QuranFontFamily,
                                                color = TextWhite,
                                                fontSize = 16.sp,
                                                lineHeight = 22.sp,
                                                style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                                            )
                                            if (item.reference != null) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(item.reference, color = GoldHex, fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (currentCount == 0) Color.Gray else EmeraldPrimary, RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        if (currentCount == 0) "تم التكرار" else "المتبقي: $currentCount",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    val clip = ClipData.newPlainText("Zekr", item.zekr)
                                                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                    Toast.makeText(context, "تم نسخ الذكر", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GoldHex)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Duas List
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(duas) { dua ->
                            val isFav = favorites.any { it.id == dua.id }
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(dua.title, color = GoldHex, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { dhikrDuaViewModel.toggleFavoriteDua(dua) }) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Fav",
                                                tint = if (isFav) Color.Red else Color.White
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dua.text,
                                        fontFamily = QuranFontFamily,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        IconButton(onClick = {
                                            val clip = ClipData.newPlainText("Dua", "${dua.title}\n${dua.text}")
                                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                            Toast.makeText(context, "تم نسخ الدعاء للصق", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GoldHex)
                                        }
                                        IconButton(onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "${dua.title}\n${dua.text}")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الدعاء"))
                                        }) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", tint = GoldHex)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Favorites List
                    if (favorites.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("مفضلة الأدعية فارغة حالياً.", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(favorites) { fav ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(fav.title, color = GoldHex, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { 
                                                dhikrDuaViewModel.toggleFavoriteDua(Dua(fav.id, fav.title, fav.text)) 
                                            }) {
                                                Icon(Icons.Filled.Favorite, contentDescription = "Unfavorite", tint = Color.Red)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(fav.text, color = TextWhite, fontSize = 15.sp, lineHeight = 22.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. LAYLAT AL-QADR SCREEN
// -------------------------------------------------------------
@Composable
fun LaylatAlQadrScreen(dhikrDuaViewModel: DhikrDuaViewModel) {
    val response by dhikrDuaViewModel.laylatAlQadr.collectAsState()
    val context = LocalContext.current

    if (response == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldHex)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = GoldHex,
                    backgroundColor = Color(0x33000000)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldHex, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(response!!.title, color = GoldHex, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(response!!.merit, color = TextWhite, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    }
                }
            }

            // Signs List
            item {
                IslamicTitle(title = "علامات ليلة القدر الشريفة", subtitle = "دلالات مباركة مأثورة في السنة النبوية")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    response!!.signs.forEach { sign ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldHex, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(sign, color = TextWhite, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Recommended deeds
            item {
                IslamicTitle(title = "الأعمال المستحبة بالليل", subtitle = "اجتهادات روحانية لمضاعفة الأجر")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    response!!.deeds.forEach { deed ->
                        GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = EmeraldPrimary.copy(0.4f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(deed, color = TextWhite, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Holy prayers & Duas
            item {
                IslamicTitle(title = "أدعية ليلة القدر المستجابة", subtitle = "ردد دعاء النبي صلى الله عليه وسلم")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    response!!.duas.forEach { prayer ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(prayer, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    IconButton(onClick = {
                                        val clip = ClipData.newPlainText("LailatDua", prayer)
                                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                        Toast.makeText(context, "تم نسخ الدعاء الشريف", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GoldHex)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. RELIABLE ISLAMIC RADIO TAB
// -------------------------------------------------------------
@Composable
fun RadioScreen(audioViewModel: AudioViewModel) {
    val radios = audioViewModel.getRadios()
    val playState = audioViewModel.playState
    val activeTrackUrl by playState.activeTrackUrl.collectAsState()
    val isPlaying by playState.isPlayingFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IslamicTitle(title = "إذاعات الراديو الإسلامية", subtitle = "بث حي ومباشر للمحطات الإسلامية والقرآن الكريم")
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(radios) { channel ->
                val isThisActive = activeTrackUrl == channel.url
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { audioViewModel.selectRadio(channel) },
                    borderColor = if (isThisActive) GoldHex else Color(0x1AFFFFFF),
                    backgroundColor = if (isThisActive) Color(0x260F9D58) else Color(0x0FFFFFFF)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(
                                        if (isThisActive && isPlaying) EmeraldPrimary else Color(0x33FFFFFF),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isThisActive && isPlaying) Icons.Filled.VolumeUp else Icons.Filled.Radio,
                                    contentDescription = null,
                                    tint = if (isThisActive && isPlaying) Color.White else GoldHex
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(channel.name, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "بث صوتي مباشر عبر الإنترنت",
                                    color = TextWhite.copy(0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        if (isThisActive) {
                            Text(
                                text = if (isPlaying) "مباشر الآن" else "مؤقت",
                                color = GoldHex,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Dedicated Playback Stop card
        if (activeTrackUrl.isNotEmpty() && playState.isRadioActive.value) {
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                borderColor = GoldHex,
                backgroundColor = Color(0xCC08120E)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(playState.currentTitleFlow.value, color = GoldHex, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("بث إسلامي مباشر", color = TextWhite.copy(0.7f), fontSize = 11.sp)
                    }
                    IconButton(onClick = { playState.onPlayPause?.invoke() }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Playback",
                            tint = GoldHex,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 10. ELECTRONIC DIGITAL TASBIH (THE ACTIVE GLOWING 3D SHAPE)
// -------------------------------------------------------------
@Composable
fun TasbihScreen(tasbihViewModel: TasbihViewModel) {
    val counter by tasbihViewModel.counterValue.collectAsState()
    val target by tasbihViewModel.targetValue.collectAsState()
    val activeDhikr by tasbihViewModel.selectedDhikr.collectAsState()
    val tasbihsList by tasbihViewModel.tasbihs.collectAsState()

    var showPhrasesPopup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IslamicTitle(title = "المسبحة الإلكترونية التفاعلية", subtitle = "اهتزاز تفاعلي خفيف لحساب الأذكار والورد")
        Spacer(modifier = Modifier.height(24.dp))

        // Custom selection dropdown styled inside Glassmorphism
        Box {
            GlassCard(
                modifier = Modifier
                    .width(260.dp)
                    .clickable { showPhrasesPopup = !showPhrasesPopup },
                borderColor = GoldHex,
                backgroundColor = Color(0x1F000000)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(activeDhikr, color = GoldHex, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = GoldHex)
                }
            }

            DropdownMenu(
                expanded = showPhrasesPopup,
                onDismissRequest = { showPhrasesPopup = false },
                modifier = Modifier
                    .width(260.dp)
                    .background(Color(0xFF0F1E19))
                    .border(1.dp, BorderJade, RoundedCornerShape(8.dp))
            ) {
                val defaultPhrases = listOf("سبحان الله", "الحمد لله", "لا إله إلا الله", "الله أكبر", "أستغفر الله", "اللهم صلّ على محمد")
                defaultPhrases.forEach { phrase ->
                    DropdownMenuItem(
                        text = { Text(phrase, color = TextWhite, fontWeight = FontWeight.Bold) },
                        onClick = {
                            tasbihViewModel.selectDhikr(phrase)
                            showPhrasesPopup = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Glowing 3D Clicker Ring Circle
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(EmeraldPrimary.copy(0.2f), Color.Transparent),
                        radius = 350f
                    )
                )
                .border(6.dp, Brush.linearGradient(listOf(EmeraldPrimary, GoldHex)), CircleShape)
                .clickable { tasbihViewModel.incrementCounter() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$counter",
                    color = GoldHex,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = Color(0x800F9D58),
                            offset = Offset(0f, 4f),
                            blurRadius = 15f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الهدف: $target",
                    color = TextWhite.copy(0.7f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            GlassButton(text = "تصفير العداد", onClick = { tasbihViewModel.clearCounter() }, isPrimary = false)
            
            // Swap Target bounds
            listOf(33, 99, 1000).forEach { bound ->
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(if (target == bound) GoldHex else Color(0x1FFFFFFF))
                        .clickable { tasbihViewModel.changeTarget(bound) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$bound",
                        color = if (target == bound) Color.Black else TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// 11. OPTIONS & MANUAL COORDINATES
// -------------------------------------------------------------
@Composable
fun SettingsScreen(
    prayerViewModel: PrayerViewModel,
    dhikrDuaViewModel: DhikrDuaViewModel,
    uiCustomizationViewModel: UiCustomizationViewModel
) {
    val city by prayerViewModel.currentCity.collectAsState()
    val country by prayerViewModel.currentCountry.collectAsState()
    val context = LocalContext.current

    val currentAccent by uiCustomizationViewModel.currentAccent.collectAsState()
    val currentFont by uiCustomizationViewModel.currentFont.collectAsState()
    val currentBackground by uiCustomizationViewModel.currentBackground.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Header
        item {
            IslamicTitle(title = "إعدادات وتخصيص إيماني", subtitle = "خيارات مظهر الواجهة وألوان وخطوط التطبيق ومواقيت البلدان")
        }

        // 2. Active Location Indicator
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldHex)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("الموقع الجغرافي المحدد", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("$city، $country", color = GoldHex, fontSize = 13.sp)
                    }
                }
            }
        }

        // 3. UI Colors Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IslamicTitle(
                    title = "لوحة الألوان التفاعلية",
                    subtitle = "اختر أحد الألوان الروحانية البراقة لتخصيص واجهات ونصوص وأزرار التطبيق كاملة",
                    alignment = Alignment.Start
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccentColor.values().forEach { col ->
                        val isSelected = currentAccent == col
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(col.color)
                                .border(
                                    2.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    uiCustomizationViewModel.updateAccent(col)
                                    Toast.makeText(context, "تم تطبيق اللون: ${col.displayNameAr}", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Arabic Font Style Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IslamicTitle(
                    title = "أنماط الخطوط العربية والقرآنية",
                    subtitle = "اختر أفضل نمط لقراءة الآيات والأحاديث بيسر تام وسكينة مستحبة بالعين",
                    alignment = Alignment.Start
                )
                Spacer(modifier = Modifier.height(10.dp))
                FontOption.values().forEach { fontOpt ->
                    val isSelected = currentFont == fontOpt
                    val previewFont = uiCustomizationViewModel.getComposeFontFamily(fontOpt)
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                uiCustomizationViewModel.updateFont(fontOpt)
                                Toast.makeText(context, "تم تفعيل خط: ${fontOpt.displayNameAr}", Toast.LENGTH_SHORT).show()
                            },
                        borderColor = if (isSelected) GoldHex else Color(0x1AFFFFFF),
                        backgroundColor = if (isSelected) Color(0x11FFFFFF) else Color(0x06FFFFFF)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fontOpt.displayNameAr,
                                    color = if (isSelected) GoldHex else TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = fontOpt.description,
                                    color = TextWhite.copy(0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            // Preview text in actual font
                            Text(
                                text = "﴿الْحَمْدُ﴾",
                                color = GoldHex,
                                fontSize = 16.sp,
                                fontFamily = previewFont,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. Animated Backgrounds Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IslamicTitle(
                    title = "الخلفيات الروحانية المتحركة 60fps",
                    subtitle = "اختر مؤشرات تضفي انسيابية وجلالا على خلفيتي الشاشة الرئيسية وقارئ المصحف",
                    alignment = Alignment.Start
                )
                Spacer(modifier = Modifier.height(10.dp))
                BackgroundOption.values().forEach { bgOpt ->
                    val isSelected = currentBackground == bgOpt
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                uiCustomizationViewModel.updateBackground(bgOpt)
                                Toast.makeText(context, "تم اختيار مظهر ممتد: ${bgOpt.displayNameAr}", Toast.LENGTH_SHORT).show()
                            },
                        borderColor = if (isSelected) GoldHex else Color(0x1AFFFFFF),
                        backgroundColor = if (isSelected) Color(0x11FFFFFF) else Color(0x06FFFFFF)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bgOpt.displayNameAr,
                                    color = if (isSelected) GoldHex else TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = bgOpt.description,
                                    color = TextWhite.copy(0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldHex,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Change City List Section
        item {
            IslamicTitle(
                title = "تغيير مدينة التوقيت",
                subtitle = "اختر لتعديل مواقيت الصلاة تلقائياً حسب الموقع الجديد ومسارات الشمس",
                alignment = Alignment.Start
            )
        }

        val locationsMapping = listOf(
            Triple("مكة المكرمة", "المملكة العربية السعودية", Pair(21.4225, 39.8262)),
            Triple("القاهرة", "جمهورية مصر العربية", Pair(30.0444, 31.2357)),
            Triple("القدس الشريف", "فلسطين الأبية", Pair(31.7683, 35.2137)),
            Triple("الرياض", "المملكة العربية السعودية", Pair(24.7136, 46.6753)),
            Triple("بغداد", "العراق الشقيق", Pair(33.3152, 44.3661)),
            Triple("الرباط", "المغرب", Pair(34.0209, -6.8416))
        )

        items(locationsMapping) { loc ->
            val isActive = city == loc.first
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        prayerViewModel.updateLocationCoordinates(
                            lat = loc.third.first,
                            lng = loc.third.second,
                            city = loc.first,
                            country = loc.second
                        )
                        Toast.makeText(context, "تم تحويل المواقيت إلى سياق: ${loc.first}", Toast.LENGTH_SHORT).show()
                    },
                borderColor = if (isActive) GoldHex else Color(0x1AFFFFFF),
                backgroundColor = if (isActive) Color(0x1F0F9D58) else Color(0x0FFFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(loc.first, color = if (isActive) GoldHex else TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(loc.second, color = TextWhite.copy(0.5f), fontSize = 11.sp)
                    }
                    if (isActive) {
                        Text("النشط حالياً", color = GoldHex, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 7. Credits block
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x0CFFFFFF)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "تطبيق إيماني برو • النسخة المتقدمة",
                        color = GoldHex,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "صدقة جارية لجميع المسلمين والمسلمات.",
                        color = TextWhite.copy(0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
