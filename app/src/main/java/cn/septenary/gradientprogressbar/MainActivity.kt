package cn.septenary.gradientprogressbar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import cn.septenary.ui.widget.GradientProgressBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun changeProgress(view: View) {
        findViewById<GradientProgressBar>(R.id.bar).setProgress((view as TextView).text.toString().toInt(), true)
    }
}
