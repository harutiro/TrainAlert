package app.makino.harutiro.trainalert.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import app.makino.harutiro.trainalert.EditActivity
import app.makino.harutiro.trainalert.MainActivity2
import app.makino.harutiro.trainalert.R

class SettingFragment : Fragment(R.layout.fragment_setting) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.gotoButton).setOnClickListener{
            startActivity(Intent(context, MainActivity2::class.java))
        }
    }
}