package com.drcmind.cleaapp.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.drcmind.cleaapp.data.local.room.dao.AgendaDao
import com.drcmind.cleaapp.data.local.room.dao.ArticleDao
import com.drcmind.cleaapp.data.local.room.dao.MenstrualDao
import com.drcmind.cleaapp.data.local.room.entity.*

@Database(
    entities = [
        CycleEntity::class,
        DayEntity::class,
        SymptomEntity::class,
        DaySymptomCrossRef::class,
        AgendaItemEntity::class,
        ArticleEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CleaDatabase : RoomDatabase() {
    abstract val menstrualDao: MenstrualDao
    abstract val agendaDao: AgendaDao
    abstract val articleDao: ArticleDao
}
