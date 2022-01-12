package app.lawnchair.icons

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.core.content.getSystemService
import com.android.launcher3.R
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SystemIconPack(context: Context) : IconPack(context, "") {

    override val label = context.getString(R.string.system_icons)
    private val appMap = run {
        val profiles = UserCache.INSTANCE.get(context).userProfiles
        val launcherApps = context.getSystemService<LauncherApps>()!!
        profiles
            .flatMap { launcherApps.getActivityList(null, Process.myUserHandle()) }
            .associateBy { ComponentKey(it.componentName, it.user) }
    }

    override fun getIcon(componentName: ComponentName) =
        IconEntry(packPackageName, ComponentKey(componentName, Process.myUserHandle()).toString())
    override fun getCalendar(componentName: ComponentName): CalendarIconEntry? = null
    override fun getClock(entry: IconEntry): ClockMetadata? = null

    override fun getCalendars(): MutableSet<ComponentName> = mutableSetOf()
    override fun getClocks(): MutableSet<ComponentName> = mutableSetOf()

    override fun getIcon(iconEntry: IconEntry, iconDpi: Int): Drawable? {
        val key = ComponentKey.fromString(iconEntry.name)
        val app = appMap[key] ?: return null
        return app.getIcon(iconDpi)
    }

    override fun loadInternal() {

    }

    override fun getAllIcons(): Flow<List<IconPickerCategory>> = flow {
        val items = appMap
            .map { (key, info) ->
                IconPickerItem(
                    packPackageName = packPackageName,
                    drawableName = key.toString(),
                    label = info.label.toString()
                )
            }
            .sortedBy { it.label.lowercase() }
        emit(listOf(IconPickerCategory(
            title = context.getString(R.string.icon_picker_default_category),
            items = items
        )))
    }.flowOn(Dispatchers.IO)
}
