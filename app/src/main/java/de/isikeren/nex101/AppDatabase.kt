package de.isikeren.nex101

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        OyuncuEntity::class,
        OyunEntity::class,
        OyunKatilimciEntity::class,
        TurEntity::class,
        CezaEntity::class,
        TurOyuncuSonucEntity::class
    ],
    version = 3,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun oyuncuDao(): OyuncuDao
    abstract fun oyunDao(): OyunDao
    abstract fun oyunKatilimciDao(): OyunKatilimciDao
    abstract fun turDao(): TurDao
    abstract fun cezaDao(): CezaDao
    abstract fun turOyuncuSonucDao(): TurOyuncuSonucDao
}