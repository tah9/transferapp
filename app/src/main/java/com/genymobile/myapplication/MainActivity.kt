package com.genymobile.myapplication

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.genymobile.myapplication.ui.theme.MyApplicationTheme
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.nio.ByteBuffer


class MainActivity : ComponentActivity(), SurfaceHolder.Callback {
    val name = "scrcpy"
    var test_surfaceView: SurfaceView? = null
    var surface_holder: SurfaceHolder? = null
    var surface: Surface? = null
    var d_width: Int? = null
    var d_height: Int? = null
    var thread2_server: Thread? = null

    var videoDecoder: VideoDecoder? = null


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var temp = this
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BoxWithConstraints {
                        val width = maxWidth
                        val height = maxHeight
                        AndroidView(
                            modifier = Modifier
                                .size(width, height),
                            factory = { context ->
                                SurfaceView(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        (width).value.toInt(),
                                        (height).value.toInt()
                                    )
//                                    setBackgroundColor(0xff000000.toInt())
                                }.also {
                                    test_surfaceView = it
                                    surface_holder = it.holder
                                    // 设置 SurfaceHolder 的回调
                                    // 设置 SurfaceHolder 的回调
                                    surface_holder?.addCallback(temp)

                                }
                            }
                        )
                    }
                }
            }
        }
        thread2_server = Thread {
            try {
//                inputStreamDecoder = InputStreamDecoder(null,1,1)

                val serverSocket = ServerSocket(20001)
                println("server_socket start success!")
//                while (true) {
                try {
                    // videoSocket
                    var videoSocket = serverSocket.accept()
                    var fileDescriptor =
                        ParcelFileDescriptor.fromSocket(videoSocket).fileDescriptor
                    Thread {
                        println("video_socket客户端已连接")
                        var header = ByteBuffer.allocate(/*64 +*/ 4)
                        IO.readFully(fileDescriptor, header)
                        header.flip()

                        val lParam: Int = header.getInt()
                        d_width = lParam shr 16
                        d_height = lParam and 0xFFFF

                        println("$d_width,$d_height")

//                        val name = ByteArray(64)
//                        println("设备名" + header.get(name))

                        videoDecoder = VideoDecoder(
                            fileDescriptor,
                            videoSocket.getInputStream(),
                            surface,
                            d_width!!,
                            d_height!!
                        )
                    }.start()

                    //controlSocket
                    val controlSocket = serverSocket.accept()
                    println("control connect success")
                    val oos = DataOutputStream(controlSocket.outputStream)
                    val ois = DataInputStream(controlSocket.inputStream)
//
//                    var displayId = ois.readLong()
//                    println("display___id $displayId")
                    Thread {
                        println("control_socket客户端已连接")
                        test_surfaceView?.setOnTouchListener { view, event ->
                            println("Pressed at $event")
                            Thread {
//                                oos.writeObject(event)
//                            oos.writeByte(0)


                                oos.writeInt(event.downTime.toInt())
                                oos.writeInt(event.action)
                                oos.writeInt(event.pointerCount)

                                for (i in 0..event.pointerCount - 1) {
                                    oos.writeInt(event.getPointerId(i))
                                    oos.writeInt(event.getX(i).toInt())
                                    oos.writeInt(event.getY(i).toInt())
                                    oos.writeFloat(event.getPressure(i))
                                }
                                oos.writeInt(event.buttonState)
                                oos.writeInt(event.source)
                                oos.flush()
                            }.start()

//                            oos.writeObject(event)
//                            outputStream.writeLong()
                            true
                        }
                    }.start()


                } catch (e: Exception) {
                    e.printStackTrace()
                }
//                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = holder.surface
        thread2_server?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

}


