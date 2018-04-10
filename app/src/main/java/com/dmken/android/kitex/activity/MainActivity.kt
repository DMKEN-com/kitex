package com.dmken.android.kitex.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.dmken.android.kitex.R
import com.dmken.android.kitex.util.PermissionUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity::class.java.name

        val PERMISSION_REQUEST_STORAGE = 0x1197
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Thread: UI

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPermissionError.setOnClickListener { askForPermissionsIfNeeded() }
        btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        btnKeyboardSettings.setOnClickListener { startActivity(Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        btnAbout.setOnClickListener { startActivity(Intent(this, AboutActivity::class.java)) }

        editTextTest.commitListener = imageViewTest::setImageURI

        // Check permissions.
        updateUIByPermissions()
        // This will also modify the UI depending on the outcome.
        askForPermissionsIfNeeded()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_REQUEST_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Update UI.
                    updateUIByPermissions()
                } else {
                    // Permission denied --> app does not work.

                    Log.d(TAG, "Permission denied.")

                    AlertDialog.Builder(this)
                            .setTitle(R.string.diag_permissionWarning_title)
                            .setMessage(R.string.diag_permissionWarning_msg)
                            .setPositiveButton(R.string.diag_permissionWarning_btn_askAgain) { _, _ -> askForPermissionsIfNeeded() }
                            .setNegativeButton(R.string.diag_permissionWarning_btn_later) { _, _ -> }
                            .show()
                }
            }
        }
    }

    private fun askForPermissionsIfNeeded() {
        // Initial UI update.
        updateUIByPermissions()

        if (!PermissionUtil.arePermissionsGranted(this)) {
            val requestPermission = {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_STORAGE)
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.diag_mediaPermission_title)
                        .setMessage(R.string.diag_mediaPermission_msg)
                        .setPositiveButton(R.string.diag_mediaPermission_btn) { _, _ -> requestPermission() }
                        .show()
            } else {
                requestPermission()
            }
        }
    }

    private fun updateUIByPermissions() {
        btnPermissionError.visibility = if (PermissionUtil.arePermissionsGranted(this)) View.GONE else View.VISIBLE
    }
}
