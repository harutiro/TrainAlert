package app.makino.harutiro.trainalert

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import app.makino.harutiro.trainalert.service.LocationService

class OpenLocationServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.open_location_service_activity)

        val targetIntent = Intent(this, LocationService::class.java)
        this.stopService(targetIntent)

        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }else{
            startService(intent)
        }
        Toast.makeText(this, "位置情報を取得します", Toast.LENGTH_SHORT).show()
        finish()
    }
}