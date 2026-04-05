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
}