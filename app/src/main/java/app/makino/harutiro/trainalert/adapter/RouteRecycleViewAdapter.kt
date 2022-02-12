package app.makino.harutiro.trainalert.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
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
        val itemRouteAlertSwitch: Switch = view.findViewById(R.id.itemRouteAlertSwitch)
        val itemRouteRemoveButton: ImageButton = view.findViewById(R.id.itemRouteRemoveButton)
        val itemROuteConstraint:ConstraintLayout =view.findViewById(R.id.itemRouteConstraint)
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
        holder.itemROuteConstraint.setOnClickListener { listener.onItemClick(item) }

//        itemとレイアウトの直接の結びつけ
        holder.itemRouteNameTextView.text = person?.routeName

//        holder.itemRouteNextStationTextView.text = person?.routeList?.get(person.routeNumber)?.placeLovalLanguageName


        holder.itemRouteAlertSwitch.isChecked = person?.alertCheck!!

        holder.itemRouteRemoveButton.setOnClickListener {
            realm.executeTransaction {
                person?.deleteFromRealm()
            }
            listener.onReView("消去しました")
        }

        holder.itemRouteAlertSwitch.setOnCheckedChangeListener{ _, isChecked ->

            realm.executeTransaction {
                person?.alertCheck = isChecked
            }
//            TODO:アニメーションが消えるもんだい
//            listener.onReView("アラートを切り替えました。")
        }

//
    }

    //リストの要素数を返すメソッド
    override fun getItemCount(): Int {
        return items.size
    }

    // RecyclerViewの要素をタップするためのもの
    interface OnItemClickListner{
        fun onItemClick(item: RouteDateClass)
        fun onReView(moji: String)
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