package de.isikeren.nex101

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tur_sonuclari")
data class TurSonucEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val turId: Int,
    val oyuncuId: Int,
    val elPuani: Int? = null,
    val bitti: Boolean = false,
    val okeyle: Boolean = false,
    val cift: Boolean = false,
    val eldenBitti: Boolean = false
)