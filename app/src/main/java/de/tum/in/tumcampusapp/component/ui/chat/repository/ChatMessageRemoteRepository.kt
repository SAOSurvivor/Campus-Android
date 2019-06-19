package de.tum.`in`.tumcampusapp.component.ui.chat.repository

import de.tum.`in`.tumcampusapp.api.app.TumCabeClient
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import io.reactivex.Observable

object ChatMessageRemoteRepository {

    lateinit var tumCabeClient: TumCabeClient

    fun getMessages(
        roomId: Int,
        messageId: Long
    ): Observable<List<ChatMessage>> = tumCabeClient.getMessages(roomId, messageId)

    fun getNewMessages(
        roomId: Int
    ): Observable<List<ChatMessage>> = tumCabeClient.getNewMessages(roomId)

    fun sendMessage(
        roomId: Int,
        chatMessage: ChatMessage
    ): Observable<ChatMessage> = tumCabeClient.sendMessage(roomId, chatMessage)

}
