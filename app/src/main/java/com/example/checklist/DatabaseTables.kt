package com.example.checklist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "AccountInfo")
data class AccountInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "username")
    var username: String = "",

    @ColumnInfo(name = "password")
    var password: String = ""
)

@Entity(tableName = "GroupParticipants")
data class GroupParticipants(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val description: String = ""
)

@Entity(tableName = "Groups")
data class Groups(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val description: String = ""
)
