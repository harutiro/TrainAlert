package app.makino.harutiro.trainalert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.makino.harutiro.trainalert.service.LocationService

class StopLocationServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_location_service)

        val targetIntent = Intent(this, LocationService::class.java)
        this.stopService(targetIntent)
        finish()
    }
}