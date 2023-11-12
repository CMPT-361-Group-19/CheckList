package com.example.checklist

import androidx.lifecycle.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.annotation.WorkerThread
import java.lang.IllegalArgumentException
class AccountViewModel(private val repository: AccountRepository) {
    val allEntriesLiveData : LiveData<List<AccountInfo>> = repository.allAccounts.asLiveData()

    fun insert(account : AccountInfo) {
        repository.insert(account)
    }

    fun deleteEntry(pos : Int) {
        val accountList = allEntriesLiveData.value
        if (accountList != null && accountList.size > 0) {
            val id = accountList[pos].id
            repository.delete(id)
        }
    }
    }



