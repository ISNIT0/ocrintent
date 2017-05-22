# OCR Scanner
OCR Intent exposing app for Android

Currently only supports English


## Using with your app
See example project for more info
#### Kotlin:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val ACTION:String = "xyz.reeve.ocrscanner.SCAN"
    val PACKAGE:String = "xyz.reeve.ocrscanner"
    val ACTIVITYID:Int = 0

    //TODO: Check intent exists
    val i = Intent(ACTION)
    i.setPackage(PACKAGE)
    startActivityForResult(i, ACTIVITYID)
}
    
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    val result = data.getStringExtra("result")
    Toast.makeText(applicationContext, "OCR Result is: " + result, Toast.LENGTH_LONG).show()
}
```
