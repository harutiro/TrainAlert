package app.makino.harutiro.trainalert.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.makino.harutiro.trainalert.MainActivity
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import io.realm.Realm
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import android.app.NotificationManager
import android.media.AudioAttributes
import app.makino.harutiro.trainalert.EditActivity
import okhttp3.internal.notify
import android.content.BroadcastReceiver
import android.os.*
import android.app.PendingIntent
import android.widget.Toast
import kotlinx.coroutines.channels.BroadcastChannel


class LocationService : Service() {
    companion object {
        const val CHANNEL_ID = "777"
    }

    private val realm by lazy {
        Realm.getDefaultInstance()
    }


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    val vibrator: Vibrator? = null


    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝通知部分
//        マネージャーのインスタンス化
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // カテゴリー名（通知設定画面に表示される情報）
        val name = "通常アラーム"
        // システムに登録するChannelのID
        val id = "TrainDefaultAlertChannel"
        // 通知の詳細情報（通知設定画面に表示される情報）
        val notifyDescription = "アラームの詳しい設定を行います"

        // Channelの取得と生成
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&notificationManager.getNotificationChannel(id) == null) {
//            デフォルト音声についての設定
            val attributes = AudioAttributes.Builder().apply {
                setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            }.build()
//            チャンネルの重要度の設定
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
//            着信音の設定
            mChannel.setSound(Uri.parse("android.resource://$packageName/${R.raw.alert}"), attributes)
//            通知チャンネルの詳細表示
            mChannel.description = notifyDescription
//            バイブの許可
            mChannel.enableVibration(true)
//            ？？？
            mChannel.canShowBadge();
//            LEDの許可
            mChannel.enableLights(true);
//            ？？？
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE;
//            ？？？
            mChannel.setShowBadge(true);
//            チャンネルの追加
            notificationManager.createNotificationChannel(mChannel)
        }

        //通知にタップで反応するレシーバーを作成
        val test_intent = Intent(this,StopAlertRecever::class.java) //空のインテントを準備
        val test_pendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            test_intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        ) //インテントをペンディングインテントに組み込む

        //通常のタップでMainに飛ぶ通知
        val intent = Intent(this, MainActivity::class.java).apply {}
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

//        通知の作成
        val notification2 = NotificationCompat.Builder(this, "casareal_chanel1")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("title")
            .setContentText("message")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // ② 通知の重要度
            .setCategory(NotificationCompat.CATEGORY_ALARM) // ③ 通知のカテゴリ
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(pendingIntent, true)
            .setVibrate(longArrayOf(0,1000,0,1000))
            .addAction(  // 4. 「応答」ボタンを追加
                R.drawable.false_bell,
                "止める",
                test_pendingIntent
            )
            .build()
        notification2.flags = Notification.FLAG_ONLY_ALERT_ONCE or Notification.FLAG_NO_CLEAR or Notification.FLAG_INSISTENT



//       ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝距離計算
        val realmResalt = realm.where(RouteDateClass::class.java).findAll()

        var updatedCount = 0
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    updatedCount++
                    Log.d("debag2", "[${updatedCount}] ${location.latitude} , ${location.longitude}")

                    for(i in realmResalt){
                        for(j in i.routeList!!){
                            val distance = getDistance(location.latitude,location.longitude,j.placeLat,j.placeLon,'k')

                            Log.d("debag3","${i.routeName},${j.placeLovalLanguageName}")
                            Log.d("debag3","$distance")

                            if(distance <= 0.200){
                                notificationManager.notify(99, notification2)
                            }

                        }
                    }
                }
            }
        }

        val openIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("位置情報テスト")
            .setContentText("位置情報を取得しています...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openIntent)
            .build()

        startForeground(9999, notification)

        startLocationUpdates()

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = createLocationRequest() ?: return
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    /*
     * 2点間の距離を取得
     * 第五引数に設定するキー（unit）で単位別で取得できる
     */
    private fun getDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        unit: Char
    ): Double {
        val theta = lon1 - lon2
        var dist =
            sin(deg2rad(lat1)) * sin(deg2rad(lat2)) + cos(deg2rad(lat1)) * cos(
                deg2rad(lat2)
            ) * cos(deg2rad(theta))
        dist = acos(dist)
        dist = rad2deg(dist)
        val miles = dist * 60 * 1.1515
        return when (unit) {
            'K' -> miles * 1.609344
            'N' -> miles * 0.8684
            'M' -> miles
            else -> miles
        }
    }

    private fun rad2deg(radian: Double): Double {
        return radian * (180f / Math.PI)
    }

    fun deg2rad(degrees: Double): Double {
        return degrees * (Math.PI / 180f)
    }
}