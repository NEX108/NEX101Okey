package de.isikeren.nex101

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OyuncuDao {

    @Query("SELECT * FROM oyuncular ORDER BY id ASC")
    fun tumOyunculariGetir(): Flow<List<OyuncuEntity>>

    @Insert
    suspend fun oyuncuEkle(oyuncu: OyuncuEntity)

    @Update
    suspend fun oyuncuGuncelle(oyuncu: OyuncuEntity)

    @Delete
    suspend fun oyuncuSil(oyuncu: OyuncuEntity)
}