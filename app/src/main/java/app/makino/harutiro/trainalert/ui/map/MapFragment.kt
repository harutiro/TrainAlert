package app.makino.harutiro.trainalert.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.adapter.MapFragmentRecycleViewAdapter
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import app.makino.harutiro.trainalert.dateBase.RouteListDateClass
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.realm.Realm
import io.realm.RealmList
import java.util.ArrayList


class MapFragment : Fragment() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1234
    }


    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    var gMap: GoogleMap? = null
    var adapter: MapFragmentRecycleViewAdapter? = null


    //    マップの丸や円を消去するために残しておくリスト
    val circlesList = ArrayList<Circle>()
    val polyLineList = ArrayList<Polyline>()
    val markerList = ArrayList<Marker>()

    lateinit var mAdView : AdView



    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        gMap = googleMap

        val realmResalt = realm.where(RouteDateClass::class.java).findAll()


        if(requestPermission()){


            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false

            //                現在地の表示
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                Log.d("debag", "緯度:" + location?.latitude.toString())
                Log.d("debag", "経度:" + location?.longitude.toString())

//                        カメラ移動
                val osakaStation = LatLng(location!!.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(osakaStation, 15.0f))
            }

        }


//        通知の範囲をお知らせ
        for (i in realmResalt){
            if(i.routeList != null){
                routeAdd(i.routeList!! , false)
            }
        }



    }

    override fun onResume() {
        super.onResume()

        adapter?.setList(realm.where(RouteDateClass::class.java).findAll())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        //        admob
        MobileAds.initialize(requireContext()) {}

        mAdView = view.findViewById(R.id.flagmentMapAdView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


//        位置情報ボタン
        view.findViewById<ImageButton>(R.id.mapFragmentLocationButton).setOnClickListener{


            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                Log.d("debag", "緯度:" + location?.latitude.toString())
                Log.d("debag", "経度:" + location?.longitude.toString())

//                        カメラ移動
                val osakaStation = LatLng(location!!.latitude, location.longitude)
                gMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(osakaStation, 15.0f))
            }
        }


        //       ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝リサイクラービュー
        val realmResalt = realm.where(RouteDateClass::class.java).findAll()

        val rView = view.findViewById<RecyclerView>(R.id.mapRouteRV)
        adapter = MapFragmentRecycleViewAdapter(requireContext(), object: MapFragmentRecycleViewAdapter.OnItemClickListner{
            override fun onItemClick(item: RouteDateClass) {
                if(item.routeList != null){
                    routeAdd(item.routeList!!,true)
                }
            }
        })

//        横向きにスライドする部分
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rView.layoutManager = linearLayoutManager

//        中心にフォーカスを合わせるやつ
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rView)
        rView.addItemDecoration(object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val edgeMargin = (parent.width - view.layoutParams.width) / 2

                val position = parent.getChildAdapterPosition(view)
                if (position == 0) {
                    outRect.left = edgeMargin
                }
                if (position == state.itemCount - 1) {
                    outRect.right = edgeMargin
                }
            }
        })

        rView?.adapter = adapter

        adapter?.setList(realmResalt)

    }

    fun routeAdd(routeLists: RealmList<RouteListDateClass>,dateClear:Boolean) {
//        保存されているリストが順番道理ではないため、順番に並べ替えられたものを新規作成
        val sortedRouteLists = routeLists.sortedBy { it.indexCount }

        if(dateClear){
            //すでにある円や線を消去
            for (i in circlesList) {
                i.remove()
            }
            circlesList.clear()
            for (i in polyLineList) {
                i.remove()
            }
            polyLineList.clear()
            for (i in markerList) {
                i.remove()
            }
            markerList.clear()

        }


//        円や線の追加 かつ カメラフレームの位置調整
        val latlngLists = mutableListOf<LatLng>()
        for ((index, j) in sortedRouteLists.withIndex()) {

//            円の追加 設定するときに消すことが出来るようにリストに保存をしておく
            val latLng = LatLng(j.placeLat, j.placeLon) // 場所
            latlngLists.add(latLng)

            val radius = 800.0// 400ｍ
            gMap?.addCircle(
                CircleOptions()
                    .center(latLng)          // 円の中心位置
                    .radius(radius)          // 半径 (メートル単位)
                    .strokeColor(Color.BLUE) // 線の色
                    .strokeWidth(2f)         // 線の太さ
                    .fillColor(0x400080ff)   // 円の塗りつぶし色
            )?.let {
                circlesList.add(
                    it
                )
            }


//            線の追加 設定するときに消すことが出来るようにリストに保存をしておく
            if (index < routeLists.size - 1) {
                gMap?.addPolyline(
                    PolylineOptions()
                        .add(
                            LatLng(
                                sortedRouteLists[index].placeLat,
                                sortedRouteLists[index].placeLon
                            )
                        ) // 1つ目
                        .add(
                            LatLng(
                                sortedRouteLists[index + 1].placeLat,
                                sortedRouteLists[index + 1].placeLon
                            )
                        ) // 次のルート
                        .color(Color.BLUE)                   // 線の色
                        .width(8f)                          // 線の太さ
                )?.let {
                    polyLineList.add(
                        it
                    )
                }
            }

        }
        if(dateClear){
            val bounds = LatLngBounds.Builder().also { builder ->
                latlngLists.forEach {
                    gMap?.addMarker(MarkerOptions().position(it))?.let { marker ->
                        markerList.add(
                            marker
                        )
                    }
                    builder.include(it)
                }
            }.build()
            gMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300))
        }

    }


    private fun requestPermission():Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val permissionCheckAccessFineLocation =
                ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)

            val permissionCheckAccessBackgroundLocation =
                ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            if (permissionCheckAccessBackgroundLocation != PackageManager.PERMISSION_GRANTED
                || permissionCheckAccessFineLocation != PackageManager.PERMISSION_GRANTED) {

                // Android 6.0 のみ、該当パーミッションが許可されていない場合
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ),
                        PERMISSION_REQUEST_CODE
                    )
                }
                return false
            } else {
                return true
            }
        }else{
            return true
        }

    }
}