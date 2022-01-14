package app.makino.harutiro.trainalert.dateBase

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class RouteDateClass(
    @PrimaryKey open var id: String? = UUID.randomUUID().toString(),
    open var iconImage: String = "",
    open var routeName: String = "",
    open var alertCheck: Boolean = false,
    open var timeAllDayCheck: Boolean = false,
    open var timeDeparture:String = "",
    open var timeArriva:String = "",
    open var weekEveryDay:Boolean = false,
    open var weekMon:Boolean = false,
    open var weekTue:Boolean = false,
    open var weekWed:Boolean = false,
    open var weekThe:Boolean = false,
    open var weekFri:Boolean = false,
    open var weekSat:Boolean = false,
    open var weekSun:Boolean = false,
    open var routeList:RealmList<RouteListDateClass>? = null,
): RealmObject()