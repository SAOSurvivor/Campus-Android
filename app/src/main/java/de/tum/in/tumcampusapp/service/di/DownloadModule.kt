package de.tum.`in`.tumcampusapp.service.di

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.IdUploadAction
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.openinghour.LocationImportAction
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.tufilm.FilmDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.updatenote.UpdateNoteDownloadAction
import de.tum.`in`.tumcampusapp.service.DownloadWorker

/**
 * This module provides dependencies that are needed in the download functionality, namely
 * [DownloadWorker]. It mainly includes data repositories and manager classes.
 */
@Module
object DownloadModule {

    @JvmStatic
    @Provides
    fun provideAssetManager(
            context: Context
    ): AssetManager = context.assets

    @JvmStatic
    @Provides
    fun provideWorkerActions(
            cafeteriaDownloadAction: CafeteriaDownloadAction,
            locationImportAction: LocationImportAction,
            eventsDownloadAction: EventsDownloadAction,
            filmDownloadAction: FilmDownloadAction,
            gradesDownloadAction: GradesDownloadAction,
            idUploadAction: IdUploadAction,
            newsDownloadAction: NewsDownloadAction,
            topNewsDownloadAction: TopNewsDownloadAction,
            updateNoteDownloadAction: UpdateNoteDownloadAction
    ): DownloadWorker.WorkerActions = DownloadWorker.WorkerActions(
            cafeteriaDownloadAction,
            locationImportAction,
            eventsDownloadAction,
            filmDownloadAction,
            gradesDownloadAction,
            idUploadAction,
            newsDownloadAction,
            topNewsDownloadAction,
            updateNoteDownloadAction
    )

}
