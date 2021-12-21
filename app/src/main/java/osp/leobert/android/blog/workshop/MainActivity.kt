package osp.leobert.android.blog.workshop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity(), Demo.OnResultListener {

    lateinit var demo: Demo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        demo = Demo(Host.ActivityHost(this))
        demo.onStart()
    }

    override fun onResult(text: String?) {
        demo.setText(text)
    }
}