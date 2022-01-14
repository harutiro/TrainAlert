package app.makino.harutiro.trainalert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


class EditActivity : AppCompatActivity(), OnMapReadyCallback {

    val tag = "debag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)


        var lat01 = 0.0
        var lon01 = 0.0
        var lat02 = 0.0
        var lon02 = 0.0




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
                lat01 = place.latLng.latitude
                lon01 = place.latLng.longitude

            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.d("debug", "An error occurred: $status")
            }
        })

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment2 =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment2)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment2.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME ,Place.Field.ADDRESS ,Place.Field.LAT_LNG ,Place.Field.TYPES))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment2.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.d("debug", "Place: ${place.name}, ${place.id},${place.types} ,${place.address},${place.latLng}")
                lat02 = place.latLng.latitude
                lon02 = place.latLng.longitude

                val distance = getDistance(lat01, lon01, lat02, lon02, 'K')
                Log.d("debug",distance.toString())
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


    /*
     * 2点間の距離を取得
     * 第五引数に設定するキー（unit）で単位別で取得できる
     */
    private fun getDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        unit: Char
    ): Double {
        val theta = lon1 - lon2
        var dist =
            sin(deg2rad(lat1)) * sin(deg2rad(lat2)) + cos(deg2rad(lat1)) * cos(
                deg2rad(lat2)
            ) * cos(deg2rad(theta))
        dist = acos(dist)
        dist = rad2deg(dist)
        val miles = dist * 60 * 1.1515
        return when (unit) {
            'K' -> miles * 1.609344
            'N' -> miles * 0.8684
            'M' -> miles
            else -> miles
        }
    }

    private fun rad2deg(radian: Double): Double {
        return radian * (180f / Math.PI)
    }

    fun deg2rad(degrees: Double): Double {
        return degrees * (Math.PI / 180f)
    }


}