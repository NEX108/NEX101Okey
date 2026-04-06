package de.isikeren.nex101

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OyunDao {

    @Insert
    suspend fun oyunEkle(oyun: OyunEntity): Long

    @Update
    suspend fun oyunGuncelle(oyun: OyunEntity)

    @Query("UPDATE oyunlar SET durum = :durum, bitisZamani = :bitisZamani WHERE id = :oyunId")
    suspend fun oyunDurumunuGuncelle(oyunId: Int, durum: String, bitisZamani: Long?)

    @Query("SELECT * FROM oyunlar WHERE id = :oyunId LIMIT 1")
    suspend fun oyunGetir(oyunId: Int): OyunEntity?

    @Query("SELECT * FROM oyunlar ORDER BY baslangicZamani DESC")
    suspend fun tumOyunlariGetir(): List<OyunEntity>

    @Query("SELECT * FROM oyunlar WHERE durum = 'devam_ediyor' ORDER BY baslangicZamani DESC LIMIT 1")
    suspend fun aktifOyunuGetir(): OyunEntity?

    @Query("SELECT * FROM oyunlar WHERE durum = 'devam_ediyor' ORDER BY baslangicZamani DESC LIMIT 1")
    fun aktifOyunuGetirFlow(): Flow<OyunEntity?>

    @Query("SELECT * FROM oyunlar WHERE durum = 'bitti' ORDER BY bitisZamani DESC, baslangicZamani DESC")
    suspend fun bitenOyunlariGetir(): List<OyunEntity>

    @Query("SELECT * FROM oyunlar WHERE durum = 'bitti' ORDER BY bitisZamani DESC, baslangicZamani DESC")
    fun bitenOyunlariGetirFlow(): Flow<List<OyunEntity>>

    @Query("DELETE FROM oyunlar WHERE id = :oyunId")
    suspend fun oyunSil(oyunId: Int)
}