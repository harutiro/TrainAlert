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

        val placesClient = Places.createClient(this)

        // Define a Place ID.
        val placeId = "EicxMyBNYXJrZXQgU3QsIFdpbG1pbmd0b24sIE5DIDI4NDAxLCBVU0EiGhIYChQKEgnRTo6ixx-qiRHo_bbmkCm7ZRAN"

// Specify the fields to return.
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME)

// Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.d("debug", "Place found: ${place.name}")
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.d("debug", "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                }
            }


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



        //情報取得を別のスレッドでおこなうようにする
        runBlocking(Dispatchers.IO){
            runCatching {
                val client: OkHttpClient = OkHttpClient().newBuilder()
                    .build()
                val requests: Request = Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyCWlZTj9siY9TFizSf06yrt55FaDmIZLjc")
                    .method("GET", null)
                    .build()
                val response: Response = client.newCall(requests).execute()
                Log.d("debug",response.toString())
            }
        }.onSuccess{

        }.onFailure {
            //失敗した時のところ。
            Toast.makeText(this,"失敗",Toast.LENGTH_LONG).show()
        }


        val context = GeoApiContext.Builder()
            .apiKey("AIzaSyCWlZTj9siY9TFizSf06yrt55FaDmIZLjc")
            .build()
        val results = DirectionsApi.newRequest(context)
            .origin("愛知御津駅")
            .destination("豊橋駅")
            .mode(TravelMode.DRIVING)
            .await()


        val gson = GsonBuilder().setPrettyPrinting().create()
        Log.d("debug2", "====================================================")
        Log.d("debug2", gson.toJson(results))

//        context.shutdown()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


    }

    override fun onMapReady(p0: GoogleMap) {

    }


}