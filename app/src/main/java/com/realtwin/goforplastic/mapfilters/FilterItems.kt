package com.realtwin.goforplastic.mapfilters

import android.location.Location
import com.mapbox.mapboxsdk.geometry.LatLng


data class FilterItems(var categoryItems: HashMap<FilterCategory, ArrayList<LatLng>>? = null){

    constructor() : this(null)
}