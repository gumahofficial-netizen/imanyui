package com.example.data.repository

import android.content.Context
import com.example.data.api.QuranApiService
import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlin.math.*

class ImanyRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val tasbihDao = db.tasbihDao()
    private val favoritesDao = db.favoritesDao()
    private val cacheDao = db.cacheDao()
    private val api = QuranApiService.instance

    // --- RECTIERS & SURAHS FAILSAFE FALLBACKS ---
    val fallbackSurahs = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", 7, "Meccan"),
        Surah(2, "البقرة", "Al-Baqarah", 286, "Medinan"),
        Surah(3, "آل عمران", "Ali 'Imran", 200, "Medinan"),
        Surah(4, "النساء", "An-Nisa", 176, "Medinan"),
        Surah(5, "المائدة", "Al-Ma'idah", 120, "Medinan"),
        Surah(6, "الأنعام", "Al-An'am", 165, "Meccan"),
        Surah(7, "الأعراف", "Al-A'raf", 206, "Meccan"),
        Surah(8, "الأنفال", "Al-Anfal", 75, "Medinan"),
        Surah(9, "التوبة", "At-Tawbah", 129, "Medinan"),
        Surah(10, "يونس", "Yunus", 109, "Meccan"),
        Surah(18, "الكهف", "Al-Kahf", 110, "Meccan"),
        Surah(36, "يس", "Ya-Sin", 83, "Meccan"),
        Surah(55, "الرحمن", "Ar-Rahman", 78, "Meccan"),
        Surah(56, "الواقعة", "Al-Waqi'ah", 96, "Meccan"),
        Surah(67, "الملك", "Al-Mulk", 30, "Meccan"),
        Surah(112, "الاخلاص", "Al-Ikhlas", 4, "Meccan"),
        Surah(113, "الفلق", "Al-Falaq", 5, "Meccan"),
        Surah(114, "الناس", "An-Nas", 6, "Meccan")
    )

    val fallbackReciters = listOf(
        Reciter(92, "ياسر الدوسري", "Y", "Yasser-Al-Dosari"),
        Reciter(104, "مشاري العفاسي", "M", "Mishary-Alafasy"),
        Reciter(118, "ماهر المعيقلي", "M", "Maher-Al-Muaiqly"),
        Reciter(54, "عبد الباسط عبد الصمد", "A", "Abdul-Basit"),
        Reciter(12, "سعد الغامدي", "S", "Saad-Al-Ghamdi"),
        Reciter(143, "أحمد العجمي", "A", "Ahmed-Al-Ajmy"),
        Reciter(23, "فارس عباد", "F", "Fares-Abbad"),
        Reciter(82, "عبد الرحمن السديس", "A", "Abdul-Rahman-Al-Sudais")
    )

    // Hadiths List (حديث شريف يومي يتجدد تلقائيا)
    val offlineHadiths = listOf(
        "إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى. (رواه البخاري ومسلم)",
        "الطهور شطر الإيمان، والحمد لله تملأ الميزان. (رواه مسلم)",
        "اتق الله حيثما كنت، وأتبع السيئة الحسنة تمحها، وخالق الناس بخلق حسن. (رواه الترمذي)",
        "من سلك طريقًا يبتغي فيه علمًا سهّل الله له طريقًا إلى الجنة. (رواه مسلم)",
        "يسروا ولا تعسروا، وبشروا ولا تنفروا. (رواه البخاري)",
        "خيركم من تعلم القرآن وعلمه. (رواه البخاري)",
        "من كان يؤمن بالله واليوم الآخر فليقل خيرًا أو ليصمت. (رواه البخاري ومسلم)",
        "احفظ الله يحفظك، احفظ الله تجده تجاهك. (رواه الترمذي)",
        "لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه. (رواه البخاري ومسلم)",
        "تبسمك في وجه أخيك لك صدقة. (رواه الترمذي)"
    )

    // Random Daily Dhikr
    val randomDhikrList = listOf(
        "سبحان الله وبحمده، عدد خلقه ورضا نفسه وزنة عرشه ومداد كلماته.",
        "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير.",
        "اللهم بك أصبحنا وبك أمسينا وبك نحيا وبك نموت وإليك النشور.",
        "اللهم أنت ربي لا إله إلا أنت، خلقتني وأنا عبدك، وأنا على عهدك ووعدك ما استطعت.",
        "رضيت بالله رباً، وبالإسلام ديناً، وبمحمد صلى الله عليه وسلم نبياً ورسولاً.",
        "يا حي يا قيوم برحمتك أستغيث، أصلح لي شأني كله ولا تكلني إلى نفسي طرفة عين.",
        "لا حول ولا قوة إلا بالله العلي العظيم.",
        "اللهم صلّ وسلم وبارك على نبينا محمد وعلى آله وصحبه أجمعين."
    )

    // Premium Radio channels (Live broadcasts)
    val fallbackRadios = listOf(
        RadioChannel(1, "إذاعة القرآن الكريم - القاهرة", "https://stream.radiojar.com/8s5uqg7cb0vtv"),
        RadioChannel(2, "إذاعة القرآن الكريم - المملكة العربية السعودية", "https://subme.sh/quran-ksa"),
        RadioChannel(3, "إذاعة الشيخ عبد الباسط عبد الصمد", "https://qurango.net/radio/tarteel"),
        RadioChannel(4, "راديو التلاوات الخاشعة", "https://qurango.net/radio/salma"),
        RadioChannel(5, "إمارات إف إم - إذاعة القرآن الشارقة", "https://shj-quran.shjmedia.ae/stream.mp3"),
        RadioChannel(6, "راديو أذكار الصباح والمساء", "https://qurango.net/radio/athkar")
    )

    // --- API QUERIES WITH OFFLINE SUPPORT ---

    suspend fun getSurahs(): List<Surah> {
        return try {
            val dtoList = api.getSurahs()
            if (dtoList.isNotEmpty()) {
                dtoList.map { dto ->
                    Surah(
                        id = dto.id.toIntOrNull() ?: dto.number.toIntOrNull() ?: 1,
                        name = dto.name_ar,
                        englishName = dto.name_en,
                        ayat = dto.ayat_count.toIntOrNull() ?: 1,
                        type = dto.type
                    )
                }
            } else {
                fallbackSurahs
            }
        } catch (e: Exception) {
            fallbackSurahs
        }
    }

    suspend fun getAyahs(surahId: Int): List<Ayah> {
        return try {
            val dtoList = api.getAyahsBySurah(surahId)
            if (dtoList.isNotEmpty()) {
                dtoList.map { dto ->
                    Ayah(
                        number = dto.number_in_surah.toIntOrNull() ?: dto.number.toIntOrNull() ?: 1,
                        text = dto.text,
                        juz = dto.juz_id.toIntOrNull() ?: 1,
                        manzil = 1,
                        page = dto.page.toIntOrNull() ?: 1,
                        ruku = 1,
                        hizbQuarter = dto.hizb_id.toIntOrNull() ?: 1,
                        surahId = dto.surah_id.toIntOrNull() ?: surahId
                    )
                }
            } else {
                getOfflineAyahsFallback(surahId)
            }
        } catch (e: Exception) {
            getOfflineAyahsFallback(surahId)
        }
    }

    private fun getOfflineAyahsFallback(surahId: Int): List<Ayah> {
        val fakeList = mutableListOf<Ayah>()
        val customSurah = fallbackSurahs.find { it.id == surahId }
        val count = customSurah?.ayat ?: 10
        for (i in 1..count) {
            fakeList.add(
                Ayah(
                    number = i,
                    text = "الآية رقم $i تظهر هنا بالرسم العثماني قريباً عند الاتصال بالشبكة.",
                    juz = 1,
                    manzil = 1,
                    page = 1,
                    ruku = 1,
                    hizbQuarter = 1,
                    surahId = surahId
                )
            )
        }
        return fakeList
    }

    suspend fun getReciters(): List<Reciter> {
        return try {
            val res = api.getReciters()
            val list = res.reciters
            if (list != null && list.isNotEmpty()) {
                list.map { dto ->
                    Reciter(
                        id = dto.reciter_id.toIntOrNull() ?: 1,
                        name = dto.reciter_name,
                        letter = dto.reciter_short_name.firstOrNull()?.toString() ?: "",
                        server = dto.reciter_short_name
                    )
                }
            } else {
                fallbackReciters
            }
        } catch (e: Exception) {
            fallbackReciters
        }
    }

    suspend fun getReciterAudio(reciterId: Int): List<ReciterAudio> {
        return try {
            val response = api.getReciterAudio(reciterId)
            val list = response.audio_urls
            if (list != null && list.isNotEmpty()) {
                list.map { dto ->
                    val surahNo = dto.surah_id.toIntOrNull() ?: 1
                    ReciterAudio(
                        id = surahNo,
                        reciterId = reciterId,
                        surahId = surahNo,
                        url = dto.audio_url,
                        title = dto.surah_name_ar ?: "سورة"
                    )
                }
            } else {
                getOfflineReciterAudioFallback(reciterId)
            }
        } catch (e: Exception) {
            getOfflineReciterAudioFallback(reciterId)
        }
    }

    private fun getOfflineReciterAudioFallback(reciterId: Int): List<ReciterAudio> {
        val matchingReciter = fallbackReciters.find { it.id == reciterId }
        val slug = matchingReciter?.server ?: "Maher-Al-Muaiqly"
        return listOf(
            ReciterAudio(1, reciterId, 1, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=1", "سورة الفاتحة"),
            ReciterAudio(2, reciterId, 18, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=18", "سورة الكهف"),
            ReciterAudio(3, reciterId, 36, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=36", "سورة يس"),
            ReciterAudio(4, reciterId, 55, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=55", "سورة الرحمن"),
            ReciterAudio(5, reciterId, 56, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=56", "سورة الواقعة"),
            ReciterAudio(6, reciterId, 67, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=67", "سورة الملك"),
            ReciterAudio(7, reciterId, 112, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=112", "سورة الإخلاص"),
            ReciterAudio(8, reciterId, 113, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=113", "سورة الفلق"),
            ReciterAudio(9, reciterId, 114, "https://quran.yousefheiba.com/api/surahAudio?reciter=$slug&id=114", "سورة الناس")
        )
    }

    suspend fun getAzkar(): List<AzkarResponse> {
        return try {
            val dto = api.getAzkar()
            val morning = dto.morning_azkar ?: emptyList()
            val evening = dto.evening_azkar ?: emptyList()
            val sleep = dto.sleep_azkar ?: emptyList()
            
            if (morning.isNotEmpty() || evening.isNotEmpty() || sleep.isNotEmpty()) {
                listOf(
                    AzkarResponse("أذكار الصباح", morning.map { item ->
                        AzkarItem(item.id, "أذكار الصباح", item.count ?: 1, null, null, item.text)
                    }),
                    AzkarResponse("أذكار المساء", evening.map { item ->
                        AzkarItem(item.id, "أذكار المساء", item.count ?: 1, null, null, item.text)
                    }),
                    AzkarResponse("أذكار النوم", sleep.map { item ->
                        AzkarItem(item.id, "أذكار النوم", item.count ?: 1, null, null, item.text)
                    })
                )
            } else {
                getOfflineAzkar()
            }
        } catch (e: Exception) {
            getOfflineAzkar()
        }
    }

    suspend fun getDuas(): List<Dua> {
        return try {
            val dto = api.getDuas()
            val combined = mutableListOf<Dua>()
            
            dto.prophetic_duas?.forEach { item ->
                combined.add(Dua(item.id, "دعاء نبوي شريف", item.text, "أدعية مأثورة مروية في السنة النبوية المطهرة"))
            }
            dto.quranic_duas?.forEach { item ->
                combined.add(Dua(item.id, "دعاء قرآني مبارك", item.text, "أدعية جليلة ذكرت في آيات الذكر الحكيم"))
            }
            dto.daily_duas?.forEach { item ->
                combined.add(Dua(item.id, "دعاء يومي مستجاب", item.text, "أدعية مباركة لليوم والليلة للتقرب لله"))
            }

            if (combined.isNotEmpty()) {
                combined
            } else {
                getOfflineDuas()
            }
        } catch (e: Exception) {
            getOfflineDuas()
        }
    }

    suspend fun getLaylatAlQadr(): LaylatAlQadrResponse {
        return try {
            val dto = api.getLaylatAlQadr()
            val inner = dto.laylat_al_qadr
            if (inner != null) {
                val signList = mutableListOf<String>()
                inner.signs?.confirmed?.let { signList.addAll(it) }
                inner.signs?.unconfirmed?.let { signList.addAll(it) }
                
                LaylatAlQadrResponse(
                    title = inner.definition?.name ?: "ليلة القدر الشريفة",
                    merit = inner.virtue?.value ?: "ليلة القدر هي ليلة عظيمة مباركة، ذكرها الله جل وعلا في كتابه الكريم، وهي خير من ألف شهر.",
                    signs = if (signList.isNotEmpty()) signList else listOf(
                        "أن تكون ليلة معتدلة لا حارة ولا باردة.",
                        "أن تطلع الشمس في صبيحتها بيضاء لا شعاع لها كأنها طست.",
                        "سكون الرياح، وانشراح عظيم في صدر المؤمن وطمأنينة."
                    ),
                    deeds = inner.recommended_acts?.acts ?: listOf(
                        "قيام الليل وإقامة صلاة التراويح والتهجد.",
                        "الاجتهاد في الدعاء المأثور (اللهم إنك عفو تحب العفو فاعف عني).",
                        "قراءة القرآن العظيم بتدبر وتأمل.",
                        "الاستغفار، والصدقة والإنفاق وسد حاجة المساكين."
                    ),
                    duas = listOf(
                        "اللهم إنك عفو كريم تحب العفو فاعف عني.",
                        "اللهم اهدنا فيمن هديت، وعافنا فيمن عافيت، وتولنا فيمن توليت.",
                        "اللهم انك عفو تحب العفو فاعف عنا وعن والدينا وجميع المسلمين."
                    )
                )
            } else {
                getOfflineLaylatAlQadrFallback()
            }
        } catch (e: Exception) {
            getOfflineLaylatAlQadrFallback()
        }
    }

    private fun getOfflineLaylatAlQadrFallback(): LaylatAlQadrResponse {
        return LaylatAlQadrResponse(
            title = "ليلة القدر الشريفة",
            merit = "ليلة القدر هي ليلة عظيمة مباركة، ذكرها الله جل وعلا في كتابه الكريم، وهي خير من ألف شهر. تنزل فيها الملائكة بالرحمات والمغفرة والسلام حتى مطلع الفجر.",
            signs = listOf(
                "أن تكون ليلة معتدلة لا حارة ولا باردة.",
                "أن تطلع الشمس في صبيحتها بيضاء لا شعاع لها كأنها طست.",
                "سكون الرياح، وانشراح عظيم في صدر المؤمن وطمأنينة.",
                "قوة الإضاءة والنور في تلك الليلة الشعورية."
            ),
            deeds = listOf(
                "قيام الليل وإقامة صلاة التراويح والتهجد.",
                "الاجتهاد في الدعاء المأثور (اللهم إنك عفو تحب العفو فاعف عني).",
                "قراءة القرآن العظيم بتدبر وتأمل.",
                "الاستغفار، والصدقة والإنفاق وسد حاجة المساكين."
            ),
            duas = listOf(
                "اللهم إنك عفو كريم تحب العفو فاعف عني.",
                "اللهم اهدنا فيمن هديت، وعافنا فيمن عافيت، وتولنا فيمن توليت.",
                "اللهم يا مقلب القلوب ثبت قلوبنا على دينك وطاعتك.",
                "اللهم انك عفو تحب العفو فاعف عنا وعن والدينا وجميع المسلمين."
            )
        )
    }

    // --- OFFLINE DATA GENERATION ---
    private fun getOfflineAzkar(): List<AzkarResponse> {
        return listOf(
            AzkarResponse("أذكار الصباح", listOf(
                AzkarItem(1, "أذكار الصباح", 1, "فضله: حرز وحفظ للعبد في يومه", "رواه مسلم", "أصبحنا وأصبح الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له."),
                AzkarItem(2, "أذكار الصباح", 3, "من قالها حين يصبح ثلاثا كفته من كل شيء", "رواه الترمذي", "بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم."),
                AzkarItem(3, "أذكار الصباح", 3, "فضله العافية وحفظ النعم", "رواه النسائي", "اللهم عافني في بدني، اللهم عافني في سمعي، اللهم عافني في بصري، لا إله إلا أنت.")
            )),
            AzkarResponse("أذكار المساء", listOf(
                AzkarItem(4, "أذكار المساء", 1, "حماية كاملة طوال الليل", "رواه مسلم", "أمسينا وأمسى الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له."),
                AzkarItem(5, "أذكار المساء", 3, "حفظ وعافية مستمرة", "أبو داود", "رضيت بالله رباً، وبالإسلام ديناً، وبمحمد صلى الله عليه وسلم نبياً ورسولاً."),
                AzkarItem(6, "أذكار المساء", 1, "سيد الاستغفار والمغفرة", "رواه البخاري", "اللهم أنت ربي لا إله إلا أنت، خلقتني وأنا عبدك، وأنا على عهدك ووعدك ما استطعت.")
            )),
            AzkarResponse("أذكار النوم", listOf(
                AzkarItem(7, "أذكار النوم", 1, "يقال حين يأوي العبد إلى فراشه", "البخاري", "باسمك ربي وضعت جنبي، وبك أرفعه، فإن أمسكت نفسي فارحمها."),
                AzkarItem(8, "أذكار النوم", 1, "الأمان السكني وطلب الوفاة الطيبة", "مسلم", "اللهم قني عذابك يوم تبعث عبادك.")
            ))
        )
    }

    private fun getOfflineDuas(): List<Dua> {
        return listOf(
            Dua(1, "دعاء الهداية والصلاح", "اللهم اهدنا فيمن هديت، وعافنا فيمن عافيت، وتولنا فيمن توليت، وبارك لنا فيما أعطيت."),
            Dua(2, "دعاء الاستعاذة من الهم", "اللهم إني أعوذ بك من الهم والحزن، والعجز والكسل، والبخل والجبن، وضلع الدين وغلبة الرجال."),
            Dua(3, "دعاء الاستغفار والتوبة", "ربنا ظلمنا أنفسنا وإن لم تغفر لنا وترحمنا لنكونن من الخاسرين."),
            Dua(4, "دعاء تيسير الأمور والفتح", "اللهم لا سهل إلا ما جعلته سهلاً، وأنت تجعل الحزن إذا شئت سهلاً."),
            Dua(5, "دعاء العافية واللطف", "اللهم إني أسألك العفو والعافية في الدنيا والآخرة، اللهم استر عوراتي وآمن روعاتي.")
        )
    }

    // --- FAVORITES & TASBIH ROOM HANDLERS ---
    fun getAllFavoriteDuas(): Flow<List<FavoriteDua>> = favoritesDao.getAllFavoriteDuas()
    suspend fun addFavoriteDua(dua: FavoriteDua) = favoritesDao.addFavoriteDua(dua)
    suspend fun removeFavoriteDua(id: Int) = favoritesDao.removeFavoriteDua(id)
    fun isDuaFavorite(id: Int): Flow<Boolean> = favoritesDao.isDuaFavorite(id)

    fun getAllFavoriteSurahs(): Flow<List<FavoriteSurah>> = favoritesDao.getAllFavoriteSurahs()
    suspend fun addFavoriteSurah(surah: FavoriteSurah) = favoritesDao.addFavoriteSurah(surah)
    suspend fun removeFavoriteSurah(id: Int) = favoritesDao.removeFavoriteSurah(id)
    fun isSurahFavorite(id: Int): Flow<Boolean> = favoritesDao.isSurahFavorite(id)

    fun getAllTasbihs(): Flow<List<Tasbih>> = tasbihDao.getAllTasbihs()
    suspend fun insertTasbih(tasbih: Tasbih) = tasbihDao.insertTasbih(tasbih)
    suspend fun updateTasbihCount(phrase: String, count: Int) = tasbihDao.updateCount(phrase, count)
    suspend fun deleteTasbih(phrase: String) = tasbihDao.deleteTasbih(phrase)

    // --- 100% OFFLINE PRAYER TIMES MATHEMATICAL CALCULATIONS ---
    data class PrayerTime(val name: String, val time: String)

    fun calculatePrayerTimes(latitude: Double, longitude: Double, timezoneOffset: Double = 3.0): List<PrayerTime> {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Basic Solar Declination calculations for prayer times estimation
        val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (284 + dayOfYear)))
        val equationOfTime = 9.87 * sin(Math.toRadians(4.0 * (dayOfYear - 81))) - 7.53 * cos(Math.toRadians(2.0 * (dayOfYear - 81))) - 1.5 * sin(Math.toRadians((dayOfYear - 81).toDouble()))

        // Solar Transit (Dhuhr)
        val dhuhrLocal = 12.0 - (longitude / 15.0) + timezoneOffset - (equationOfTime / 60.0)

        // Helper hour angles
        fun localTimeToHourString(decimalHours: Double): String {
            var normHours = decimalHours
            while (normHours < 0) normHours += 24.0
            while (normHours >= 24) normHours -= 24.0
            val hoursPart = normHours.toInt()
            val minutesPart = ((normHours - hoursPart) * 60).toInt()
            return String.format("%02d:%02d", hoursPart, minutesPart)
        }

        // Standard ISNA Angle values: Fajr: -15, Isha: -15 (MWL is Fajr -18, Isha -17)
        val fajrAngle = -18.0
        val ishaAngle = -17.0

        // Zenith Hour angles
        fun mathTimeForAngle(angle: Double): Double {
            val angleRad = Math.toRadians(angle)
            val declRad = Math.toRadians(declination)
            val latRad = Math.toRadians(latitude)
            val cosH = (sin(angleRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
            if (cosH > 1.0 || cosH < -1.0) return Double.NaN
            val hHours = Math.toDegrees(acos(cosH)) / 15.0
            return hHours
        }

        val hFajr = mathTimeForAngle(fajrAngle)
        val hMaghrib = mathTimeForAngle(-0.833)
        val hIsha = mathTimeForAngle(ishaAngle)

        // Asr calculation (Shafi style: shadow ratio = 1)
        val declRad = Math.toRadians(declination)
        val latRad = Math.toRadians(latitude)
        val term = tan(abs(latRad - declRad)) + 1.0
        val sinAsrAngle = sin(atan(1.0 / term))
        val cosHAsr = (sinAsrAngle - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val hAsr = if (cosHAsr in -1.0..1.0) Math.toDegrees(acos(cosHAsr)) / 15.0 else Double.NaN

        val fajrTime = if (hFajr.isNaN()) "04:31" else localTimeToHourString(dhuhrLocal - hFajr)
        val dhuhrTime = localTimeToHourString(dhuhrLocal)
        val asrTime = if (hAsr.isNaN()) "15:45" else localTimeToHourString(dhuhrLocal + hAsr)
        val maghribTime = if (hMaghrib.isNaN()) "18:42" else localTimeToHourString(dhuhrLocal + hMaghrib)
        val ishaTime = if (hIsha.isNaN()) "20:10" else localTimeToHourString(dhuhrLocal + hIsha)

        return listOf(
            PrayerTime("الفجر", fajrTime),
            PrayerTime("الظهر", dhuhrTime),
            PrayerTime("العصر", asrTime),
            PrayerTime("المغرب", maghribTime),
            PrayerTime("العشاء", ishaTime)
        )
    }
}
