package com.example.videojuegosandroidtienda.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.entities.VideogamePost2
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideogameViewModel : ViewModel() {
    private val repository = VideogameRepository()

    private val cacheById: MutableMap<String, Videogame> = mutableMapOf()
    private var cachedList: List<Videogame>? = null

    private val _videogames = MutableLiveData<List<Videogame>>()
    val videogames: LiveData<List<Videogame>> = _videogames

    suspend fun getVideogames(): List<Videogame> = withContext(Dispatchers.IO) {
        cachedList?.let { list ->
            _videogames.postValue(list)
            return@withContext list
        }
        val list = repository.getVideogames()
        cachedList = list
        list.forEach { vg -> vg.id?.let { cacheById[it] = vg } }
        _videogames.postValue(list)
        list
    }

    suspend fun getVideogame(id: String): VideogamePost2 = withContext(Dispatchers.IO) {
        repository.getVideogameById(id)
    }
}