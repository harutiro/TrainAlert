package app.makino.harutiro.trainalert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import app.makino.harutiro.trainalert.service.LocationService

class StopLocationServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_location_service)

        val targetIntent = Intent(this, LocationService::class.java)
        this.stopService(targetIntent)
        Toast.makeText(this, "位置情報の取得を停止します", Toast.LENGTH_SHORT).show()
        finish()
    }
}