package de.isikeren.nex101

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OyunKatilimciDao {

    @Insert
    suspend fun katilimcilariEkle(katilimcilar: List<OyunKatilimciEntity>)

    @Query("SELECT * FROM oyun_katilimcilari WHERE oyunId = :oyunId ORDER BY pozisyon ASC")
    suspend fun oyununKatilimcilariniGetir(oyunId: Int): List<OyunKatilimciEntity>

    @Query("DELETE FROM oyun_katilimcilari WHERE oyunId = :oyunId")
    suspend fun oyununKatilimcilariniSil(oyunId: Int)

    @Query(
        """
        SELECT COUNT(*)
        FROM oyun_katilimcilari ok
        INNER JOIN oyunlar o ON o.id = ok.oyunId
        WHERE ok.oyuncuId = :oyuncuId
          AND o.durum = 'bitti'
        """
    )
    suspend fun oyuncununOyunSayisiniGetir(oyuncuId: Int): Int
}