package de.isikeren.nex101

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OyunDao {

    @Insert
    suspend fun oyunEkle(oyun: OyunEntity): Long

    @Query("SELECT * FROM oyunlar WHERE id = :oyunId LIMIT 1")
    suspend fun oyunGetir(oyunId: Int): OyunEntity?

    @Query("SELECT * FROM oyunlar ORDER BY baslangicZamani DESC")
    suspend fun tumOyunlariGetir(): List<OyunEntity>
}