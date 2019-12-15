package com.realtwin.goforplastic.mapfilters

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

class FilterItemsManager(val context: Context) {

    //Map icons filter staff
    private val categoryMarkerName = HashMap<FilterCategory, String>()
    private var filterItems = FilterItems()

    init {
        initializeFromFile()
    }

    private fun initializeFromFile(){
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val jsonString = sharedPref.getString(MARKERPREF, null)
        filterItems = if(jsonString != null){
            Gson().fromJson<FilterItems>(jsonString, FilterItems::class.java)
        }
        else{
            FilterItems(HashMap())
        }
    }

    fun AddMarker(filterCategory: FilterCategory, location: LatLng){
        if(filterItems.categoryItems == null){
            initializeFromFile()
        }
        filterItems.categoryItems?.let{
            if(it.contains(filterCategory)){
                it[filterCategory]!!.add(location)
            }
            else{
                it.put(filterCategory, arrayListOf(location))
            }
        }
        UpdateFile();
    }

    fun GetMarkers(filterCategory: FilterCategory): ArrayList<LatLng>?{
        return filterItems?.categoryItems?.get(filterCategory)
    }

    fun UpdateFile(){
        val jsonString = Gson().toJson(filterItems)
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        with(sharedPref.edit()){
            putString(MARKERPREF, jsonString).commit()
        }
    }

    fun ClearMarkers(){
        filterItems.categoryItems?.clear()
        UpdateFile()
    }

    companion object {
        const val MARKERPREF = "markersPref"
    }
}