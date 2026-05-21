package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CachedSurah
import com.example.data.model.FavoriteDua
import com.example.data.model.FavoriteSurah
import com.example.data.model.Tasbih
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih")
    fun getAllTasbihs(): Flow<List<Tasbih>>

    @Query("SELECT * FROM tasbih WHERE phrase = :phrase LIMIT 1")
    fun getTasbih(phrase: String): Flow<Tasbih?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbih(tasbih: Tasbih)

    @Query("UPDATE tasbih SET count = :count WHERE phrase = :phrase")
    suspend fun updateCount(phrase: String, count: Int)

    @Query("DELETE FROM tasbih WHERE phrase = :phrase")
    suspend fun deleteTasbih(phrase: String)
}

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorite_duas")
    fun getAllFavoriteDuas(): Flow<List<FavoriteDua>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteDua(dua: FavoriteDua)

    @Query("DELETE FROM favorite_duas WHERE id = :id")
    suspend fun removeFavoriteDua(id: Int)

    @Query("SELECT COUNT(*) > 0 FROM favorite_duas WHERE id = :id")
    fun isDuaFavorite(id: Int): Flow<Boolean>

    @Query("SELECT * FROM favorite_surahs")
    fun getAllFavoriteSurahs(): Flow<List<FavoriteSurah>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteSurah(surah: FavoriteSurah)

    @Query("DELETE FROM favorite_surahs WHERE id = :id")
    suspend fun removeFavoriteSurah(id: Int)

    @Query("SELECT COUNT(*) > 0 FROM favorite_surahs WHERE id = :id")
    fun isSurahFavorite(id: Int): Flow<Boolean>
}

@Dao
interface CacheDao {
    @Query("SELECT * FROM cached_surahs WHERE id = :id LIMIT 1")
    fun getCachedSurah(id: Int): Flow<CachedSurah?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheSurah(cachedSurah: CachedSurah)
}

@Database(
    entities = [Tasbih::class, FavoriteDua::class, FavoriteSurah::class, CachedSurah::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tasbihDao(): TasbihDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "imany_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
