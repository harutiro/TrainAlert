package app.makino.harutiro.trainalert.ui.route

import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import app.makino.harutiro.trainalert.R

class RouteFragment : Fragment(R.layout.fragment_route) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Switch>(R.id.switch2).thumbDrawable = ResourcesCompat.getDrawable(resources, R.drawable.true_bell, null)
    }
}