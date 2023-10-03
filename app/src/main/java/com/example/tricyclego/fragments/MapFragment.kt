package com.example.tricyclego.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tricyclego.Login
import com.example.tricyclego.R
import com.example.tricyclego.data.VTricycle
import com.example.tricyclego.data.availableId
import com.example.tricyclego.data.passContactNo
import com.example.tricyclego.data.passengerAddress
import com.example.tricyclego.data.phoneCall
import com.example.tricyclego.databinding.DialogCompleteServiceBinding
import com.example.tricyclego.databinding.FragmentMapBinding
import com.example.tricyclego.fragments.map.MapAdaptor
import com.example.tricyclego.fragments.map.MapViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.TravelMode
import java.io.IOException
import java.util.Locale

@Suppress("DEPRECATION", "SameParameterValue")
class MapFragment : Fragment(), OnMapReadyCallback {
    init {
        // Retain the fragment instance across configuration changes
        retainInstance = true
    }

    private lateinit var nMap: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var _binding :  FragmentMapBinding
    private val binding get() = _binding

    private var origLat = 0.0
    private var origLng = 0.0
    private var destLat = 0.0
    private var destLng = 0.0
    private var passInKm = 0.0
    private var disInKm = 0.0
    private var paidFare = 0.0
    private var passId = ""

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
        private const val REQUEST_CALL_PERMISSION = 1
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _binding.lifecycleOwner = this

        sharedPreferences = this.requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        passContactNo = sharedPreferences.getString("contact_no","No Contact no.").toString()

        val viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        val rdOrig = binding.rdBtnOrig
        val rdDest = binding.rdBtnDest
        val btnFindTricycle = binding.btnSearch



        recyclerView = binding.recyclerVacantTricycle
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //viewModel.setService(false)
        rdOrig.setOnCheckedChangeListener { _, b -> rdDest.isChecked = !b }
        rdDest.setOnCheckedChangeListener { _, b -> rdOrig.isChecked = !b }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val mapOptionButton = binding.btnMapOption
        val popUpMenu = PopupMenu(this.context, mapOptionButton)
        popUpMenu.menuInflater.inflate(R.menu.map_option, popUpMenu.menu)
        popUpMenu.setOnMenuItemClickListener {
            showMapType(it.itemId)
            true
        }


        mapOptionButton.setOnClickListener {
            popUpMenu.show()
        }

        btnFindTricycle.setOnClickListener {
            it.isVisible = false
            binding.lblAvailableOptions.isVisible = false
            recyclerView.isVisible = false
            showVacantTricycle()
        }

        Places.initialize(requireContext(),getString(R.string.none_restricted)) //Initialize places
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity()) //Initialize current location

        autocompleteFragment = childFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
            override fun onError(p0: Status) {
                Toast.makeText(this@MapFragment.context, "Some Error in Search", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("SetTextI18n")
            override fun onPlaceSelected(place: Place) {
                nMap.clear()

                if(rdOrig.isChecked){
                    origLat = place.latLng!!.latitude
                    origLng = place.latLng!!.longitude
                    binding.txtOrig.text = Html.fromHtml("<b>My location:</b> ${place.address}")
                    passengerAddress = place.address!!
                } else {
                    destLat = place.latLng!!.latitude
                    destLng = place.latLng!!.longitude
                    binding.txtDest.text = Html.fromHtml("<b>Destination:</b> ${place.address}")
                    btnFindTricycle.text = "Find tricycle"
                    btnFindTricycle.isVisible = true
                    binding.lblAvailableOptions.isVisible = false
                    binding.recyclerVacantTricycle.isVisible = false
                    binding.txtSelectedDestination.text = Html.fromHtml("<font color='#1E8323'><b>Destination:</b></font>\n${place.address}")
                }


                nMap.addMarker(MarkerOptions().position(LatLng(origLat,origLng)))
                if(rdDest.isChecked) drawRoute(nMap)
                if(!rdOrig.isChecked) nMap.addMarker(MarkerOptions().position(LatLng(destLat,destLng)))
                else nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(origLat,origLng), 16f))

            }

        })


        viewModel.serviceCompleted.observe(this.requireActivity()){isCompleted ->
            if(isCompleted == true) {
                val db1 = FirebaseFirestore.getInstance()
                val collRef =  db1.collection("vacant_tricycle").document(availableId)
                collRef.get()
                    .addOnSuccessListener {document ->
                        if(document.exists()){
                            val isComplete = document.get("completed") as Boolean
                            if(isComplete) showCompletedService()
                            if(isComplete){
                                //showCompletedService()
                                nMap.clear()
                                binding.btnSearch.isVisible = false
                                binding.recyclerVacantTricycle.isVisible = false
                                binding.lblAvailableOptions.isVisible = false
                                binding.btnCallDriver.isVisible = false
                                binding.cardSearch.isVisible = true
                                binding.txtSelectedDestination.isVisible = false
                                binding.cardLocation.isVisible = true
                                binding.rdBtnOrig.isChecked = true
                            } else {
                                binding.btnSearch.isVisible = false
                                binding.lblAvailableOptions.text = "Selected tricycle"
                                binding.btnCallDriver.isVisible = true
                                binding.cardSearch.isVisible = false
                                binding.txtSelectedDestination.isVisible = true
                                binding.txtDest.text = Html.fromHtml("<b>Destination:</b>")
                                binding.txtOrig.text = Html.fromHtml("<b>My location:</b>")
                                binding.cardLocation.isVisible = false
                            }

                        }
                    }
            }

        }


        val callButton = binding.btnCallDriver
        callButton.setOnClickListener {
            // Check for permission to make a call
            if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission granted, make the call
                makePhoneCall()
            } else {
                // Request permission to make the call
                ActivityCompat.requestPermissions(
                    this.requireActivity(),
                    arrayOf(Manifest.permission.CALL_PHONE),
                    REQUEST_CALL_PERMISSION
                )
            }
        }

        return binding.root
    }

    private fun makePhoneCall() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneCall")

        try {
            startActivity(intent)
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this.requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showMapType(itemId: Int){
        when (itemId) {
            R.id.normal_map -> nMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> nMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> nMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> nMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            R.id.mnuLogout -> logOut()
        }
    }

    private fun logOut(){
        val myIntent = Intent(requireContext(),Login::class.java)
        startActivity(myIntent)
        activity?.finish()
    }

    override fun onMapReady(map: GoogleMap) {
        nMap = map
        nMap.uiSettings.isZoomControlsEnabled = true
        nMap.uiSettings.isMyLocationButtonEnabled = true
        nMap.uiSettings.isCompassEnabled = true
        nMap.isIndoorEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            this.activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE)
            }
            return
        }

        nMap.isMyLocationEnabled = true
        nMap.mapType = GoogleMap.MAP_TYPE_SATELLITE


        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if(location !=null){
                origLat = location.latitude
                origLng = location.longitude
                //passengerAddress = location.get
                val markerPosition = LatLng(origLat, origLng)

                val markerOptions = MarkerOptions()
                    .position(markerPosition)
                    .title("tricycle")

                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_account_circle_24))
                val marker = nMap.addMarker(markerOptions)

                val currentLatLong = LatLng(location.latitude, location.longitude)

                //nMap.addMarker(MarkerOptions().position(LatLng(origLat,origLng)))
                nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position, 18f))
                getAddressFromLocation(origLat, origLng)
            }
        }

    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this.requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address> =
                geocoder.getFromLocation(latitude, longitude, 1) as List<Address>


            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                val fullAddress = address.getAddressLine(0) // Full address as a string
                binding.txtOrig.text = Html.fromHtml("<b>My location</b>: $fullAddress")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //Show list of available tricycles
    @SuppressLint("SetTextI18n")
    private fun showVacantTricycle(){
        binding.mapProgressBar.isVisible = true
        val viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        viewModel.setService(false)
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("vacant_tricycle")
        val query = collectionRef.limit(3)
        query.get()
            .addOnSuccessListener { querySnapShot ->

                binding.btnSearch.text = "Search again..."
                binding.btnSearch.isVisible = true
                binding.lblAvailableOptions.isVisible = true
                binding.recyclerVacantTricycle.isVisible = true
                recyclerView.isVisible = true


                val vacantTricycleList = mutableListOf<VTricycle>()
                for(documents in querySnapShot.documents){
                    val tricycle = documents.data
                    val actId = documents.id
                    val dtOnline = tricycle?.get("dtOnline") as Timestamp
                    val driverId = tricycle.get("driverId").toString()
                    val contactNo = tricycle.get("contact_no").toString()
                    val triLat = tricycle.get("orgLat").toString().toDouble()
                    val triLng = tricycle.get("orgLng").toString().toDouble()
                    val driverName = tricycle.get("driver_name").toString()
                    val tricycleNo = tricycle.get("tricycle_no").toString()
                    val picked = tricycle.get("picked") as Boolean
                    val accepted = tricycle.get("accepted") as Boolean
                    val rejected = tricycle.get("rejected") as Boolean
                    val completed = tricycle.get("completed") as Boolean

                    val newAvailable = VTricycle(actId, dtOnline, driverId, passId, driverName, tricycleNo,
                        triLat, triLng, origLat, origLng, destLat, destLng, contactNo, picked, accepted, rejected,
                    passInKm, disInKm, paidFare, completed)
                    vacantTricycleList.add(newAvailable)
                }

                if(vacantTricycleList.size > 0) {
                    recyclerView.adapter = MapAdaptor(this.requireContext(), viewModel, vacantTricycleList)
                    binding.mapProgressBar.isVisible = false
                } else {
                    binding.btnSearch.text = "Search again?..."
                    binding.lblAvailableOptions.text = "Cannot find tricycle.."
                }
            }

            .addOnFailureListener {
                binding.mapProgressBar.isVisible = false
                binding.btnSearch.text = "Search again?..."
                binding.btnSearch.isVisible = true
                binding.lblAvailableOptions.text = "No available tricycle"
                binding.lblAvailableOptions.isVisible = true
            }


    }

    private fun drawRoute(map: GoogleMap){
        val origin = com.google.maps.model.LatLng(origLat,origLng)
        val destin = com.google.maps.model.LatLng(destLat,destLng)

        val geoApiContext = GeoApiContext.Builder()
            .apiKey(getString(R.string.none_restricted))
            .build()

        val directions = DirectionsApi.newRequest(geoApiContext)
            .origin(origin)
            .destination(destin)
            .mode(TravelMode.DRIVING)
            .await()

        val decodePath =
            PolylineEncoding.decode(directions.routes[0].overviewPolyline.encodedPath)
        val options = PolylineOptions()
            .color(Color.CYAN)
            .width(10f)

        for (point in decodePath) {
            options.add(LatLng(point.lat, point.lng))
        }

        map.addPolyline(options)

        val builder = LatLngBounds.Builder()
            .include(LatLng(origin.lat,origin.lng))
            .include(LatLng(destin.lat,destin.lng))
            .build()

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder, 100))
    }


    private fun showCompletedService(){
        val completeMessage = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_complete_service, null)
        val binding = DialogCompleteServiceBinding.bind(dialogView)

        val btnOk = binding.btnCompleteOk
        completeMessage.setView(dialogView)
        val showDialog = completeMessage.create()

        btnOk.setOnClickListener {
            setServiceIncomplete(false)
            showDialog.dismiss()
        }

        showDialog.show()

    }

    private fun setServiceIncomplete(isComplete: Boolean){
        val viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        viewModel.setService(isComplete)
    }

}


