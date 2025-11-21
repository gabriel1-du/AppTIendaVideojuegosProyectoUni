package com.example.videojuegosandroidtienda.data.network

import com.example.videojuegosandroidtienda.data.entities.AuthTokenResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class AuthTokenResponseDeserializer : JsonDeserializer<AuthTokenResponse> {
    private val candidates = listOf("token", "authToken", "access_token", "jwt")

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): AuthTokenResponse {
        val token = findTokenRecursive(json)
            ?: throw JsonParseException("Auth token field not found in response")
        return AuthTokenResponse(token)
    }

    private fun findTokenRecursive(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null
        if (element.isJsonPrimitive) {
            return null
        }
        if (element.isJsonObject) {
            val obj = element.asJsonObject
            for (key in candidates) {
                if (obj.has(key) && obj.get(key).isJsonPrimitive) {
                    val prim = obj.get(key).asJsonPrimitive
                    if (prim.isString) return prim.asString
                }
            }
            // buscar en propiedades anidadas
            for ((_, v) in obj.entrySet()) {
                val found = findTokenRecursive(v)
                if (found != null) return found
            }
        }
        if (element.isJsonArray) {
            val arr = element.asJsonArray
            for (item in arr) {
                val found = findTokenRecursive(item)
                if (found != null) return found
            }
        }
        return null
    }
}