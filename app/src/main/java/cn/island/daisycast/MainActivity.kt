package cn.island.daisycast

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import cn.island.daisycast.databinding.ActivityMainBinding
import cn.island.daisycast.service.CastService
import cn.island.daisycast.transport.tx.Transmitter
import cn.island.daisycast.transport.tx.impl.WebSocketTransmitter

class MainActivity : AppCompatActivity() {

    private lateinit var mCastManager: CastService.CastManager
    private var mCasting: Boolean = false
    private val mBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var mCastLauncher: ActivityResultLauncher<Intent>
    private val mTransmitter: Transmitter by lazy {
        WebSocketTransmitter(8080)
    }

    private val mServerCollection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mCastManager = service as CastService.CastManager
            mCastManager.attachToTransmitter(mTransmitter)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mCastLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data!!.putExtra(CastService.KEY_EXTRA_NOTIFICATION_CHANNEL_ID, MyApplication.NOTIFICATION_CHANNEL_ID)
                mCastManager.start(it.data!!)
            }
        }
        Intent()
            .apply {
                setClass(applicationContext, CastService::class.java)
            }
            .let {
                bindService(it, mServerCollection, BIND_AUTO_CREATE)
            }
        mBinding.switchButton.setOnClickListener {
            mCasting = !mCasting
            switchCast()
        }
    }

    private fun switchCast() {
        if (mCasting) {
            val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mCastLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        } else {
            mCastManager.stop()
        }
    }
}