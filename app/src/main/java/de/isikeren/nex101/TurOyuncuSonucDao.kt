
package de.isikeren.nex101

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TurOyuncuSonucDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun turOyuncuSonucuEkle(sonuc: TurOyuncuSonucEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun turOyuncuSonuclariniEkle(sonuclar: List<TurOyuncuSonucEntity>)

    @Update
    suspend fun turOyuncuSonucuGuncelle(sonuc: TurOyuncuSonucEntity)

    @Delete
    suspend fun turOyuncuSonucuSil(sonuc: TurOyuncuSonucEntity)

    @Query("SELECT * FROM tur_oyuncu_sonuclari WHERE turId = :turId ORDER BY pozisyon ASC")
    fun turunOyuncuSonuclariniGetir(turId: Int): Flow<List<TurOyuncuSonucEntity>>

    @Query("SELECT * FROM tur_oyuncu_sonuclari WHERE turId = :turId ORDER BY pozisyon ASC")
    suspend fun turunOyuncuSonuclariniGetirListe(turId: Int): List<TurOyuncuSonucEntity>

    @Query(
        """
        SELECT tos.*
        FROM tur_oyuncu_sonuclari tos
        INNER JOIN turlar t ON t.id = tos.turId
        INNER JOIN oyunlar o ON o.id = t.oyunId
        WHERE tos.oyuncuId = :oyuncuId
          AND o.durum = 'bitti'
        ORDER BY tos.id ASC
        """
    )
    suspend fun oyuncununTumTurSonuclariniGetir(oyuncuId: Int): List<TurOyuncuSonucEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM tur_oyuncu_sonuclari tos
        INNER JOIN turlar t ON t.id = tos.turId
        INNER JOIN oyunlar o ON o.id = t.oyunId
        WHERE tos.oyuncuId = :oyuncuId
          AND o.durum = 'bitti'
        """
    )
    suspend fun oyuncununOynadigiTurSayisiniGetir(oyuncuId: Int): Int

    @Query("SELECT COUNT(*) FROM tur_oyuncu_sonuclari WHERE oyuncuId = :oyuncuId AND acamadi = 1")
    suspend fun oyuncununAcamadiSayisiniGetir(oyuncuId: Int): Int

    @Query("SELECT COUNT(*) FROM tur_oyuncu_sonuclari WHERE oyuncuId = :oyuncuId AND cift = 1")
    suspend fun oyuncununCiftSayisiniGetir(oyuncuId: Int): Int

    @Query("SELECT COUNT(*) FROM tur_oyuncu_sonuclari WHERE oyuncuId = :oyuncuId AND bitti = 1")
    suspend fun oyuncununBittiSayisiniGetir(oyuncuId: Int): Int

    @Query("SELECT COUNT(*) FROM tur_oyuncu_sonuclari WHERE oyuncuId = :oyuncuId AND okeyle = 1")
    suspend fun oyuncununOkeyleBitirmeSayisiniGetir(oyuncuId: Int): Int

    @Query("SELECT COUNT(*) FROM tur_oyuncu_sonuclari WHERE oyuncuId = :oyuncuId AND eldenBitti = 1")
    suspend fun oyuncununEldenBitirmeSayisiniGetir(oyuncuId: Int): Int
}

