package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.lifecycle.AndroidViewModel
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AccentColor(val displayNameAr: String, val color: Color, val secondaryColor: Color) {
    GOLD("ذهبي ملكي شريف", Color(0xFFD4AF37), Color(0xFF917215)),
    EMERALD("أخضر زمردي روحاني", Color(0xFF10B981), Color(0xFF047857)),
    TEAL("أزرق تيل هادئ", Color(0xFF14B8A6), Color(0xFF0F766E)),
    AMBER("كهرماني دافئ", Color(0xFFF59E0B), Color(0xFFB45309)),
    INDIGO("نيلي ليلي مبارك", Color(0xFF6366F1), Color(0xFF4338CA)),
    ROSE("وردي ناصع", Color(0xFFF43F5E), Color(0xFFBE123C))
}

enum class BackgroundOption(val displayNameAr: String, val description: String) {
    SILENT_COSMIC("الكون الهادئ المظلم", "خلفية داكنة غامرة توفر تركيزاً تاماً للتدبر والتأمل"),
    GOLD_STARFIELD("شهب وحقول الذهب", "أجرام ذهبية متناثرة تتحرك بلطف تضفي وقاراً إسلامياً"),
    FLOATING_LANTERNS("الفوانيس الطائرة العائمة", "فوانيس تقليدية تنبض وتصعد ببطء تملأ القلب سكينة"),
    EMERALD_WAVES("موجات الزمرد المضيئة", "خطوط هندسية مضيئة كأمواج سائلة تعبر بهدوء وجلال")
}

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

enum class FontOption(val displayNameAr: String, val fontName: String, val description: String) {
    AMIRI("خط الأميري النسخي الكلاسيكي", "Amiri", "الخط النسخي العربي الشهير والمثالي لقراءة النصوص الدينية بوضوح"),
    LATEEF("خط لطيف القرآني الجلي", "Lateef", "خط عربي يمتاز بجمالية الانسياب ومناسب جداً لعرض آيات الذكر الحكيم"),
    TAJAWAL("خط تجول العصري الهندسي", "Tajawal", "تصميم هندسي حديث ونظيف يناسب القراءة المعاصرة والأخبار اليومية"),
    CAIRO("خط القاهرة البارز الحديث", "Cairo", "خط عربي متميز بوضوح شديد وتناسق عالي جداً يريح العين")
}

@Stable
class UiCustomizationViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("ui_customization_prefs", Context.MODE_PRIVATE)

    private val _currentAccent = MutableStateFlow(getSavedAccent())
    val currentAccent: StateFlow<AccentColor> = _currentAccent.asStateFlow()

    private val _currentFont = MutableStateFlow(getSavedFont())
    val currentFont: StateFlow<FontOption> = _currentFont.asStateFlow()

    private val _currentBackground = MutableStateFlow(getSavedBackground())
    val currentBackground: StateFlow<BackgroundOption> = _currentBackground.asStateFlow()

    fun updateAccent(accent: AccentColor) {
        _currentAccent.value = accent
        sharedPrefs.edit().putString("accent_key", accent.name).apply()
    }

    fun updateFont(font: FontOption) {
        _currentFont.value = font
        sharedPrefs.edit().putString("font_key", font.name).apply()
    }

    fun updateBackground(background: BackgroundOption) {
        _currentBackground.value = background
        sharedPrefs.edit().putString("background_key", background.name).apply()
    }

    fun getComposeFontFamily(option: FontOption): FontFamily {
        val googleFont = GoogleFont(option.fontName)
        return FontFamily(
            Font(googleFont = googleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
            Font(googleFont = googleFont, fontProvider = fontProvider, weight = FontWeight.Bold)
        )
    }

    private fun getSavedAccent(): AccentColor {
        val name = sharedPrefs.getString("accent_key", AccentColor.GOLD.name)
        return try { AccentColor.valueOf(name!!) } catch (e: Exception) { AccentColor.GOLD }
    }

    private fun getSavedFont(): FontOption {
        val name = sharedPrefs.getString("font_key", FontOption.AMIRI.name)
        return try { FontOption.valueOf(name!!) } catch (e: Exception) { FontOption.AMIRI }
    }

    private fun getSavedBackground(): BackgroundOption {
        val name = sharedPrefs.getString("background_key", BackgroundOption.SILENT_COSMIC.name)
        return try { BackgroundOption.valueOf(name!!) } catch (e: Exception) { BackgroundOption.SILENT_COSMIC }
    }
}
