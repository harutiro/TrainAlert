package app.makino.harutiro.trainalert.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.dateBase.RouteListDateClass
import io.realm.Realm

class EditRecycleViewAdapter(private val context: Context,private val listener: OnItemClickListner):
    RecyclerView.Adapter<EditRecycleViewAdapter.ViewHolder>() {

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    //リサイクラービューに表示するリストを宣言する
    val items: MutableList<RouteListDateClass> = mutableListOf()

    //データをcourseDateと結びつける？？
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemEditRouteEditText: EditText = view.findViewById(R.id.itemEditRouteEditText)
        val itemEditAlarmTimeEditText:EditText = view.findViewById(R.id.itemEditAlarmTimeEditText)
        val itemEditAddButton: Button = view.findViewById(R.id.itemEditAddButton)
        val itemEditTopLineView:View = view.findViewById(R.id.itemEditTopLineView)
        val itemEditDiamondView:View = view.findViewById(R.id.itemEditDiamondView)
        val itemEditButtomLineView:View = view.findViewById(R.id.itemEditButtomLineView)

    }

    //はめ込むものを指定
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_course_edit_date,parent,false)
        return ViewHolder(view)
    }

    //itemsのposition番目の要素をviewに表示するコード
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val person = realm.where(RouteListDateClass::class.java).equalTo("id",item.id).findFirst()

//        // MainActivity側でタップしたときの動作を記述するため，n番目の要素を渡す
//        holder.container.setOnClickListener { listener.onItemClick(item) }

//        itemとレイアウトの直接の結びつけ
        holder.itemEditRouteEditText.setText(item.placeName)
        holder.itemEditAlarmTimeEditText.setText(item.alertTime)

        if (item.start){
            holder.itemEditTopLineView.visibility = INVISIBLE
        }
        if (item.end){
            holder.itemEditButtomLineView.visibility = INVISIBLE
        }
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

    fun setList(list: List<RouteListDateClass>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}