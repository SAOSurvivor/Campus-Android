package de.tum.`in`.tumcampusapp.component.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember

class MemberSuggestionsListAdapter(
    private val context: Context,
    private val members: MutableList<ChatMember>
) : BaseAdapter(), Filterable {

    private val originalData = members

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)

        val title = view.findViewById<View>(android.R.id.text1) as TextView
        title.text = members[position].displayName

        val subtitle = view.findViewById<View>(android.R.id.text2) as TextView
        subtitle.text = members[position].lrzId

        return view
    }

    override fun getItem(position: Int) = members[position]

    override fun getItemId(position: Int) = members[position].id.toLong()

    override fun getCount() = members.size

    fun updateSuggestions(newMembers: List<ChatMember>) {
        originalData.clear()
        originalData += newMembers

        members.clear()
        members += newMembers
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()

                if (charSequence != null) {
                    results.values = originalData.filter {
                        it.displayName?.contains(charSequence) ?: false
                    }
                }

                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                members += filterResults.values as List<ChatMember>
                notifyDataSetChanged()
            }
        }
    }

}
