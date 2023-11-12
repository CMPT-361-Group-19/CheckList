package com.example.checklist

import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {
    val allAccounts : Flow <List<AccountInfo>> = accountDao.getAllAccount()

    fun insert(account : AccountInfo) {
        Thread { accountDao.insert(account) }.start() }

    fun delete(id: Long) {
        Thread { accountDao.deleteEntry(id) }.start() }

    fun deleteAll() {
        Thread { accountDao.deleteAll() }.start() }
}

class ParticipantsRepository(private val participantsDao: ParticipantsDao) {
    val allParticipants : Flow <List<GroupParticipants>> = participantsDao.getAllParticipants()

    fun insert(groupParticipants: GroupParticipants) {
        Thread { participantsDao.insert(groupParticipants) }.start() }

    fun delete(id: Long) {
        Thread { participantsDao.deleteEntry(id) }.start() }

    fun deleteAll() {
        Thread { participantsDao.deleteAll() }.start() }
}

class GroupsRepository(private val groupsDao: GroupsDao) {
    val allGroups : Flow <List<Groups>> = groupsDao.getAllGroups()

    fun insert(groups: Groups) {
        Thread { groupsDao.insert(groups) }.start() }

    fun delete(id: Long) {
        Thread { groupsDao.deleteEntry(id) }.start() }

    fun deleteAll() {
        Thread { groupsDao.deleteAll() }.start() }
}



