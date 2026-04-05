package de.isikeren.nex101

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cezalar")
data class CezaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val turId: Int,
    val cezaTipi: String, // islek_atti, tas_cekildi, acamadi, okey_atti, okey_elde_patladi, gerisi
    val cezaAlanOyuncuId: Int,
    val sebepOyuncuId: Int? = null,
    val tasDegeri: Int? = null,
    val puan: Int
)