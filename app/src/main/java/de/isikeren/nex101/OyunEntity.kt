package de.isikeren.nex101

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oyunlar")
data class OyunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mod: String, // "tek" oder "ortak"
    val baslangicZamani: Long,
    val bitisZamani: Long? = null,
    val durum: String // "hazirlaniyor", "devam_ediyor", "bitti"
)