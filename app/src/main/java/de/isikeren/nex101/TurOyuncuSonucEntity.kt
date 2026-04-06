
package de.isikeren.nex101

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tur_oyuncu_sonuclari",
    foreignKeys = [
        ForeignKey(
            entity = TurEntity::class,
            parentColumns = ["id"],
            childColumns = ["turId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OyuncuEntity::class,
            parentColumns = ["id"],
            childColumns = ["oyuncuId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["turId"]),
        Index(value = ["oyuncuId"]),
        Index(value = ["turId", "oyuncuId"], unique = true)
    ]
)
data class TurOyuncuSonucEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val turId: Int,
    val oyuncuId: Int,
    val pozisyon: Int, // 1..4
    val takimNo: Int? = null, // tek = null, ortak = 1 oder 2
    val girilenDeger: Int,
    val sonucPuani: Int,
    val multiplikator: Int,
    val cift: Boolean = false,
    val bitti: Boolean = false,
    val okeyle: Boolean = false,
    val eldenBitti: Boolean = false,
    val acamadi: Boolean = false
)

