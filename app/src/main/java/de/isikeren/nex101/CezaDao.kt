
package de.isikeren.nex101

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CezaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cezaEkle(ceza: CezaEntity): Long

    @Update
    suspend fun cezaGuncelle(ceza: CezaEntity)

    @Delete
    suspend fun cezaSil(ceza: CezaEntity)

    @Query("SELECT * FROM cezalar WHERE turId = :turId ORDER BY siraNo ASC, id ASC")
    fun turunCezalariniGetir(turId: Int): Flow<List<CezaEntity>>

    @Query("SELECT * FROM cezalar WHERE turId = :turId ORDER BY siraNo ASC, id ASC")
    suspend fun turunCezalariniGetirListe(turId: Int): List<CezaEntity>

    @Query("SELECT * FROM cezalar WHERE id = :cezaId LIMIT 1")
    suspend fun cezaGetir(cezaId: Int): CezaEntity?

    @Query("SELECT * FROM cezalar WHERE kirmiziOyuncuId = :oyuncuId")
    suspend fun oyuncununVerdigiCezalariGetir(oyuncuId: Int): List<CezaEntity>

    @Query("SELECT * FROM cezalar WHERE yesilOyuncuId = :oyuncuId")
    suspend fun oyuncununSebepOlduguCezalariGetir(oyuncuId: Int): List<CezaEntity>

    @Query("SELECT COUNT(*) FROM cezalar WHERE kirmiziOyuncuId = :oyuncuId AND cezaTipi = :cezaTipi")
    suspend fun cezaSayisiniGetir(oyuncuId: Int, cezaTipi: String): Int

    @Query("SELECT COALESCE(SUM(puan), 0) FROM cezalar WHERE kirmiziOyuncuId = :oyuncuId AND cezaTipi = :cezaTipi")
    suspend fun cezaToplamPuaniniGetir(oyuncuId: Int, cezaTipi: String): Int
}

