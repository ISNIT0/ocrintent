package xyz.reeve.ocrintent

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.graphics.BitmapFactory
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import co.metalab.asyncawait.async

import com.edmodo.cropper.CropImageView
import com.flurgle.camerakit.CameraKit
import com.flurgle.camerakit.CameraListener
import com.flurgle.camerakit.CameraView
import com.googlecode.tesseract.android.TessBaseAPI

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class SCAN : AppCompatActivity() {
    internal lateinit var scanCont: FrameLayout
    internal lateinit var cropCont: LinearLayout
    internal lateinit var camera: CameraView
    internal lateinit var takenPicture: ByteArray
    internal lateinit var toCameraButton: Button
    internal lateinit var image:Bitmap
    internal lateinit var progressCont: ConstraintLayout
    internal lateinit var croppedImage:Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        copyFilesToSdCard()

        setContentView(R.layout.activity_scan)
        val globalTouch = findViewById(R.id.global_touch)
        camera = findViewById(R.id.camera) as CameraView
        //camera.setJpegQuality(80)

        toCameraButton = findViewById(R.id.toCamera) as Button
        scanCont = findViewById(R.id.scan) as FrameLayout
        cropCont = findViewById(R.id.crop) as LinearLayout
        progressCont = findViewById(R.id.scanningProgress) as ConstraintLayout

        val cropImageView = findViewById(R.id.CropImageView) as CropImageView
        cropImageView.setGuidelines(1)

        globalTouch.setOnClickListener {
            camera.setCameraListener(object : CameraListener() {
                override fun onPictureTaken(picture: ByteArray) {
                    super.onPictureTaken(picture)
                    takenPicture = picture
                    try {
                        image = BitmapFactory.decodeByteArray(takenPicture, 0, takenPicture.size)

                        cropImageView.setImageBitmap(image)

                        Handler(applicationContext.getMainLooper()).post(Runnable(){
                            run() {
                                camera.stop()

                                scanCont.visibility = View.INVISIBLE
                                cropCont.visibility = View.VISIBLE
                            }
                        });

                    } catch (err: Exception) {
                        err.printStackTrace()
                        Toast.makeText(this@SCAN, "Out of memory, try a smaller region", Toast.LENGTH_LONG).show()
                    }

                }
            })

            camera.captureImage()
        }

        toCameraButton.setOnClickListener {
            scanCont.visibility = View.VISIBLE
            cropCont.visibility = View.INVISIBLE
            camera.start()
        }


        val doneButton = findViewById(R.id.doneButton) as Button
        doneButton.setOnClickListener {
            cropCont.setVisibility(View.INVISIBLE)
            progressCont.setVisibility(View.VISIBLE)
            croppedImage = cropImageView.croppedImage

            try {
                async {
                    val result = await { getText(croppedImage) }
                    Log.e("test", result)
                    val intent = Intent()
                    intent.putExtra("result", result)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }


            } catch (err: Exception) {
                err.printStackTrace()
                Toast.makeText(this@SCAN, "Out of memory, try a smaller region", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        camera.start()
    }

    override fun onPause() {
        camera.stop()
        super.onPause()
    }

    override fun onDestroy() {
        image.recycle()
        croppedImage.recycle()

        System.gc()
        super.onDestroy()
    }


    private fun getText(image: Bitmap): String {

        val baseApi = TessBaseAPI()
        baseApi.init(TARGET_BASE_PATH, "eng")
        baseApi.setImage(image)
        var recognizedText: String? = baseApi.utF8Text
        baseApi.end()
        if (recognizedText == null || recognizedText.length == 0) {
            recognizedText = "No Text Found"
        }
        return recognizedText
    }


    private fun copyFilesToSdCard() {
        copyFileOrDir("") // copy all files in assets folder in my project
    }

    private fun copyFileOrDir(path: String) {
        val assetManager = this.assets
        var assets: Array<String>? = null
        try {
            Log.i("tag", "copyFileOrDir() " + path)
            assets = assetManager.list(path)
            if (assets!!.size == 0) {
                copyFile(path)
            } else {
                val fullPath = TARGET_BASE_PATH + path
                Log.i("tag", "path=" + fullPath)
                val dir = File(fullPath)
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Log.i("tag", "could not create dir " + fullPath)
                for (i in assets.indices) {
                    val p: String
                    if (path == "")
                        p = ""
                    else
                        p = path + "/"

                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir(p + assets[i])
                }
            }
        } catch (ex: IOException) {
            Log.e("tag", "I/O Exception", ex)
        }

    }

    private fun copyFile(filename: String) {
        val assetManager = this.assets

        var `in`: InputStream? = null
        var out: OutputStream? = null
        var newFileName: String? = null
        try {
            Log.i("tag", "copyFile() " + filename)
            `in` = assetManager.open(filename)
            if (filename.endsWith(".jpg"))
            // extension was added to avoid compression on APK file
                newFileName = TARGET_BASE_PATH + filename.substring(0, filename.length - 4)
            else
                newFileName = TARGET_BASE_PATH + filename

            val file = File(newFileName)
            if (!file.exists()) {
                out = FileOutputStream(newFileName)

                val buffer = ByteArray(1024)
                var read: Int
                read = `in`!!.read(buffer)
                while (read != -1) {
                    out.write(buffer, 0, read)
                    read = `in`!!.read(buffer)
                }
                `in`!!.close()
                `in` = null
                out.flush()
                out.close()
                out = null
            }
        } catch (e: Exception) {
            Log.e("tag", "Exception in copyFile() of " + newFileName!!)
            Log.e("tag", "Exception in copyFile() " + e.toString())
        }

    }

    companion object {
        internal val TARGET_BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/ocrIntent/"
    }
}
