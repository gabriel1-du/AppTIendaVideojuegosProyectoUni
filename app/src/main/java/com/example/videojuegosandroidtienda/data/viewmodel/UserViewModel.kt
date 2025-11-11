package com.example.videojuegosandroidtienda.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.data.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    private val cache: MutableMap<String, User> = mutableMapOf()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    suspend fun listUsers(): List<User> = withContext(Dispatchers.IO) {
        val list = repository.listUsers()
        list.forEach { user -> cache[user.id] = user }
        _users.postValue(list)
        list
    }

    suspend fun getUser(userId: String): User = withContext(Dispatchers.IO) {
        val cached = cache[userId]
        if (cached != null) {
            _currentUser.postValue(cached)
            return@withContext cached
        }
        val fetched = repository.getUser(userId)
        cache[userId] = fetched
        _currentUser.postValue(fetched)
        fetched
    }

    suspend fun patchUserBlock(userId: String, blocked: Boolean): User = withContext(Dispatchers.IO) {
        val updated = repository.patchUser(userId, mapOf("bloqueo" to blocked))
        cache[userId] = updated
        _currentUser.postValue(updated)
        updated
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        repository.deleteUser(userId)
        cache.remove(userId)
        if (_currentUser.value?.id == userId) {
            _currentUser.postValue(null)
        }
        _users.value?.let { list ->
            _users.postValue(list.filterNot { it.id == userId })
        }
    }
}