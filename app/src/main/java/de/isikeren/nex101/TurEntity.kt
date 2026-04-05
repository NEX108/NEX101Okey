package de.isikeren.nex101

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turlar")
data class TurEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oyunId: Int,
    val turNo: Int,
    val durum: String // "baslamadi", "devam_ediyor", "bitti"
)