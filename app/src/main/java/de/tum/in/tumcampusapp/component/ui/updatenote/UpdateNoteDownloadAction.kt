package de.tum.`in`.tumcampusapp.component.ui.updatenote

import android.content.Context
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.api.app.TumCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import javax.inject.Inject

class UpdateNoteDownloadAction @Inject constructor(
        val context: Context,
        val tumCabeClient: TumCabeClient
): DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        val savedNote = Utils.getSetting(context, Const.UPDATE_MESSAGE, "")
        if (savedNote.isNotEmpty()) {
            // note has already been downloaded
            return
        }

        val note = tumCabeClient.getUpdateNote(BuildConfig.VERSION_CODE)
        Utils.setSetting(context, Const.UPDATE_MESSAGE, note?.updateNote.orEmpty())
    }

}
