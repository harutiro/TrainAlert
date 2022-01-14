package app.makino.harutiro.trainalert.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import app.makino.harutiro.trainalert.ui.route.RouteFragment
import io.realm.Realm

class RouteRecycleViewAdapter(private val context: Context, private val listener: OnItemClickListner):
    RecyclerView.Adapter<RouteRecycleViewAdapter.ViewHolder>() {

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    //リサイクラービューに表示するリストを宣言する
    val items: MutableList<RouteDateClass> = mutableListOf()

    //データをcourseDateと結びつける？？
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemRouteIconImageView : ImageView = view.findViewById(R.id.itemRouteIconImageView)
        val itemRouteNameTextView: TextView = view.findViewById(R.id.itemRouteNameTextView)
        val itemTouteAlertSwitch: Switch = view.findViewById(R.id.itemRouteAlertSwitch)
        val itemRouteRemoveButton: ImageButton = view.findViewById(R.id.itemRouteRemoveButton)
        
    }

    //はめ込むものを指定
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_course_route_date,parent,false)
        return ViewHolder(view)
    }

    //itemsのposition番目の要素をviewに表示するコード
    override fun onBindViewHolder(holder: RouteRecycleViewAdapter.ViewHolder, position: Int) {
        val item = items[position]
        val person = realm.where(RouteDateClass::class.java).equalTo("id",item.id).findFirst()

//        // MainActivity側でタップしたときの動作を記述するため，n番目の要素を渡す
//        holder.container.setOnClickListener { listener.onItemClick(item) }

//        itemとレイアウトの直接の結びつけ
        holder.itemRouteNameTextView.text = person?.routeName

//
    }

    //リストの要素数を返すメソッド
    override fun getItemCount(): Int {
        return items.size
    }

    // RecyclerViewの要素をタップするためのもの
    interface OnItemClickListner{
//        fun onItemClick(item: RouteListDateClass)
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