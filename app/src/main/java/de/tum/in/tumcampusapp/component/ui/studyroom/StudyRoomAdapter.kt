package de.tum.`in`.tumcampusapp.component.ui.studyroom

import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoom
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.format.DateTimeFormat
import java.util.*

class StudyRoomAdapter(private val fragment: Fragment, private val studyRooms: List<StudyRoom>) :
        RecyclerView.Adapter<StudyRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_header_details_button, parent, false)
        return StudyRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudyRoomViewHolder, position: Int) {
        val (_, code, name, buildingName, _, occupiedUntil) = studyRooms[position]

        holder.apply {
            openRoomFinderButton.setText(R.string.go_to_room)
            openRoomFinderButton.tag = code
            headerTextView.text = code
            val isOccupied = occupiedUntil != null && !occupiedUntil.isBeforeNow

            val detailsText = StringBuilder("$name<br>$buildingName")
            if (isOccupied) {
                val time = DateTimeFormat.forPattern("HH:mm")
                        .withLocale(Locale.getDefault())
                        .print(occupiedUntil)
                detailsText.append("<br>${fragment.getString(R.string.occupied)} <b>$time</b>")
            }

            detailsTextView.text = Utils.fromHtml(detailsText.toString())

            val colorResId = if (isOccupied) R.color.red_100 else R.color.green_100
            val color = ContextCompat.getColor(holder.itemView.context, colorResId)
            cardView.setCardBackgroundColor(color)
        }
    }

    override fun getItemCount() = studyRooms.size
}