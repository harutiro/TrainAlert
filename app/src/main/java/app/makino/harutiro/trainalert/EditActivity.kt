package app.makino.harutiro.trainalert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.*
import app.makino.harutiro.trainalert.adapter.EditRecycleViewAdapter
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import app.makino.harutiro.trainalert.dateBase.RouteListDateClass
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import android.widget.LinearLayout
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.iterator
import com.google.android.material.snackbar.Snackbar


class EditActivity : AppCompatActivity(), OnMapReadyCallback {

    val tag = "debag"

    //    データ受け渡し
    var id: String? = ""

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)


        var lat01 = 0.0
        var lon01 = 0.0
        var lat02 = 0.0
        var lon02 = 0.0

//       ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝リニアレイアウトの追加部分
        val editAddRouteLiniurLayout = findViewById<LinearLayout>(R.id.editAddRouteLinearLayout)

        if (id == "") {

            val newRoute = listOf<RouteListDateClass>(
                RouteListDateClass(start = true),
                RouteListDateClass(end = true)
            )
            for (i in newRoute) {
                addNLinearLayOutRouteItem(editAddRouteLiniurLayout, i, null)
            }


        } else {
            val realmResalt = realm.where(RouteDateClass::class.java).equalTo("id", id).findFirst()
        }


//        ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝セーブ部分
        findViewById<FloatingActionButton>(R.id.editSaveFab).setOnClickListener {

            Snackbar.make(findViewById(android.R.id.content),"保存中", Snackbar.LENGTH_SHORT).show()

            var saveArrayDate = ArrayList<RouteListDateClass>()

            var indexCount = 0
            for (i in editAddRouteLiniurLayout) {
                val saveDate = RouteListDateClass()

                if (i.findViewById<View>(R.id.itemEditTopLineView).visibility == GONE) {
                    saveDate.start = true
                }
                if (i.findViewById<View>(R.id.itemEditButtomLineView).visibility == GONE) {
                    saveDate.end = true
                }

                saveDate.indexCount = indexCount


//               ＋＋＋＋＋＋＋＋＋＋＋＋＋＋＋＋＋PlaceID取得部分

                Places.initialize(application, "AIzaSyCbnAj8bhSfWi4vuDTZa--6OnnFk7VUm7g")
                val localLanguageName = i.findViewById<EditText>(R.id.itemEditRouteEditText).text.toString()
//                Token作成
                val token = AutocompleteSessionToken.newInstance()
//                PlaceIDのリクエストするビルダー作成
                val request =
                    FindAutocompletePredictionsRequest.builder()
                        .setCountries("JP")
                        .setSessionToken(token)
                        .setQuery(localLanguageName)
                        .build()

//                実際にリクエストを投げる部分
                val placesClient = Places.createClient(this)
                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->

//                        ＋＋＋＋＋＋＋＋＋＋PlaceIDから緯度経度の取得

                        for (prediction in response.autocompletePredictions) {
//                            取得したいデータの項目を指定
                            val placeFields = listOf(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.LAT_LNG,
                                Place.Field.TYPES
                            )

                            // リクエストするインスタンスの作成
                            val request1 =
                                FetchPlaceRequest.newInstance(prediction.placeId, placeFields)

//                            実際にリクエストを投げる
                            val placesClient = Places.createClient(this)
                            placesClient.fetchPlace(request1)
                                .addOnSuccessListener { response: FetchPlaceResponse ->
//                                    取得したデータを保存
                                    val place = response.place

//                                    もしその住所の属性に駅があったら
                                    if (place.types.contains(Place.Type.TRANSIT_STATION)) {

                                        Log.d(
                                            "debag",
                                            "Place: ${place.name}, ${place.id},${place.types} ,${place.address},${place.latLng}"
                                        )

//                                        住所の配列に保存
                                        saveDate.placeLovalLanguageName = localLanguageName
                                        saveDate.placeName = place.name
                                        saveDate.placeId = place.id
                                        saveDate.placeType = place.types.toString()
                                        saveDate.placeMyAddress = place.address
                                        saveDate.placeLat = place.latLng.latitude
                                        saveDate.placeLon = place.latLng.longitude

//                                        重複保存を回避
                                        if(!saveArrayDate.any { it.placeName == saveDate.placeName }){
                                            saveArrayDate.add(saveDate)
                                        }

                                        return@addOnSuccessListener


                                    }


                                }.addOnFailureListener { exception: Exception ->
//                                    エラー部分
                                    if (exception is ApiException) {
                                        Log.d("debag", "Place not found: ${exception.message}")
                                        val statusCode = exception.statusCode
                                    }
                                }


                        }
                    }.addOnFailureListener { exception: Exception? ->
//                        PlaceIDのエラー部分
                        if (exception is ApiException) {
                            Log.e("debag", "Place not found: " + exception.statusCode)

                        }
                    }

                indexCount++
            }

//            PlaceIDの取得のために５秒ほど送らせてから保存をする
            Handler(Looper.getMainLooper()).postDelayed({
//                住所の配列が０でないとき
                if (saveArrayDate.size != 0){
                    realm.executeTransaction {

                        val new = if (id.isNullOrEmpty()) {
                            realm.createObject(RouteDateClass::class.java, UUID.randomUUID().toString())
                        } else {
                            realm.where(RouteDateClass::class.java).equalTo("id", id).findFirst()
                        }

                        new?.timeAllDayCheck = findViewById<CheckBox>(R.id.editAllDayCheckBox).isChecked
                        new?.timeDeparture =
                            findViewById<EditText>(R.id.editDepartureEditText).text.toString()
                        new?.timeArriva =
                            findViewById<EditText>(R.id.editArrivalEditText).text.toString()
                        new?.weekEveryDay = findViewById<CheckBox>(R.id.editEverydayCheckBox).isChecked
                        new?.weekMon = findViewById<ToggleButton>(R.id.editMondayButton).isChecked
                        new?.weekTue = findViewById<ToggleButton>(R.id.editThursdayButton).isChecked
                        new?.weekWed = findViewById<ToggleButton>(R.id.editWednesdayButton).isChecked
                        new?.weekThe = findViewById<ToggleButton>(R.id.editThursdayButton).isChecked
                        new?.weekFri = findViewById<ToggleButton>(R.id.editFridayButton).isChecked
                        new?.weekSat = findViewById<ToggleButton>(R.id.editSaturdayButton).isChecked
                        new?.weekSun = findViewById<ToggleButton>(R.id.editSundayButton).isChecked

                        new?.routeName = findViewById<EditText>(R.id.editRouteName).text.toString()
                        new?.alertCheck = findViewById<Switch>(R.id.editSwichi).isChecked

                        new?.routeList?.addAll(saveArrayDate)

                    }

                    finish()
                    Snackbar.make(findViewById(android.R.id.content),"保存ができました。", Snackbar.LENGTH_SHORT).show()
                }else{
                    Snackbar.make(findViewById(android.R.id.content),"保存ができませんでした。", Snackbar.LENGTH_SHORT).show()
                }
            }, 5000)
        }
    }


////       ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝リサイクラービュー
//        val rView = findViewById<RecyclerView>(R.id.editRV)
//        adapter = EditRecycleViewAdapter(this , object: EditRecycleViewAdapter.OnItemClickListner{})
//        rView.layoutManager = LinearLayoutManager(this)
//        rView.adapter = adapter
//        if(id == ""){
//            adapter?.setList(
//                listOf<RouteListDateClass>(
//                    RouteListDateClass(start = true),
//                    RouteListDateClass(end = true)
//                )
//            )
//        }else{
//            val realmResalt = realm.where(RouteDateClass::class.java).equalTo("id",id).findFirst()
//            adapter?.setList(realmResalt?.routeList!!)
//        }


    fun addNLinearLayOutRouteItem(
        editAddRouteLiniurLayout: LinearLayout,
        i: RouteListDateClass?,
        index: Int?
    ) {
//        val imageButtonId = ArrayList<Int>()
//        imageButtonId.add(ViewCompat.generateViewId())

        val v: View = layoutInflater.inflate(R.layout.item_course_edit_date, null)
//        v.id = imageButtonId.last()

        v.findViewById<Button>(R.id.itemEditAddButton).setOnClickListener() {
            addNLinearLayOutRouteItem(
                editAddRouteLiniurLayout,
                null,
                (v.parent as ViewGroup).indexOfChild(v) + 1
            )
        }

        if (i?.start == true) {
            v.findViewById<View>(R.id.itemEditTopLineView).visibility = GONE
        }
        if (i?.end == true) {
            v.findViewById<View>(R.id.itemEditButtomLineView).visibility = GONE
            v.findViewById<Button>(R.id.itemEditAddButton).visibility = GONE
        }


        if (index != null) {
            editAddRouteLiniurLayout.addView(v, index)
        } else {
            editAddRouteLiniurLayout.addView(v)
        }
    }

    override fun onMapReady(p0: GoogleMap) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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