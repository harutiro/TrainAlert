package app.makino.harutiro.trainalert.ui.route

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.trainalert.EditActivity
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.adapter.RouteRecycleViewAdapter
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm

class RouteFragment : Fragment(R.layout.fragment_route) {

    var adapter: RouteRecycleViewAdapter? = null

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    override fun onResume() {
        super.onResume()

        adapter?.setList(realm.where(RouteDateClass::class.java).findAll())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.findViewById<Switch>(R.id.switch2).thumbDrawable = ResourcesCompat.getDrawable(resources, R.drawable.true_bell, null)
        view.findViewById<FloatingActionButton>(R.id.fragmentRouteAddFAB).setOnClickListener {
            startActivity(Intent(context, EditActivity::class.java))
        }

        //       ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝リサイクラービュー
        val rView = view.findViewById<RecyclerView>(R.id.routeRV)
        adapter = RouteRecycleViewAdapter(requireContext(), object: RouteRecycleViewAdapter.OnItemClickListner{
            override fun onItemClick(item: RouteDateClass) {
                // SecondActivityに遷移するためのIntent
                val intent = Intent(context, EditActivity::class.java)
                // RecyclerViewの要素をタップするとintentによりSecondActivityに遷移する
                // また，要素のidをSecondActivityに渡す
                intent.putExtra("id", item.id)
                startActivity(intent)
            }

            override fun onReView(moji: String) {
                Snackbar.make(requireActivity().findViewById(android.R.id.content),moji, Snackbar.LENGTH_SHORT).show()
                adapter?.setList(realm.where(RouteDateClass::class.java).findAll())
            }
        })
        rView.layoutManager = LinearLayoutManager(context)
        rView.adapter = adapter


        val realmResalt = realm.where(RouteDateClass::class.java).findAll()
        for(i in realmResalt){
            Log.d("debag3",i.toString())
            if(i.routeList !=null ){
                for(j in i.routeList!!){
                    Log.d("debag3",j.placeName)
                    Log.d("debag3",j.indexCount.toString())


                }
            }


        }
        adapter?.setList(realmResalt)

    }

}