package com.dmken.android.kitex.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.dmken.android.kitex.R
import kotlinx.android.synthetic.main.activity_main.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Thread: UI

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }
}
