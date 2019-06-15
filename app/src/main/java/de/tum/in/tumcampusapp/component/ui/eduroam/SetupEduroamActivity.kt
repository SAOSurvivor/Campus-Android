package de.tum.`in`.tumcampusapp.component.ui.eduroam

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.activity_setup_eduroam.cancelButton
import kotlinx.android.synthetic.main.activity_setup_eduroam.disableBackupButton
import kotlinx.android.synthetic.main.activity_setup_eduroam.errorContainer
import kotlinx.android.synthetic.main.activity_setup_eduroam.lrzIdEditText
import kotlinx.android.synthetic.main.activity_setup_eduroam.passwordEditText
import kotlinx.android.synthetic.main.activity_setup_eduroam.setupButton
import java.util.regex.Pattern

class SetupEduroamActivity : BaseActivity(R.layout.activity_setup_eduroam) {

    private val eduroamController: EduroamController by lazy { EduroamController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra(Const.EXTRA_FOREIGN_CONFIGURATION_EXISTS, false)) {
            showDeleteProfileDialog()
        }

        val lrzId = Utils.getSetting(this, Const.LRZ_ID, "")
        lrzIdEditText.setText(lrzId)

        if (lrzId.isEmpty()) {
            lrzIdEditText.requestFocus()
        } else {
            passwordEditText.requestFocus()
        }

        errorContainer.setOnClickListener { showDeleteProfileDialog() }
        disableBackupButton.setOnClickListener { startActivity(Intent(Settings.ACTION_SETTINGS)) }

        cancelButton.setOnClickListener {
            Utils.setSetting(this, Const.REFRESH_CARDS, true)
            finish()
        }

        setupButton.setOnClickListener { setup() }
    }

    private fun showDeleteProfileDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.eduroam_dialog_title)
            .setMessage(R.string.eduroam_dialog_info_text)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.eduroam_dialog_preferences) { dialogInterface, i ->
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }
            .show()
    }

    private fun setup() {
        // Verify that we have a valid LRZ / TUM ID
        val pattern = Pattern.compile(Const.TUM_ID_PATTERN)
        val lrzId = lrzIdEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (!pattern.matcher(lrzId).matches()) {
            Utils.showToast(this, getString(R.string.eduroam_not_valid_id))
            return
        }

        // We need some sort of password
        if (password.isEmpty()) {
            Utils.showToast(this, getString(R.string.eduroam_please_enter_password))
            return
        }

        // Do setup
        val success = eduroamController.configureEduroam(lrzId, password)
        if (success) {
            Utils.showToast(this, R.string.eduroam_success)
            Utils.setSetting(this, Const.REFRESH_CARDS, true)
            finish()
        } else {
            errorContainer.isVisible = true
        }
    }

}
