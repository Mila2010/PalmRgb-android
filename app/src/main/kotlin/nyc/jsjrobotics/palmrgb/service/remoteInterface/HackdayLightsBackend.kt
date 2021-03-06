package nyc.jsjrobotics.palmrgb.service.remoteInterface

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.google.gson.GsonBuilder
import nyc.jsjrobotics.palmrgb.DEBUG
import nyc.jsjrobotics.palmrgb.ERROR
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicInteger


/**
 * Service to download values from HackdayLightsBackend.
 * Always stop self after completing tasks, and do not allow binding
 */
class HackdayLightsBackend : Service() {

    private lateinit var retrofit: Retrofit
    private lateinit var backendApi : HackDayLightsApi

    private val DOMAIN: String = Paths.SERVER_ADDRESS
    private val downloadsInProgress : AtomicInteger = AtomicInteger(0)

    companion object {
        private val RPC_TYPE = "RPC_TYPE"
        private val RPC_FUNCTION = "RPC_FUNCTION"
        val CONNECTION_CHECK_RESPONSE = "CONNECTION_CHECK_RESPONSE"

        fun connectionCheckIntent() : Intent {
            return Intent(CONNECTION_CHECK_RESPONSE)
        }

        fun intent(requestType: RequestType) : Intent {
            val intent = Intent()
            intent.component = ComponentName("nyc.jsjrobotics.palmrgb", "nyc.jsjrobotics.palmrgb.service.remoteInterface.HackdayLightsBackend")
            intent.putExtra(RPC_TYPE, requestType.name)
            intent.putExtra(RPC_FUNCTION, requestType.rpcFunction)
            return intent
        }
    }
    override fun onCreate() {
        super.onCreate()
        DEBUG("Starting HackdayLightsBackend")
        val gson = GsonBuilder()
                .setLenient()
                .create()

        retrofit = Retrofit.Builder()
                .baseUrl(DOMAIN)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        backendApi = retrofit.create(HackDayLightsApi::class.java)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        DEBUG("Shutting down HackdayLightsBackend")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val requestType = intent.getStringExtra(RPC_TYPE)
            val requestFunction = intent.getStringExtra(RPC_FUNCTION)
            startRequestThread(requestType, requestFunction)
        }
        return START_NOT_STICKY
    }

    private fun startRequestThread(requestType: String, rpcFunction: String?) {
        downloadsInProgress.getAndIncrement()
        val requestType = RequestType.valueOf(requestType)
        Thread( Runnable {
            if (requestType == RequestType.LEFT_IDLE) {
                if (rpcFunction == null) {
                    ERROR("NULL request function")
                    return@Runnable
                }
                rainbowRequest(rpcFunction)
            } else if (requestType == RequestType.CHECK_CONNECTION) {
                connectionCheck()
            } else {
                ERROR("UNKNOWN request")
            }
        }).start()

    }

    private fun connectionCheck() {
        val request = backendApi.connectionCheck()
        try {
            val response = request.execute()
            DEBUG("Result: ${response.isSuccessful}")
            broadcastConnectionCheckResult(response.isSuccessful)
        } catch (e : Exception) {
            ERROR("Failed to connection check: $e")
            broadcastConnectionCheckResult(false)
        }
        checkStopSelf()
    }

    private fun broadcastConnectionCheckResult(successful: Boolean) {
        val intent = connectionCheckIntent()
        intent.putExtra(CONNECTION_CHECK_RESPONSE, successful)
        LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcastSync(intent)
    }

    private fun rainbowRequest(rpcFunction: String) {
        val request = backendApi.triggerFunction(rpcFunction)
        try {
            val response = request.execute()
            DEBUG("Result:${response.body()?.status.orEmpty()}")
        } catch (e : Exception) {
            ERROR("Failed to trigger rainbow: $e")
        }
        checkStopSelf()
    }


    private fun checkStopSelf() {
        if (downloadsInProgress.decrementAndGet() == 0) {
            stopSelf()
        }
    }

}