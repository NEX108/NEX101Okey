package de.isikeren.nex101

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TurDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun turEkle(tur: TurEntity): Long

    @Update
    suspend fun turGuncelle(tur: TurEntity)

    @Delete
    suspend fun turSil(tur: TurEntity)

    @Query("SELECT * FROM turlar WHERE oyunId = :oyunId ORDER BY turNo ASC")
    fun oyununTurlariniGetir(oyunId: Int): Flow<List<TurEntity>>

    @Query("SELECT * FROM turlar WHERE oyunId = :oyunId ORDER BY turNo ASC")
    suspend fun oyununTurlariniGetirListe(oyunId: Int): List<TurEntity>

    @Query("SELECT * FROM turlar WHERE id = :turId LIMIT 1")
    suspend fun turGetir(turId: Int): TurEntity?

    @Query("SELECT * FROM turlar WHERE oyunId = :oyunId AND turNo = :turNo LIMIT 1")
    suspend fun oyunVeTurNoIleTurGetir(oyunId: Int, turNo: Int): TurEntity?

    @Query("DELETE FROM turlar WHERE oyunId = :oyunId AND turNo = :turNo")
    suspend fun oyunVeTurNoIleTurSil(oyunId: Int, turNo: Int)
}