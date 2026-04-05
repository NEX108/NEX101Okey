package de.isikeren.nex101

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oyuncular")
data class OyuncuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ad: String
)