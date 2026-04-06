package de.isikeren.nex101

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "turlar",
    foreignKeys = [
        ForeignKey(
            entity = OyunEntity::class,
            parentColumns = ["id"],
            childColumns = ["oyunId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["oyunId"]),
        Index(value = ["oyunId", "turNo"], unique = true)
    ]
)
data class TurEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oyunId: Int,
    val turNo: Int,
    val durum: String, // "baslamadi", "devam_ediyor", "bitti"
    val olusturmaZamani: Long,
    val bitisZamani: Long? = null
)