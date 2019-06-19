package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.api.app.TumCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TicketsRemoteRepository @Inject constructor(
    private val tumCabeClient: TumCabeClient,
    private val ticketsLocalRepository: TicketsLocalRepository
) {

    fun fetchTickets(): Observable<List<Ticket>> {
        return tumCabeClient
                .fetchTickets()
                .doOnError { Utils.log(it) }
                .subscribeOn(Schedulers.io())
    }

    fun fetchTicketTypesForTickets(tickets: List<Ticket>): Completable {
        val sources = tickets.map { fetchTicketTypesForEvent(it.eventId) }
        return Observable
                .merge(sources)
                .ignoreElements()
    }

    fun fetchTicketTypesForEvent(eventId: Int): Observable<List<TicketType>> {
        return tumCabeClient.fetchTicketTypes(eventId)
                .doOnNext(ticketsLocalRepository::addTicketTypes)
    }

}
