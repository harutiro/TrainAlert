package app.makino.harutiro.trainalert

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class RealmApplication: Application() {
    override fun onCreate(){
        super.onCreate()


        //Realm初期化処理
        Realm.init(this)

        //開発を効率化するための設定
        //データベースに保存するモデルに変更を加えた時アプリを消去して再インストールする手間を省くことができる
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(realmConfig)


    }
}