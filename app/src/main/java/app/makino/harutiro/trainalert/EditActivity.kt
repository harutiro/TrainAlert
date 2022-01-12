package app.makino.harutiro.trainalert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import java.util.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import java.lang.Exception
import com.google.gson.GsonBuilder

import com.google.gson.Gson
import com.google.maps.DirectionsApi

import com.google.maps.GeocodingApi

import com.google.maps.model.GeocodingResult

import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi.geocode
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class EditActivity : AppCompatActivity(), OnMapReadyCallback {

    val tag = "debag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)



        Places.initialize(application, "AIzaSyCbnAj8bhSfWi4vuDTZa--6OnnFk7VUm7g")

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME ,Place.Field.ADDRESS ,Place.Field.LAT_LNG ,Place.Field.TYPES))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.d("debug", "Place: ${place.name}, ${place.id},${place.types} ,${place.address},${place.latLng}")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.d("debug", "An error occurred: $status")
            }
        })

//        context.shutdown()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


    }

    override fun onMapReady(p0: GoogleMap) {

    }


}