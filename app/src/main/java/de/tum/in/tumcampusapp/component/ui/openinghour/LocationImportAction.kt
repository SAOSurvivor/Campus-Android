package de.tum.`in`.tumcampusapp.component.ui.openinghour

import android.content.Context
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TumCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import javax.inject.Inject

/**
 * Import default location and opening hours
 */
class LocationImportAction @Inject constructor(
        private val context: Context,
        private val database: TcaDb,
        private val tumCabeClient: TumCabeClient
): DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        val openingHours = tumCabeClient.fetchOpeningHours(context.getString(R.string.language))
        if (openingHours != null) {
            database.locationDao().removeCache()
            database.locationDao().replaceInto(openingHours)
        }
    }

}
