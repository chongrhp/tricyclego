package com.example.tricyclego.fragments.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tricyclego.R
import com.google.android.gms.maps.GoogleMap

class MapViewModel: ViewModel() {
    private val _addActivity = MutableLiveData<Boolean>()
    val addActivity : LiveData<Boolean> get() = _addActivity
    private val _driverResponse = MutableLiveData<Boolean>()
    val driverResponse : LiveData<Boolean> get() = _driverResponse
    private val _serviceCompleted = MutableLiveData<Boolean>()
    val serviceCompleted : LiveData<Boolean> get() = _serviceCompleted
    private val _orgLat = MutableLiveData<Double>()
    val orgLat : LiveData<Double> get() = _orgLat
    private val _orgLng = MutableLiveData<Double>()
    val orgLng : LiveData<Double> get() = _orgLng
    private val _btnSearch = MutableLiveData<Boolean>()
    val btnSearch : LiveData<Boolean> get() = _btnSearch
    private val _orgPlace = MutableLiveData<String>()
    val orgPlace : LiveData<String> get() = _orgPlace
    private val _desPlace = MutableLiveData<String>()
    val desPlace : LiveData<String> get() = _desPlace

    fun changeMap(itemId: Int, mGoogleMap: GoogleMap) {
        when (itemId) {
            R.id.normal_map -> mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> mGoogleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> mGoogleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> mGoogleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    fun setBtnSearch(btnSearh: Boolean){
        _btnSearch.value = btnSearh
    }

    fun setOrgPlace(orgPlace: String){
        _orgPlace.value = orgPlace
    }

    fun setDesPlace(desPlace: String){
        _desPlace.value = desPlace
    }

    //fun driveResponse(isTrue: Boolean) {_driverResponse.value = isTrue}
    fun setService(complete: Boolean){_serviceCompleted.value = complete}


}