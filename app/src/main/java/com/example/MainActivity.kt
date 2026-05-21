package com.gumah.imanypro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.CompositionLocalProvider
import com.gumah.imanypro.ui.theme.LocalAccentColor
import com.gumah.imanypro.ui.theme.LocalQuranFontFamily
import com.gumah.imanypro.ui.theme.LocalBackgroundOption
import com.gumah.imanypro.ui.components.AnimatedIslamicBackground
import com.gumah.imanypro.ui.components.GlassCard
import com.gumah.imanypro.ui.screens.*
import com.gumah.imanypro.ui.theme.MyApplicationTheme
import com.gumah.imanypro.ui.theme.EmeraldPrimary
import com.gumah.imanypro.ui.theme.GoldHex
import com.gumah.imanypro.ui.theme.TextWhite
import com.gumah.imanypro.ui.theme.DeepJadeBackground
import com.gumah.imanypro.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        if (locationGranted) {
            Toast.makeText(this, "تم تفعيل حساب المواقيت التفاعلي بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            val uiCustomizationViewModel: UiCustomizationViewModel = viewModel()
            val accentColorOption by uiCustomizationViewModel.currentAccent.collectAsState()
            val fontOption by uiCustomizationViewModel.currentFont.collectAsState()
            val backgroundOption by uiCustomizationViewModel.currentBackground.collectAsState()

            val activeAccent = accentColorOption.color
            val activeFontFamily = uiCustomizationViewModel.getComposeFontFamily(fontOption)

            CompositionLocalProvider(
                LocalAccentColor provides activeAccent,
                LocalQuranFontFamily provides activeFontFamily,
                LocalBackgroundOption provides backgroundOption
            ) {
                MyApplicationTheme {
                    MainAppContainer(uiCustomizationViewModel)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }
}

// Global identifiers for tabs
const val SCREEN_SPLASH = "splash"
const val SCREEN_ONBOARDING = "onboarding"
const val TAB_HOME = "home"
const val TAB_QURAN = "quran"
const val TAB_PHOTO_QURAN = "photo_quran"
const val TAB_AUDIO = "audio"
const val TAB_DHIKR = "dhikr"
const val TAB_TASBIH = "tasbih"
const val TAB_RADIO = "radio"
const val TAB_SETTING = "setting"
const val VIEW_LAILAT = "lailat"

@Composable
fun MainAppContainer(uiCustomizationViewModel: UiCustomizationViewModel) {
    val context = LocalContext.current
    
    // ViewModels instantiations
    val prayerViewModel: PrayerViewModel = viewModel()
    val quranViewModel: QuranViewModel = viewModel()
    val audioViewModel: AudioViewModel = viewModel()
    val dhikrDuaViewModel: DhikrDuaViewModel = viewModel()
    val tasbihViewModel: TasbihViewModel = viewModel()

    // Navigation and screen stack history state
    var currentScreen by remember { mutableStateOf(SCREEN_SPLASH) }
    val navigationHistory = remember { mutableStateListOf<String>() }

    fun navigateTo(destination: String) {
        if (currentScreen != destination) {
            navigationHistory.add(currentScreen)
            currentScreen = destination
        }
    }

    // Physical hardware Back Button interceptor
    BackHandler(enabled = true) {
        if (currentScreen == SCREEN_SPLASH || currentScreen == SCREEN_ONBOARDING) {
            // Can't go back further on intro views, exit app
            (context as? ComponentActivity)?.finish()
        } else if (currentScreen == TAB_HOME) {
            // Exit if on main home and back pressed, or pop history if any
            if (navigationHistory.isNotEmpty()) {
                val last = navigationHistory.removeAt(navigationHistory.size - 1)
                currentScreen = last
            } else {
                (context as? ComponentActivity)?.finish()
            }
        } else if (currentScreen == TAB_PHOTO_QURAN) {
            // Return to quran textual listing
            currentScreen = TAB_QURAN
        } else {
            // Default popped back stack behavior
            if (navigationHistory.isNotEmpty()) {
                val last = navigationHistory.removeAt(navigationHistory.size - 1)
                currentScreen = last
            } else {
                currentScreen = TAB_HOME
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Hide bottom bar on intro scenes
            if (currentScreen != SCREEN_SPLASH && currentScreen != SCREEN_ONBOARDING) {
                GlassmorphicBottomNav(
                    currentTab = currentScreen,
                    onTabSelected = { selected ->
                        navigateTo(selected)
                    }
                )
            }
        },
        containerColor = DeepJadeBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Render dynamic background if on home screen or quran views
            if (currentScreen == TAB_HOME || currentScreen == TAB_QURAN || currentScreen == TAB_PHOTO_QURAN || currentScreen == TAB_SETTING) {
                AnimatedIslamicBackground(
                    option = LocalBackgroundOption.current,
                    modifier = Modifier.fillMaxSize()
                )
            }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "mainAppNavigator"
            ) { target ->
                when (target) {
                    SCREEN_SPLASH -> SplashScreen(onNavigateToOnboarding = {
                        currentScreen = SCREEN_ONBOARDING
                    })
                    SCREEN_ONBOARDING -> OnboardingScreen(onNavigateToHome = {
                        currentScreen = TAB_HOME
                    })
                    TAB_HOME -> HomeScreen(
                        prayerViewModel = prayerViewModel,
                        onNavigateToSection = { path -> navigateTo(path) }
                    )
                    TAB_QURAN -> QuranScreen(
                        quranViewModel = quranViewModel,
                        onNavigateToPhotoQuran = { navigateTo(TAB_PHOTO_QURAN) }
                    )
                    TAB_PHOTO_QURAN -> PhotoQuranScreen(onBack = {
                        currentScreen = TAB_QURAN
                    })
                    TAB_AUDIO -> AudioRecitersScreen(audioViewModel = audioViewModel)
                    TAB_DHIKR -> DhikrDuaScreen(dhikrDuaViewModel = dhikrDuaViewModel)
                    TAB_TASBIH -> TasbihScreen(tasbihViewModel = tasbihViewModel)
                    TAB_RADIO -> RadioScreen(audioViewModel = audioViewModel)
                    TAB_SETTING -> SettingsScreen(
                        prayerViewModel = prayerViewModel,
                        dhikrDuaViewModel = dhikrDuaViewModel,
                        uiCustomizationViewModel = uiCustomizationViewModel
                    )
                }
            }

            // Quick Floating Banner to access Laylat al-Qadr with high spiritual visibility
            if (currentScreen == TAB_HOME) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 90.dp, end = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { navigateTo(TAB_DHIKR) }, // Navigates to prayers/Azkar where users can tap tabs
                        containerColor = GoldHex,
                        contentColor = Color.Black,
                        shape = CircleShape
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الأذكار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// GLASSMORPHISM BOTTOM NAVIGATION COMPONENT
// ---------------------------------------------------------------------
data class NavItem(val id: String, val title: String, val icon: ImageVector)

@Composable
fun GlassmorphicBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        NavItem(TAB_HOME, "الرئيسية", Icons.Default.Home),
        NavItem(TAB_QURAN, "القرآن", Icons.Default.MenuBook),
        NavItem(TAB_AUDIO, "التلاوات", Icons.Default.Radio),
        NavItem(TAB_DHIKR, "الأذكار", Icons.Default.AutoAwesome),
        NavItem(TAB_TASBIH, "المسبحة", Icons.Default.Fingerprint),
        NavItem(TAB_SETTING, "تطبيقنا", Icons.Default.Settings)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .windowInsetsPadding(WindowInsets.navigationBars) // Support Edge-to-Edge bar offsets
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0x16FFFFFF)) // 9% transparent premium glass background
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(32.dp))
            .background(Brush.radialGradient(listOf(Color(0x1AD4AF37), Color.Transparent), radius = 250f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isActive = currentTab == item.id || (item.id == TAB_QURAN && currentTab == TAB_PHOTO_QURAN)
                val activeSizeMultiplier by animateFloatAsState(if (isActive) 1.2f else 1f, label = "activeIconAnim")
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(item.id) }
                        .padding(4.dp)
                        .graphicsLayer(scaleX = activeSizeMultiplier, scaleY = activeSizeMultiplier)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isActive) GoldHex else Color.Gray.copy(0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.title,
                        color = if (isActive) GoldHex else Color.Gray.copy(0.8f),
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )

                    // Glow circle indicator
                    if (isActive) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(GoldHex, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
