package app.makino.harutiro.trainalert.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class StopAlertRecever: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
        Toast.makeText(context, "アラームを止めました", Toast.LENGTH_SHORT).show()
    }
}