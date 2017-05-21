package xyz.reeve.ocrintent

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.util.ArraySet
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast

import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    internal lateinit var iView: ImageView
    internal lateinit var textView: TextInputEditText
    internal lateinit var copyButton: Button
    internal lateinit var resetButton: Button
    internal lateinit var successView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val splashSizes = getSizes(R.drawable.splashscreen)
        val loadingSizes = getSizes(R.drawable.loading)

        val loadingSplash = BitmapFactory.decodeResource(resources, R.drawable.loading)

        successView = findViewById(R.id.successView) as LinearLayout
        textView = findViewById(R.id.textEdit) as TextInputEditText
        copyButton = findViewById(R.id.copyButton) as Button
        resetButton = findViewById(R.id.toCamera) as Button
        iView = findViewById(R.id.iv_background) as ImageView
        iView.layoutParams = LinearLayout.LayoutParams(splashSizes[0], splashSizes[1])

        iView.setOnClickListener {
            iView.setImageBitmap(loadingSplash)
            iView.layoutParams = LinearLayout.LayoutParams(loadingSizes[0], loadingSizes[1])

            val i = Intent(this@MainActivity, SCAN::class.java)
            startActivityForResult(i, 0)
        }

        resetButton.setOnClickListener { iView.performClick() }
    }

    private fun getSizes(imageId: Int): IntArray {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val height = metrics.heightPixels
        val width = metrics.widthPixels

        val bmap = this.resources.getDrawable(imageId) as BitmapDrawable
        val bmapWidth = bmap.bitmap.width.toFloat()
        val bmapHeight = bmap.bitmap.height.toFloat()

        val wRatio = width / bmapWidth
        val hRatio = height / bmapHeight

        var ratioMultiplier = wRatio
        // Untested conditional though I expect this might work for landscape mode
        if (hRatio < wRatio) {
            ratioMultiplier = hRatio
        }

        val newBmapWidth = (bmapWidth * ratioMultiplier).toInt()
        val newBmapHeight = (bmapHeight * ratioMultiplier).toInt()
        val ret = intArrayOf(newBmapWidth, newBmapHeight)
        return ret
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                var result = data.getStringExtra("result")
                if(result == null || result.length == 0) result = "No Text Found";

                textView.setText(result)
                iView.visibility = View.INVISIBLE
                successView.visibility = View.VISIBLE

                copyButton.setOnClickListener {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("OCR Text", result)
                    clipboard.primaryClip = clip
                    Toast.makeText(this@MainActivity, "Coppied to Clipboard", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
