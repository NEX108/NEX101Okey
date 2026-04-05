package de.isikeren.nex101

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oyun_katilimcilari")
data class OyunKatilimciEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oyunId: Int,
    val oyuncuId: Int,
    val pozisyon: Int, // 1..4
    val takimNo: Int? = null, // tek = null, ortak = 1 oder 2
    val takimAdi: String? = null
)