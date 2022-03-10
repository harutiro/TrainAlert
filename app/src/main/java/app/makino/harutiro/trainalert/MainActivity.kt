package app.makino.harutiro.trainalert

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.AppLaunchChecker
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.makino.harutiro.trainalert.databinding.ActivityMainBinding
import app.makino.harutiro.trainalert.service.LocationService
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1234
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map, R.id.navigation_route, R.id.navigation_setting
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


//        ForgraundService

        if(AppLaunchChecker.hasStartedFromLauncher(this)){
            Log.d("AppLaunchChecker","2回目以降");
        } else {
            AppLaunchChecker.onActivityCreate(this);
            AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("位置情報の取り扱い")
                .setMessage("このアプリでは位置情報によるアラート機能を可能にするために、現在地のデータが収集されます。\n" +
                        "アプリを閉じている時や、使用していないときにも収集されます。\n" +
                        "位置情報は、個人を特定できない統計的な情報として、\n" +
                        "お知らせの配信、位置情報の利用を許可しない場合は、\n" +
                        "この後表示されるダイアログで「許可しない」を選択してください。")
                .setPositiveButton("OK") { dialog, which ->
                    requestPermission()
                }
                .show()
            createNotificationChannel()
        }




        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }else{
            startService(intent)
        }

        Log.d("debag10","onCreateがきた")
    }

    private fun requestPermission() {
        val permissionAccessCoarseLocationApproved =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        if (permissionAccessCoarseLocationApproved) {
            val backgroundLocationPermissionApproved = ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

            if (backgroundLocationPermissionApproved) {
                // フォアグラウンドとバックグランドのバーミッションがある
            } else {
                // フォアグラウンドのみOKなので、バックグラウンドの許可を求める
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // 位置情報の権限が無いため、許可を求める
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            val channel = NotificationChannel(
                LocationService.CHANNEL_ID,
                "お知らせ",
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "お知らせを通知します。"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.lastIndex <= 0) {
            return
        }
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                run {
                    if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                        /// 許可が取れた場合・・・
                        /// 必要な処理を書いておく
                        val intent = Intent(this,OpenLocationServiceActivity::class.java)
                        startActivity(intent)
                    } else {
                        /// 許可が取れなかった場合・・・
                        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                            .setTitle("位置情報の許可がされませんでした。")
                            .setMessage("今後、位置情報の許可をする場合、アプリを入れ直すか、本体設定からパーミッションの設定に入り、位置情報の許可をお願いします。")
                            .setPositiveButton("OK") { dialog, which ->
                            }
                            .show()
                    }
                }
            }
        }
    }
}