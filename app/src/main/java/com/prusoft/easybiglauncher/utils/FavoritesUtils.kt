package com.prusoft.easybiglauncher.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class FavoriteContact(val name: String, val number: String)

object FavoritesUtils {
    fun getFavorites(context: Context): List<FavoriteContact> {
        val prefs = context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("favorites", "[]") ?: "[]"
        val list = mutableListOf<FavoriteContact>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(FavoriteContact(obj.getString("name"), obj.getString("number")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun addFavorite(context: Context, name: String, number: String) {
        val prefs = context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE)
        val current = getFavorites(context).toMutableList()
        if (current.none { it.number == number }) {
            current.add(FavoriteContact(name, number))
            val array = JSONArray()
            current.forEach {
                val obj = JSONObject()
                obj.put("name", it.name)
                obj.put("number", it.number)
                array.put(obj)
            }
            prefs.edit().putString("favorites", array.toString()).apply()
        }
    }

    fun removeFavorite(context: Context, number: String) {
        val prefs = context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE)
        val current = getFavorites(context).toMutableList()
        current.removeAll { it.number == number }
        val array = JSONArray()
        current.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("number", it.number)
            array.put(obj)
        }
        prefs.edit().putString("favorites", array.toString()).apply()
    }
}
