package com.example.checklist
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [AccountInfo::class, GroupParticipants::class,Groups::class], version = 1)
abstract class ChecklistDatabase : RoomDatabase() {
    abstract val accountDao: AccountDao
    abstract val participantsDao: ParticipantsDao
    abstract val groupDao:GroupsDao

    companion object {

        @Volatile
        private var INSTANCE: ChecklistDatabase? = null

        fun getInstance(context: Context) : ChecklistDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if(instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, ChecklistDatabase::class.java,"app_database").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

}


