package com.example.checklist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM AccountInfo")
    fun getAllAccount(): Flow<List<AccountInfo>>

    @Insert
    fun insert(accountInfo: AccountInfo)

    @Query ("DELETE FROM accountinfo")
    fun deleteAll()

    @Query ("DELETE FROM accountinfo WHERE id = :key")
    fun deleteEntry(key:Long)
}

@Dao
interface ParticipantsDao {
    @Query("SELECT * FROM GroupParticipants")
    fun getAllParticipants(): Flow<List<GroupParticipants>>

    @Insert
    fun insert(groupParticipants: GroupParticipants)

    @Query ("DELETE FROM groupparticipants")
    fun deleteAll()

    @Query ("DELETE FROM groupparticipants WHERE id = :key")
    fun deleteEntry(key:Long)
}

@Dao
interface GroupsDao {
    @Query("SELECT * FROM Groups")
    fun getAllGroups(): Flow<List<Groups>>

    @Insert
    fun insert(groups: Groups)

    @Query ("DELETE FROM groups")
    fun deleteAll()

    @Query ("DELETE FROM groups WHERE id = :key")
    fun deleteEntry(key:Long)
}