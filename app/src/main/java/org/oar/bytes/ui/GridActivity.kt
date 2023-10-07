package org.oar.bytes.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.oar.bytes.R
import org.oar.bytes.utils.ScreenProperties

class GridActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenProperties.load(this)
        setContentView(R.layout.activity_grid)
    }
}