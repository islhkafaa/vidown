package app.vidown

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                YoutubeDL.getInstance().init(this@Application)
                FFmpeg.getInstance().init(this@Application)
                Log.d("VidownApp", "YoutubeDL and FFmpeg initialized successfully")
            } catch (e: Exception) {
                Log.e("VidownApp", "Failed to initialize YoutubeDL or FFmpeg", e)
            }
        }
    }
}
