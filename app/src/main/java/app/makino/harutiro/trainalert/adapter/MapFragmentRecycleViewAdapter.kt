package app.makino.harutiro.trainalert.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import app.makino.harutiro.trainalert.dateBase.RouteListDateClass
import io.realm.Realm

class MapFragmentRecycleViewAdapter (private val context: Context, private val listener: MapFragmentRecycleViewAdapter.OnItemClickListner):
    RecyclerView.Adapter<MapFragmentRecycleViewAdapter.ViewHolder>() {

        private val realm by lazy {
            Realm.getDefaultInstance()
        }

        //リサイクラービューに表示するリストを宣言する
        val items: MutableList<RouteDateClass> = mutableListOf()

        //データをcourseDateと結びつける？？
        class ViewHolder(view: View): RecyclerView.ViewHolder(view){
            val itemMapRouteIcon: ImageView = view.findViewById(R.id.itemMapRouteIcon)
            val itemMapRouteName: TextView = view.findViewById(R.id.itemMapRouteName)
            val itemMapContainer: ConstraintLayout = view.findViewById(R.id.itemMapContainer)
        }

        //はめ込むものを指定
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_course_map_date,parent,false)
            return ViewHolder(view)
        }

        //itemsのposition番目の要素をviewに表示するコード
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]

//        // MainActivity側でタップしたときの動作を記述するため，n番目の要素を渡す
        holder.itemMapContainer.setOnClickListener { listener.onItemClick(item) }

//        itemとレイアウトの直接の結びつけ
            holder.itemMapRouteName.text = item.routeName
        }

        //リストの要素数を返すメソッド
        override fun getItemCount(): Int {
            return items.size
        }

        // RecyclerViewの要素をタップするためのもの
        interface OnItemClickListner{
            fun onItemClick(item: RouteDateClass)
        }

        fun reView(){
            notifyDataSetChanged()
        }

        fun setList(list: List<RouteDateClass>){
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        fun getList(): List<RouteDateClass>{
            return items
        }
}