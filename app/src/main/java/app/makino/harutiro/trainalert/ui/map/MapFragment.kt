package app.makino.harutiro.trainalert.ui.map

import android.Manifest
import android.Manifest.permission_group.LOCATION
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.trainalert.MainActivity
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.adapter.MapFragmentRecycleViewAdapter
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import io.realm.Realm


class MapFragment : Fragment() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1234
    }


    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    var gMap: GoogleMap? = null
    var adapter: MapFragmentRecycleViewAdapter? = null


    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        gMap = googleMap

        val realmResalt = realm.where(RouteDateClass::class.java).findAll()

        //        ツールバーの表示
        googleMap.uiSettings.isMapToolbarEnabled = true

        if(requestPermission()){
            googleMap.isMyLocationEnabled = true

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
            val sortByRouteList = i.routeList?.sortedBy { it.indexCount }

            for ((index,j) in sortByRouteList?.withIndex()!!){
                Log.d("debag9",j.placeLovalLanguageName)
                Log.d("debag9",j.indexCount.toString())

                val latLng = LatLng(j.placeLat, j.placeLon) // 東京駅
                val radius = 800.0// 10km
                googleMap.addCircle(
                    CircleOptions()
                        .center(latLng)          // 円の中心位置
                        .radius(radius)          // 半径 (メートル単位)
                        .strokeColor(Color.BLUE) // 線の色
                        .strokeWidth(2f)         // 線の太さ
                        .fillColor(0x400080ff)   // 円の塗りつぶし色
                )

                if (index < i.routeList!!.size - 1){
                    googleMap.addPolyline(
                        PolylineOptions()
                            .add(LatLng(sortByRouteList[index]?.placeLat ?: 0.0,sortByRouteList[index]?.placeLon ?: 0.0 )) // 東京駅
                            .add(LatLng(sortByRouteList[index + 1]?.placeLat ?: 0.0,sortByRouteList[index +1]?.placeLon ?: 0.0 )) // 東京駅
                            .color(Color.BLUE)                   // 線の色
                            .width(8f)                          // 線の太さ
                    )
                }

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


        //       ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝リサイクラービュー
        val realmResalt = realm.where(RouteDateClass::class.java).findAll()

        val rView = view?.findViewById<RecyclerView>(R.id.mapRouteRV)
        adapter = MapFragmentRecycleViewAdapter(requireContext(), object: MapFragmentRecycleViewAdapter.OnItemClickListner{
            override fun onItemClick(item: RouteDateClass) {
                //        通知の範囲をお知らせ
                val sortByRouteList = item.routeList?.sortedBy { it.indexCount }

                for ((index,j) in sortByRouteList?.withIndex()!!){
                    Log.d("debag9",j.placeLovalLanguageName)
                    Log.d("debag9",j.indexCount.toString())

                    val latLng = LatLng(j.placeLat, j.placeLon) // 東京駅
                    val radius = 800.0// 10km

                    gMap?.addCircle(
                        CircleOptions()
                            .center(latLng)          // 円の中心位置
                            .radius(radius)          // 半径 (メートル単位)
                            .strokeColor(Color.BLUE) // 線の色
                            .strokeWidth(2f)         // 線の太さ
                            .fillColor(0x400080ff)   // 円の塗りつぶし色
                    )

                    if (index < item.routeList!!.size - 1){
                        gMap?.addPolyline(
                            PolylineOptions()
                                .add(LatLng(sortByRouteList[index]?.placeLat ?: 0.0,sortByRouteList[index]?.placeLon ?: 0.0 )) // 東京駅
                                .add(LatLng(sortByRouteList[index + 1]?.placeLat ?: 0.0,sortByRouteList[index +1]?.placeLon ?: 0.0 )) // 東京駅
                                .color(Color.BLUE)                   // 線の色
                                .width(8f)                          // 線の太さ
                        )
                    }

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