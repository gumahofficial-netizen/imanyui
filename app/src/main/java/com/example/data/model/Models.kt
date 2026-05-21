package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

// 1. Surah Model
@JsonClass(generateAdapter = true)
data class Surah(
    val id: Int,
    val name: String,
    val englishName: String,
    val ayat: Int,
    val type: String // "Meccan" or "Medinan" / مكية أو مدنية
)

// 2. Ayah Model
@JsonClass(generateAdapter = true)
data class Ayah(
    val number: Int,
    val text: String,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    val surahId: Int? = null
)

@JsonClass(generateAdapter = true)
data class SurahDetailsResponse(
    val surahId: Int,
    val name: String,
    val ayahs: List<Ayah>
)

// 3. Reciter Model
@JsonClass(generateAdapter = true)
data class Reciter(
    val id: Int,
    val name: String,
    val letter: String? = null,
    val server: String? = null
)

// 4. Reciter Audio Clip Model
@JsonClass(generateAdapter = true)
data class ReciterAudio(
    val id: Int,
    val reciterId: Int,
    val surahId: Int,
    val url: String,
    val title: String? = null
)

// 5. Azkar Item
@JsonClass(generateAdapter = true)
data class AzkarItem(
    val id: Int? = null,
    val category: String, // "صباح", "مساء", "نوم", etc.
    val count: Int = 1,
    val description: String? = null,
    val reference: String? = null,
    val zekr: String
)

// List wrap
@JsonClass(generateAdapter = true)
data class AzkarResponse(
    val category: String,
    val items: List<AzkarItem>
)

// 6. Dua Item
@JsonClass(generateAdapter = true)
data class Dua(
    val id: Int,
    val title: String,
    val text: String,
    val description: String? = null
)

// 7. Laylat Al Qadr Model
@JsonClass(generateAdapter = true)
data class LaylatAlQadrResponse(
    val title: String,
    val merit: String,
    val signs: List<String>,
    val deeds: List<String>,
    val duas: List<String>
)

// 8. Radio Channel Model
@JsonClass(generateAdapter = true)
data class RadioChannel(
    val id: Int,
    val name: String,
    val url: String,
    val language: String? = null
)

// --- ROOM DB ENTITIES ---

@Entity(tableName = "tasbih")
data class Tasbih(
    @PrimaryKey val phrase: String,
    val count: Int,
    val target: Int = 33
)

@Entity(tableName = "favorite_duas")
data class FavoriteDua(
    @PrimaryKey val id: Int,
    val title: String,
    val text: String
)

@Entity(tableName = "favorite_surahs")
data class FavoriteSurah(
    @PrimaryKey val id: Int,
    val name: String,
    val englishName: String,
    val ayat: Int,
    val type: String
)

@Entity(tableName = "cached_surahs")
data class CachedSurah(
    @PrimaryKey val id: Int,
    val name: String,
    val englishName: String,
    val ayat: Int,
    val type: String,
    val content: String // JSON stringified list of ayahs for offline support
)

// --- API DATA TRANSFER OBJECTS (DTOs) ---

@JsonClass(generateAdapter = true)
data class SurahDto(
    val id: String,
    val number: String,
    val name_ar: String,
    val name_en: String,
    val type: String,
    val ayat_count: String
)

@JsonClass(generateAdapter = true)
data class AyahDto(
    val id: String,
    val number: String,
    val text: String,
    val number_in_surah: String,
    val page: String,
    val surah_id: String,
    val hizb_id: String,
    val juz_id: String,
    val sajda: String
)

@JsonClass(generateAdapter = true)
data class RecitersDtoResponse(
    val reciters: List<ReciterDto>? = null
)

@JsonClass(generateAdapter = true)
data class ReciterDto(
    val reciter_id: String,
    val reciter_name: String,
    val reciter_short_name: String
)

@JsonClass(generateAdapter = true)
data class ReciterAudioDtoResponse(
    val reciter_id: String? = null,
    val reciter_name: String? = null,
    val audio_urls: List<AudioUrlDto>? = null
)

@JsonClass(generateAdapter = true)
data class AudioUrlDto(
    val surah_id: String,
    val surah_name_ar: String? = null,
    val audio_url: String
)

@JsonClass(generateAdapter = true)
data class AzkarDtoResponse(
    val morning_azkar: List<AzkarItemDto>? = null,
    val evening_azkar: List<AzkarItemDto>? = null,
    val sleep_azkar: List<AzkarItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class AzkarItemDto(
    val id: Int? = null,
    val text: String,
    val count: Int? = null
)

@JsonClass(generateAdapter = true)
data class DuasDtoResponse(
    val prophetic_duas: List<DuaDto>? = null,
    val quranic_duas: List<DuaDto>? = null,
    val daily_duas: List<DuaDto>? = null
)

@JsonClass(generateAdapter = true)
data class DuaDto(
    val id: Int,
    val text: String,
    val count: Int? = null
)

@JsonClass(generateAdapter = true)
data class LaylatAlQadrDtoResponse(
    val laylat_al_qadr: LaylatAlQadrInnerDto? = null
)

@JsonClass(generateAdapter = true)
data class LaylatAlQadrInnerDto(
    val definition: LaylatValDto? = null,
    val virtue: LaylatValDto? = null,
    val signs: LaylatSignsDto? = null,
    val recommended_acts: LaylatActsDto? = null
)

@JsonClass(generateAdapter = true)
data class LaylatValDto(
    val name: String? = null,
    val value: String? = null
)

@JsonClass(generateAdapter = true)
data class LaylatSignsDto(
    val name: String? = null,
    val confirmed: List<String>? = null,
    val unconfirmed: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class LaylatActsDto(
    val name: String? = null,
    val acts: List<String>? = null
)

