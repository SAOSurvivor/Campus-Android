package de.tum.`in`.tumcampusapp.component.ui.chat

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.ApiHelper
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_chat_member.joinChatQrCode
import kotlinx.android.synthetic.main.activity_add_chat_member.userSearchView
import org.jetbrains.anko.inputMethodManager
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject

sealed class SearchResult {
    data class Success(val members: List<ChatMember>) : SearchResult()
    object Failure : SearchResult()
}

class AddChatMemberActivity : BaseActivity(R.layout.activity_add_chat_member) {

    private val room: ChatRoom by lazy {
        ChatRoom(intent.getStringExtra(Const.CHAT_ROOM_NAME)).apply {
            id = intent.getIntExtra(Const.CURRENT_CHAT_ROOM, -1)
        }
    }

    @Inject
    lateinit var tumCabeClient: TUMCabeClient

    @Inject
    lateinit var database: TcaDb

    private val compositeDisposable = CompositeDisposable()

    private val tumIdPattern = Pattern.compile(Const.TUM_ID_PATTERN)
    private var suggestions = mutableListOf<ChatMember>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userSearchView.threshold = THRESHOLD
        userSearchView.setAdapter(MemberSuggestionsListAdapter(this, suggestions))

        userSearchView.setOnItemClickListener { adapterView, _, position, _ ->
            val member = adapterView.getItemAtPosition(position) as ChatMember
            showConfirmDialog(member)
        }

        compositeDisposable += userSearchView.editorActions()
            .filter { it == EditorInfo.IME_ACTION_SEARCH }
            .map { userSearchView.text.toString() }
            .flatMap { tumCabeClient.searchChatMember(it) }
            .doOnNext { inputMethodManager.hideSoftInputFromWindow(userSearchView.windowToken, 0) }
            .subscribeOn(Schedulers.io())
            .map { SearchResult.Success(it) as SearchResult }
            .onErrorReturnItem(SearchResult.Failure)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it) {
                    is SearchResult.Success -> onSuccess(it.members)
                    is SearchResult.Failure -> onError()
                }
            }

        compositeDisposable += userSearchView.textChanges()
            .map { it.toString() }
            .filter { it.length >= THRESHOLD }
            .flatMap { loadForNameOrTumId(it) }
            .subscribeOn(Schedulers.io())
            .map { SearchResult.Success(it) as SearchResult }
            .onErrorReturnItem(SearchResult.Failure)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it) {
                    is SearchResult.Success -> onSuccess(it.members)
                    is SearchResult.Failure -> onError()
                }
            }

        joinChatQrCode.setImageBitmap(ApiHelper.createQRCode(room.getCombinedName()))
    }

    private fun loadForNameOrTumId(query: String): Observable<List<ChatMember>> {
        val isTumId = tumIdPattern.matcher(query).matches()
        val containsDigit = query.any { it.isDigit() }

        return when {
            isTumId -> tumCabeClient.getChatMemberByLrzId(query).map { listOf(it) }
            containsDigit -> {
                // We don't autocomplete TUM IDs
                if (query.length > 7) {
                    userSearchView.error = getString(R.string.error_invalid_tum_id_format)
                } else {
                    userSearchView.error = null
                }
                Observable.never()
            }
            else -> tumCabeClient.searchChatMember(query).debounce(1, TimeUnit.SECONDS)
        }
    }

    private fun onSuccess(chatMembers: List<ChatMember>) {
        userSearchView.error = null
        suggestions = chatMembers.toMutableList()

        val adapter = userSearchView.adapter as MemberSuggestionsListAdapter
        adapter.updateSuggestions(suggestions)
    }

    private fun onError() {
        userSearchView.error = getString(R.string.error_user_not_found)
    }

    private fun showConfirmDialog(member: ChatMember) {
        val message = getString(R.string.add_user_to_chat_message, member.displayName, room.title)

        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(R.string.add) { _, _ ->
                joinRoom(member)
                reset()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }
            .show()
    }

    /**
     * Clears everything from the last search.
     */
    private fun reset() {
        suggestions = ArrayList()
        val adapter = userSearchView.adapter as MemberSuggestionsListAdapter
        adapter.updateSuggestions(suggestions)
        userSearchView.setText("")
    }

    private fun joinRoom(member: ChatMember) {
        val verification = TUMCabeVerification.create(this, null)
        if (verification == null) {
            Utils.showToast(this, R.string.error)
            return
        }

        compositeDisposable += tumCabeClient.addUserToChat(room, member, verification)
            .subscribe(this::onChatMemberAddedSuccess) {
                Utils.showToast(this, R.string.error_something_wrong)
            }
    }

    private fun onChatMemberAddedSuccess(chatRoom: ChatRoom) {
        database.chatRoomDao().updateMemberCount(chatRoom.members, chatRoom.id)
        Utils.showToast(this, R.string.chat_member_added)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private companion object {
        private const val THRESHOLD = 3 // min number of characters before getting suggestions
    }

}
