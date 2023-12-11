package cn.island.daisycast.service

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.Surface
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import cn.island.daisycast.common.R
import cn.island.daisycast.transport.tx.Transmitter
import cn.island.daisycast.util.DisplayUtils

class CastService: Service() {
    private var mMediaProjection: MediaProjection? = null
    private var mTransmitter: Transmitter? = null
    private lateinit var mSurface: Surface
    private var mMediaCodec: MediaCodec? = null

    override fun onBind(intent: Intent?): IBinder {
        return CastManager()
    }

    override fun onCreate() {
        super.onCreate()
    }

    inner class CodecCallbackImpl: MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            /*ignored*/
        }

        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            codec.getOutputBuffer(index)?.let {
                val buffer = ByteArray(info.size)
                it.position(info.offset)
                it.limit(info.size)
                it.get(buffer, 0, info.size)
                Log.d(TAG, buffer.contentToString())
                mTransmitter?.write(buffer)
            }
            codec.releaseOutputBuffer(index, false)
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            System.err.println("codec: ${codec.name} error: $e")
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            Log.w(TAG, "MediaCodec format changed.")
        }

    }

    inner class CastManager: Binder() {
        fun start(data: Intent) {
            startForeground(data.getStringExtra(KEY_EXTRA_NOTIFICATION_CHANNEL_ID) ?: NotificationChannelCompat.DEFAULT_CHANNEL_ID)
            mMediaProjection = (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
                .getMediaProjection(Activity.RESULT_OK, data)
            mMediaProjection?.createVirtualDisplay(
                "daisy-display",
                DisplayUtils.widthPixels,
                DisplayUtils.heightPixels,
                DisplayUtils.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                createMediaCodec(),
                null,
                null)
            mMediaCodec!!.start()
        }

        fun stop() {
            mMediaProjection?.stop()
            mMediaCodec?.stop()
            mMediaCodec?.release()
            stopForeground(STOP_FOREGROUND_REMOVE)
        }

        fun attachToTransmitter(transmitter: Transmitter) {
            mTransmitter?.close()
            mTransmitter = transmitter
            mTransmitter?.open()
        }
    }

    private fun startForeground(notificationChannelId: String) {
        Log.d(TAG, notificationChannelId)
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_cast_notification)
            .setContentTitle("Title")
            .setContentText("Content")
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaProjection?.stop()
        mMediaCodec?.release()
    }

    private fun createMediaCodec(): Surface {
        mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            .apply {
                val outputFormat: MediaFormat = MediaFormat.createVideoFormat(
                    MediaFormat.MIMETYPE_VIDEO_AVC,
                    DisplayUtils.widthPixels,
                    DisplayUtils.heightPixels,
                ).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, 6000000)
                    setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                    setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / 30)
                    setInteger(MediaFormat.KEY_CAPTURE_RATE, 30)
                    setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                }
                configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                setCallback(CodecCallbackImpl())
            }
        return mMediaCodec!!.createInputSurface()
    }

    companion object {
        val TAG = CastService::class.simpleName
        const val KEY_EXTRA_NOTIFICATION_CHANNEL_ID = "notification-id"
    }
}