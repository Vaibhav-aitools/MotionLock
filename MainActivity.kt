package com.motionlock.videowallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.motionlock.videowallpaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val pickVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                saveVideoUri(it)
                updateUI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnChoose.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pickVideo.launch(intent)
        }

        binding.btnSetWallpaper.setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, 
                    ComponentName(this@MainActivity, VideoWallpaperService::class.java))
            }
            startActivity(intent)
        }

        loadSavedState()
    }

    private fun saveVideoUri(uri: Uri) {
        getSharedPreferences("MotionLockPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("video_uri", uri.toString())
            .apply()
    }

    private fun loadSavedState() {
        val uriString = getSharedPreferences("MotionLockPrefs", Context.MODE_PRIVATE)
            .getString("video_uri", null)
        if (uriString != null) {
            updateUI(Uri.parse(uriString))
        }
    }

    private fun updateUI(uri: Uri) {
        binding.txtStatus.text = "Video Selected"
        binding.txtFilename.text = uri.lastPathSegment
        binding.btnSetWallpaper.isEnabled = true
    }
}
