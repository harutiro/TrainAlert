package app.makino.harutiro.trainalert.service

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import app.makino.harutiro.trainalert.MainActivity
import app.makino.harutiro.trainalert.R
import app.makino.harutiro.trainalert.dateBase.RouteDateClass
import com.google.android.gms.location.*
import io.realm.Realm
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


class LocationService : Service() {
    companion object {
        const val CHANNEL_ID = "777"
        const val MONITOR_HEADSET_SERVICE_ID = 72
        const val MONITOR_HEADSET_NOTIFY_ID = 69
    }

    private val realm by lazy {
        Realm.getDefaultInstance()
    }


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    //　　　　イヤホン取得部分
    private var currentBluetoothHeadset: BluetoothHeadset? = null
    private var isBluetoothHeadsetConnected = false
    private var isEarphoneConnected = false

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    @SuppressLint("NewApi", "LaunchActivityFromNotification")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        =====================イヤホン装着状態の確認
        registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
        registerReceiver(broadcastReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        registerReceiver(broadcastReceiver, monitorHeadsetFilter)
        bluetoothAdapter.getProfileProxy(this, bluetoothPolicyListener, BluetoothProfile.HEADSET)


//        ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝通知部分
//        マネージャーのインスタンス化
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // カテゴリー名（通知設定画面に表示される情報）
        val nonEarName = "イヤホンがないときのアラーム"
        // システムに登録するChannelのID
        val nonEarId = "TrainNonEarAlertChannel"
        // 通知の詳細情報（通知設定画面に表示される情報）
        val nonEarNotifyDescription = "アラームの詳しい設定を行います"

        // Channelの取得と生成
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&notificationManager.getNotificationChannel(nonEarId) == null) {
//            チャンネルの重要度の設定
            val mChannel = NotificationChannel(nonEarId, nonEarName, NotificationManager.IMPORTANCE_HIGH)
//            通知音をなくす
            mChannel.setSound(null, null)
//            通知チャンネルの詳細表示
            mChannel.description = nonEarNotifyDescription
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
        val test_intent1 = Intent(this,StopAlertRecever::class.java) //空のインテントを準備
        test_intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val test_pendingIntent1 = PendingIntent.getBroadcast(
            baseContext,
            0,
            test_intent1,
            PendingIntent.FLAG_MUTABLE
        ) //インテントをペンディングインテントに組み込む

//        通知の作成
        val notification3 = NotificationCompat.Builder(this, nonEarId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("駅にもうじき到着します")
            .setContentText("通知をタップして止めましょう！！")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // ② 通知の重要度
            .setCategory(NotificationCompat.CATEGORY_ALARM) // ③ 通知のカテゴリ
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(test_pendingIntent1)
            .setVibrate(longArrayOf(0,1000,0,1000))
            .build()
        notification3.flags = Notification.FLAG_ONLY_ALERT_ONCE or Notification.FLAG_NO_CLEAR or Notification.FLAG_INSISTENT




//       ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝距離計算
        val realmResalt = realm.where(RouteDateClass::class.java).findAll()

        var routeDateUUID = ""
        var routeListDateUUID = ""

        var updatedCount = 0
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    updatedCount++
                    Log.d("debag2", "[${updatedCount}] ${location.latitude} , ${location.longitude}")

                    for(i in realmResalt){
                        for( j in i.routeList!!){
                            val distance = j?.let {
                                getDistance(location.latitude,location.longitude,
                                    it.placeLat,j.placeLon,'k')
                            }

                            if (j != null) {
                                Log.d("debag3","${i.routeName},${j.placeLovalLanguageName}")
                                Log.d("debag3","$distance")
                            }

                            if (distance!! <= 0.600 && routeDateUUID.isEmpty() && routeListDateUUID.isEmpty()) {

                                if((isBluetoothHeadsetConnected || isEarphoneConnected) ){
//                                    notificationManager.notify(99, notification2)
                                }else{
                                    notificationManager.notify(102, notification3)
                                }

                                routeDateUUID = i.id.toString()
                                routeListDateUUID = j.id.toString()

                            }

                            if (distance > 0.600 && routeDateUUID == i.id && routeListDateUUID == j.id) {
                                routeDateUUID = ""
                                routeListDateUUID = ""
                            }

                            Log.d("debag8",i.id.toString() )
                            Log.d("debag8",routeDateUUID )
                            Log.d("debag8",j.id.toString() )
                            Log.d("debag8",routeListDateUUID )
                        }
                    }
                }
            }
        }

        val openIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
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
        unregisterReceiver(broadcastReceiver)

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

    //    ＝＝＝＝＝＝＝＝＝＝＝イヤホン取得部分
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            when (action) {
                Intent.ACTION_HEADSET_PLUG -> {
                    Log.d("debag", "Intent.ACTION_HEADSET_PLUG")
                    val state = intent.getIntExtra("state", -1)
                    if (state == 0) {
                        // ヘッドセットが装着されていない・外された
                        Log.d("debag", "😊")
                    } else if (state > 0) {
                        // イヤホン・ヘッドセット(マイク付き)が装着された
                        Log.d("debag", "❤️")
                    }

                    isEarphoneConnected = state > 0
                }
                BluetoothDevice.ACTION_ACL_CONNECTED    -> {
                    Thread.sleep(2000)

                    Log.d("debag", "Broadcast: ACTION_ACL_CONNECTED")
                    if (currentBluetoothHeadset?.connectedDevices?.size ?: 0 > 0) {
                        isBluetoothHeadsetConnected = true
                        Log.d("debag", "★")

                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.d("debag", "Broadcast: ACTION_ACL_DISCONNECTED")
                    isBluetoothHeadsetConnected = false
                    Log.d("debag", "😒")
                }
                else -> {}
            }
        }
    }

    private val bluetoothPolicyListener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d("debag", "BluetoothProfile onServiceConnected")

                currentBluetoothHeadset = proxy as BluetoothHeadset
                isBluetoothHeadsetConnected = (currentBluetoothHeadset?.connectedDevices?.size ?: 0 > 0)
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d("debag", "BluetoothProfile onServiceDisconnected")

                currentBluetoothHeadset = null
            }
        }
    }


    private val monitorHeadsetFilter = IntentFilter().apply {
        addAction(AudioManager.ACTION_HEADSET_PLUG)
        addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    }

}