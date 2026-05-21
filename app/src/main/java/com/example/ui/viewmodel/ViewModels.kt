package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.text.format.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ImanyRepository
import com.example.data.service.AudioPlaybackService
import com.example.data.service.AudioPlayerState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 1. PRAYER & HOME VIEWMODEL
class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ImanyRepository(application)
    
    private val _prayerTimes = MutableStateFlow<List<ImanyRepository.PrayerTime>>(emptyList())
    val prayerTimes: StateFlow<List<ImanyRepository.PrayerTime>> = _prayerTimes.asStateFlow()

    private val _currentCity = MutableStateFlow("مكة المكرمة")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    private val _currentCountry = MutableStateFlow("المملكة العربية السعودية")
    val currentCountry: StateFlow<String> = _currentCountry.asStateFlow()

    private val _hijriDate = MutableStateFlow("")
    val hijriDate: StateFlow<String> = _hijriDate.asStateFlow()

    private val _gregorianDate = MutableStateFlow("")
    val gregorianDate: StateFlow<String> = _gregorianDate.asStateFlow()

    private val _dailyHadith = MutableStateFlow("")
    val dailyHadith: StateFlow<String> = _dailyHadith.asStateFlow()

    private val _dailyDhikr = MutableStateFlow("")
    val dailyDhikr: StateFlow<String> = _dailyDhikr.asStateFlow()

    private var currentLat = 21.4225  // Mecca latitude
    private var currentLng = 39.8262  // Mecca longitude

    init {
        refreshHomeData()
    }

    fun refreshHomeData() {
        // Formulate dates
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
        _gregorianDate.value = sdf.format(Date())

        // Standard astronomical estimation of hijri date based on current Gregorian date offsets
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        // Basic approximation of Hijri date (Islamic year calculation)
        val hijriYear = ((year - 622) * 1.0307).toInt()
        val hijriMonths = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
        // Offset mapping to approximate RAMADAN context in 2026/May
        val approxHijriMonthIndex = 5 // approximate (Jamada al-Akhira/Rajab)
        val approxDayInMonth = (day + 3) % 29 + 1
        _hijriDate.value = "$approxDayInMonth ${hijriMonths[approxHijriMonthIndex]} $hijriYear هـ"

        // Pick Daily hadith & random dhikr
        _dailyHadith.value = repository.offlineHadiths.random()
        _dailyDhikr.value = repository.randomDhikrList.random()

        updateLocationCoordinates(currentLat, currentLng, "مكة المكرمة", "المملكة العربية السعودية")
    }

    fun updateLocationCoordinates(lat: Double, lng: Double, city: String, country: String) {
        currentLat = lat
        currentLng = lng
        _currentCity.value = city
        _currentCountry.value = country
        
        // Dynamic offline astronomical calculations
        _prayerTimes.value = repository.calculatePrayerTimes(lat, lng)
    }
}

// 2. QURAN TEXT & IMAGE VIEWMODEL
class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ImanyRepository(application)

    private val _surahs = MutableStateFlow<List<Surah>>(emptyList())
    val surahs: StateFlow<List<Surah>> = _surahs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeAyahs = MutableStateFlow<List<Ayah>>(emptyList())
    val activeAyahs: StateFlow<List<Ayah>> = _activeAyahs.asStateFlow()

    private val _activeSurah = MutableStateFlow<Surah?>(null)
    val activeSurah: StateFlow<Surah?> = _activeSurah.asStateFlow()

    init {
        loadSurahs()
    }

    fun loadSurahs() = viewModelScope.launch {
        _isLoading.value = true
        _surahs.value = repository.getSurahs()
        _isLoading.value = false
    }

    fun loadSurahText(surah: Surah) = viewModelScope.launch {
        _isLoading.value = true
        _activeSurah.value = surah
        _activeAyahs.value = repository.getAyahs(surah.id)
        _isLoading.value = false
    }
}

// 3. AUDIO PLAYER VIEWMODEL
class AudioViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val repository = ImanyRepository(context)

    private val _reciters = MutableStateFlow<List<Reciter>>(emptyList())
    val reciters: StateFlow<List<Reciter>> = _reciters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _reciterAudios = MutableStateFlow<List<ReciterAudio>>(emptyList())
    val reciterAudios: StateFlow<List<ReciterAudio>> = _reciterAudios.asStateFlow()

    private val _selectedReciter = MutableStateFlow<Reciter?>(null)
    val selectedReciter: StateFlow<Reciter?> = _selectedReciter.asStateFlow()

    val playState = AudioPlayerState

    // Track list coordinates for Next/Previous triggers
    private var currentPlaylist: List<ReciterAudio> = emptyList()
    private var currentIndex = -1

    init {
        loadReciters()
        setupPlaylistListeners()
    }

    private fun loadReciters() = viewModelScope.launch {
        _isLoading.value = true
        _reciters.value = repository.getReciters()
        _isLoading.value = false
    }

    fun selectReciter(reciter: Reciter) = viewModelScope.launch {
        _isLoading.value = true
        _selectedReciter.value = reciter
        val audios = repository.getReciterAudio(reciter.id)
        _reciterAudios.value = audios
        currentPlaylist = audios
        _isLoading.value = false
    }

    fun playTrack(reciterAudio: ReciterAudio, isRadio: Boolean = false) {
        val recName = _selectedReciter.value?.name ?: "راديو وتلاوات"
        val fallbackSurahName = if (isRadio) reciterAudio.title ?: "بث راديو مباشر" else "سورة رقم ${reciterAudio.surahId}"
        val trackTitle = reciterAudio.title ?: fallbackSurahName

        currentIndex = currentPlaylist.indexOfFirst { it.id == reciterAudio.id }

        AudioPlaybackService.startService(
            context = context,
            action = AudioPlaybackService.ACTION_PLAY_NEW,
            extras = mapOf(
                AudioPlaybackService.EXTRA_URL to reciterAudio.url,
                AudioPlaybackService.EXTRA_TITLE to trackTitle,
                AudioPlaybackService.EXTRA_SUBTITLE to recName,
                AudioPlaybackService.EXTRA_IS_RADIO to isRadio
            )
        )
    }

    private fun setupPlaylistListeners() {
        AudioPlayerState.onNext = {
            if (currentIndex != -1 && currentPlaylist.isNotEmpty()) {
                val nextIdx = (currentIndex + 1) % currentPlaylist.size
                playTrack(currentPlaylist[nextIdx])
            }
        }
        AudioPlayerState.onPrevious = {
            if (currentIndex != -1 && currentPlaylist.isNotEmpty()) {
                val prevIdx = if (currentIndex - 1 < 0) currentPlaylist.size - 1 else currentIndex - 1
                playTrack(currentPlaylist[prevIdx])
            }
        }
    }

    fun selectRadio(channel: RadioChannel) {
        AudioPlaybackService.startService(
            context = context,
            action = AudioPlaybackService.ACTION_PLAY_NEW,
            extras = mapOf(
                AudioPlaybackService.EXTRA_URL to channel.url,
                AudioPlaybackService.EXTRA_TITLE to channel.name,
                AudioPlaybackService.EXTRA_SUBTITLE to "بث إسلامي مباشر",
                AudioPlaybackService.EXTRA_IS_RADIO to true
            )
        )
    }

    fun getRadios(): List<RadioChannel> {
        return repository.fallbackRadios
    }
}

// 4. DHIKR, DUA, AND LAYLAT AL-QADR VIEWMODEL
class DhikrDuaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ImanyRepository(application)

    private val _azkarGroups = MutableStateFlow<List<AzkarResponse>>(emptyList())
    val azkarGroups: StateFlow<List<AzkarResponse>> = _azkarGroups.asStateFlow()

    private val _duasList = MutableStateFlow<List<Dua>>(emptyList())
    val duasList: StateFlow<List<Dua>> = _duasList.asStateFlow()

    private val _laylatAlQadr = MutableStateFlow<LaylatAlQadrResponse?>(null)
    val laylatAlQadr: StateFlow<LaylatAlQadrResponse?> = _laylatAlQadr.asStateFlow()

    private val _favoriteDuas = MutableStateFlow<List<FavoriteDua>>(emptyList())
    val favoriteDuas: StateFlow<List<FavoriteDua>> = _favoriteDuas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
        observeFavorites()
    }

    private fun loadData() = viewModelScope.launch {
        _isLoading.value = true
        _azkarGroups.value = repository.getAzkar()
        _duasList.value = repository.getDuas()
        _laylatAlQadr.value = repository.getLaylatAlQadr()
        _isLoading.value = false
    }

    private fun observeFavorites() = viewModelScope.launch {
        repository.getAllFavoriteDuas().collect {
            _favoriteDuas.value = it
        }
    }

    fun toggleFavoriteDua(dua: Dua) = viewModelScope.launch {
        val isFav = _favoriteDuas.value.any { it.id == dua.id }
        if (isFav) {
            repository.removeFavoriteDua(dua.id)
        } else {
            repository.addFavoriteDua(FavoriteDua(dua.id, dua.title, dua.text))
        }
    }
}

// 5. TASBIH (ELECTRONIC COUNTER) VIEWMODEL
class TasbihViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val repository = ImanyRepository(context)

    private val _tasbihs = MutableStateFlow<List<Tasbih>>(emptyList())
    val tasbihs: StateFlow<List<Tasbih>> = _tasbihs.asStateFlow()

    private val _selectedDhikr = MutableStateFlow("سبحان الله")
    val selectedDhikr: StateFlow<String> = _selectedDhikr.asStateFlow()

    private val _counterValue = MutableStateFlow(0)
    val counterValue: StateFlow<Int> = _counterValue.asStateFlow()

    private val _targetValue = MutableStateFlow(33)
    val targetValue: StateFlow<Int> = _targetValue.asStateFlow()

    private val defaultDhikrOptions = listOf("سبحان الله", "الحمد لله", "لا إله إلا الله", "الله أكبر", "أستغفر الله", "اللهم صلّ على محمد")

    init {
        loadTasbihRecords()
    }

    private fun loadTasbihRecords() = viewModelScope.launch {
        // Collect Tasbih from Room database
        repository.getAllTasbihs().collect { list ->
            _tasbihs.value = list
            val active = list.find { it.phrase == _selectedDhikr.value }
            if (active != null) {
                _counterValue.value = active.count
                _targetValue.value = active.target
            } else {
                // Initialize default in database
                defaultDhikrOptions.forEach { p ->
                    if (list.none { it.phrase == p }) {
                        repository.insertTasbih(Tasbih(p, 0, 33))
                    }
                }
            }
        }
    }

    fun selectDhikr(phrase: String) = viewModelScope.launch {
        _selectedDhikr.value = phrase
        val match = _tasbihs.value.find { it.phrase == phrase }
        if (match != null) {
            _counterValue.value = match.count
            _targetValue.value = match.target
        } else {
            _counterValue.value = 0
            _targetValue.value = 33
            repository.insertTasbih(Tasbih(phrase, 0, 33))
        }
    }

    fun incrementCounter() = viewModelScope.launch {
        _counterValue.value += 1
        repository.updateTasbihCount(_selectedDhikr.value, _counterValue.value)
        triggerVibration()
    }

    fun clearCounter() = viewModelScope.launch {
        _counterValue.value = 0
        repository.updateTasbihCount(_selectedDhikr.value, 0)
        triggerVibration(long = true)
    }

    fun changeTarget(target: Int) = viewModelScope.launch {
        _targetValue.value = target
        repository.insertTasbih(Tasbih(_selectedDhikr.value, _counterValue.value, target))
    }

    private fun triggerVibration(long: Boolean = false) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (long) {
                    vibrator.vibrate(120)
                } else {
                    vibrator.vibrate(45)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
