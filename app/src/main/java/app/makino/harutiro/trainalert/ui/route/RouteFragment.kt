package app.makino.harutiro.trainalert.ui.route

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import app.makino.harutiro.trainalert.EditActivity
import app.makino.harutiro.trainalert.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RouteFragment : Fragment(R.layout.fragment_route) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.findViewById<Switch>(R.id.switch2).thumbDrawable = ResourcesCompat.getDrawable(resources, R.drawable.true_bell, null)
        view.findViewById<FloatingActionButton>(R.id.fragmentRouteAddFAB).setOnClickListener {
            startActivity(Intent(context, EditActivity::class.java))
        }
    }
}