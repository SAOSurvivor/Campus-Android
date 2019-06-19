package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import android.annotation.SuppressLint
import de.tum.`in`.tumcampusapp.api.app.TumCabeClient
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CafeteriaRemoteRepository @Inject constructor(
    private val tumCabeClient: TumCabeClient,
    private val localRepository: CafeteriaLocalRepository
) {

    /**
     * Downloads cafeterias and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new cafeterias
     * Lastly updates last sync
     *
     */
    @SuppressLint("CheckResult")
    fun fetchCafeterias(force: Boolean) {
        Observable.just(1)
                .filter { localRepository.getLastSync() == null || force }
                .subscribeOn(Schedulers.io())
                .doOnNext { localRepository.clear() }
                .flatMap { tumCabeClient.cafeterias }
                .doAfterNext { localRepository.updateLastSync() }
                .map { it.toTypedArray() }
                .subscribe(localRepository::addCafeteria, Utils::log)
    }

}
