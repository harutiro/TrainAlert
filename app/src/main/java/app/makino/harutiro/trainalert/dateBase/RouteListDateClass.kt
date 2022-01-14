package app.makino.harutiro.trainalert.dateBase

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class RouteListDateClass(
    @PrimaryKey open var id: String? = UUID.randomUUID().toString(),
    open var placeName: String = "",
    open var placeId: String = "",
    open var placeType:String = "",
    open var placeMyAddress:String = "",
    open var placeLat:Double = 0.0,
    open var placeLon:Double = 0.0,
    open var start:Boolean = false,
    open var end:Boolean = false,
    open var alertTime:String = ""

): RealmObject()