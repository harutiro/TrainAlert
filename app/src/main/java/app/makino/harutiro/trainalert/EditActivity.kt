package app.makino.harutiro.trainalert

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.*
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import app.makino.harutiro.trainalert.dateBase.RouteListDateClass
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import java.util.*
import android.widget.LinearLayout
import android.view.ViewGroup
import androidx.core.view.iterator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar


class EditActivity : AppCompatActivity(), OnMapReadyCallback {

//    データ受け渡し
    var id: String? = ""

//    レルム
    private val realm by lazy {
        Realm.getDefaultInstance()
    }

//    GoogleMapの操作ができるやつ
    private lateinit var googleMap: GoogleMap
//    ルートのリストを保存しておく部分
    var routeLists = ArrayList<RouteListDateClass>()
//    マップの丸や円を消去するために残しておくリスト
    val circlesList = ArrayList<Circle>()
    val polyLineList = ArrayList<Polyline>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        //マップとフラグメントを結び透ける部分
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.editMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // MainActivityのRecyclerViewの要素をタップした場合はidが，fabをタップした場合は"空白"が入っているはず
        id = intent.getStringExtra("id")

//       ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝項目初期化部分
        val editAddRouteLiniurLayout = findViewById<LinearLayout>(R.id.editAddRouteLinearLayout)

        if (id.isNullOrEmpty()) {
//          新規
            val newRoute = listOf<RouteListDateClass>(
                RouteListDateClass(start = true),
                RouteListDateClass(end = true)
            )
            for (i in newRoute) {
                addNLinearLayOutRouteItem(editAddRouteLiniurLayout, i, null)
            }


        } else {
//            編集
//            ※円や線のマップ関係の初期設定はonMapReadyで行う。
            val realmResalt = realm.where(RouteDateClass::class.java).equalTo("id", id).findFirst()

            findViewById<EditText>(R.id.editRouteName).setText(realmResalt?.routeName)

            if (realmResalt != null) {
                for (i in realmResalt.routeList?.sortedBy { it.indexCount }!!) {
                    addNLinearLayOutRouteItem(editAddRouteLiniurLayout, i, null)
                }
            }
        }

//        ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝編集したるーとを手動で反映させる部分
        findViewById<Button>(R.id.editSearchButton).setOnClickListener{
            Snackbar.make(findViewById(android.R.id.content),"検索中", Snackbar.LENGTH_SHORT).show()

            searchRouteList(editAddRouteLiniurLayout)
            Handler(Looper.getMainLooper()).postDelayed({
                routeAdd()
            },5000)



        }


//        ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝セーブ部分
        findViewById<FloatingActionButton>(R.id.editSaveFab).setOnClickListener {

            Snackbar.make(findViewById(android.R.id.content),"保存中", Snackbar.LENGTH_SHORT).show()

//            ルートの緯度経度などのリストを取得する
            searchRouteList(editAddRouteLiniurLayout)

//            PlaceIDの取得のために５秒ほど送らせてから保存をする
            Handler(Looper.getMainLooper()).postDelayed({
//                住所の配列が０でないとき（０のときは保存できなかったと仮定する）
                if (routeLists.size != 0){
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

                        new?.routeList?.clear()
                        new?.routeList?.addAll(routeLists.sortedBy { it.indexCount })

                    }

                    finish()
                    Snackbar.make(findViewById(android.R.id.content),"保存ができました。", Snackbar.LENGTH_SHORT).show()
                }else{
                    Snackbar.make(findViewById(android.R.id.content),"保存ができませんでした。", Snackbar.LENGTH_SHORT).show()
                }
            }, 5000)
        }
    }


    fun searchRouteList(editAddRouteLiniurLayout: LinearLayout) {
        routeLists.clear()

        var indexCount = 0
        for (i in editAddRouteLiniurLayout) {

            Log.d("debag6",i.findViewById<EditText>(R.id.itemEditRouteEditText).text.toString())
            Log.d("debag6",indexCount.toString())
//            保存する要素を作成
            val saveDate = RouteListDateClass()
//            ルートのラインが見えていないところで最初か最後かを判断する。
            if (i.findViewById<View>(R.id.itemEditTopLineView).visibility != VISIBLE) {
                saveDate.start = true
            }
            if (i.findViewById<View>(R.id.itemEditButtomLineView).visibility != VISIBLE) {
                saveDate.end = true
            }
//            保存する順番を残すために残しておく
            saveDate.indexCount = indexCount


//               ＋＋＋＋＋＋＋＋＋＋＋＋＋＋＋＋＋PlaceID取得部分
//                APIキーの指定
//            TODO:STRINGに登録
            Places.initialize(application, "AIzaSyCbnAj8bhSfWi4vuDTZa--6OnnFk7VUm7g")
//                検索ワードの取得
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
                                    if(!routeLists.any { it.placeName == saveDate.placeName }){
                                        routeLists.add(saveDate)
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
    }

    fun routeAdd(){
//        保存されているリストが順番道理ではないため、順番に並べ替えられたものを新規作成
        val sortedRouteLists = routeLists.sortedBy { it.indexCount }

//        すでにある円や線を消去
        for(i in circlesList){
            i.remove()
        }
        circlesList.clear()
        for(i in polyLineList){
            i.remove()
        }
        polyLineList.clear()

//        円や線の追加
        for ((index,j) in sortedRouteLists.withIndex()){

//            円の追加 設定するときに消すことが出来るようにリストに保存をしておく
            val latLng = LatLng(j.placeLat, j.placeLon) // 場所
            val radius = 400.0// 400ｍ
            circlesList.add(
                googleMap.addCircle(
                    CircleOptions()
                        .center(latLng)          // 円の中心位置
                        .radius(radius)          // 半径 (メートル単位)
                        .strokeColor(Color.BLUE) // 線の色
                        .strokeWidth(2f)         // 線の太さ
                        .fillColor(0x400080ff)   // 円の塗りつぶし色
                )
            )


//            線の追加 設定するときに消すことが出来るようにリストに保存をしておく
            if (index < routeLists.size - 1){
                polyLineList.add(
                    googleMap.addPolyline(
                        PolylineOptions()
                            .add(LatLng(sortedRouteLists[index].placeLat, sortedRouteLists[index].placeLon)) // 1つ目
                            .add(LatLng(sortedRouteLists[index + 1].placeLat,sortedRouteLists[index +1].placeLon)) // 次のルート
                            .color(Color.BLUE)                   // 線の色
                            .width(8f)                          // 線の太さ
                    )
                )
            }

        }
    }



    fun addNLinearLayOutRouteItem(
        editAddRouteLiniurLayout: LinearLayout,
        i: RouteListDateClass?,
        index: Int?
    ) {
//        新規のVIEWを作成
        val v: View = layoutInflater.inflate(R.layout.item_course_edit_date, null)

//        ボタンを押されたときの設定を追加
        v.findViewById<Button>(R.id.itemEditAddButton).setOnClickListener() {
            addNLinearLayOutRouteItem(
                editAddRouteLiniurLayout,
                null,
                (v.parent as ViewGroup).indexOfChild(v) + 1
            )
        }

//        ルートのラインを設定する 最初と最後の部分で表示させるものを指定する
        if (i?.start == true) {
            v.findViewById<View>(R.id.itemEditTopLineView).visibility = INVISIBLE
        }
        if (i?.end == true) {
            v.findViewById<View>(R.id.itemEditButtomLineView).visibility = INVISIBLE
            v.findViewById<Button>(R.id.itemEditAddButton).visibility = INVISIBLE
        }

//        テキストの表示
        if(!i?.placeLovalLanguageName.isNullOrBlank()){
            v.findViewById<EditText>(R.id.itemEditRouteEditText).setText(i?.placeLovalLanguageName)
        }

//        追加ボタンを押したときの動作、途中で組み込むためにインデックスを指定する。
        if (index != null) {
            editAddRouteLiniurLayout.addView(v, index)
        } else {
            editAddRouteLiniurLayout.addView(v)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        //        ツールバーの表示
        googleMap.uiSettings.isMapToolbarEnabled = true
//          自分の位置の表示
        googleMap.isMyLocationEnabled = true

        //                現在地の表示
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // Got last known location. In some rare situations this can be null.
            Log.d("debag", "緯度:" + location?.latitude.toString())
            Log.d("debag", "経度:" + location?.longitude.toString())

//                        カメラ移動
            val osakaStation = LatLng(location!!.latitude, location.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(osakaStation, 16.0f))
        }

//        初期のルートの線や円リストの追加
        val realmResalt = realm.where(RouteDateClass::class.java).equalTo("id", id).findFirst()
        if(!id.isNullOrEmpty()){
            routeLists.addAll(realmResalt?.routeList!!)
            routeAdd()
        }

    }

}