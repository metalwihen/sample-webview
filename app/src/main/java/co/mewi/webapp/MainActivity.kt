package co.mewi.webapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val MY_PERMISSIONS_REQUEST_LOCATION = 22
    var mGeoLocationRequestOrigin: String? = null
    var mGeoLocationCallback: GeolocationPermissions.Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebView()

        supportActionBar?.hide() // Hide Toolbar

        webView.loadUrl("https://permission.site")
    }

    /**
     * TODO:
     * 1. X Error
     * 2. X Progress - webChromeClient / onProgressChanged
     * 3. X Hide Toolbar
     * 4. Permissions - Camera
     * 5. Permissions - Location
     * 6. X Back button sync with browser history
     * 7. Cookies
     * 8. Custom Headers
     */

    private fun initWebView() {
        with(webView.settings) {
            javaScriptEnabled = true
            defaultTextEncodingName = "utf-8"
            domStorageEnabled = true
        }

        webView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    Log.e("WEBAPPLOG", "OVERRIDE URL $url")
                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.e("WEBAPPLOG", "ON_PAGE_FINISHED")
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    Log.e("WEBAPPLOG", "Page Load Error 1: $errorCode") // THIS WORKED ON API 24
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e("WEBAPPLOG", "Page Load Error 2: $error") // THIS DID NOT WORK ON API 24
                }

            }

        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    Log.e("WEBAPPLOG", "PROGRESS $newProgress")
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    super.onPermissionRequest(request)
                    request?.grant(request?.resources);
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String,
                    callback: GeolocationPermissions.Callback
                ) {
                    Log.e("WEBAPPLOG", "WEBVIEW REQ LOCATION")

                    // Do We need to ask for permission?
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        ) {
                            AlertDialog.Builder(this@MainActivity)
                                .setMessage("WEBAPP NEEDS - DO YOU WANT TO GIVE PERMISSION TO LOCATION?")
                                .setNeutralButton(android.R.string.ok) { _, _ ->
                                    mGeoLocationRequestOrigin = origin
                                    mGeoLocationCallback = callback
                                    ActivityCompat.requestPermissions(
                                        this@MainActivity,
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        MY_PERMISSIONS_REQUEST_LOCATION
                                    )
                                }
                                .show()

                        } else {
                            // No explanation needed, we can request the permission.
                            mGeoLocationRequestOrigin = origin
                            mGeoLocationCallback = callback
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                MY_PERMISSIONS_REQUEST_LOCATION
                            )
                        }
                    } else {
                        // Tell the WebView that permission has been granted
                        callback.invoke(origin, true, false)
                    }
                }

            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    Log.e("WEBAPPLOG", "PERMISSION RECEIVED - Location")
                    mGeoLocationCallback?.invoke(mGeoLocationRequestOrigin, true, false)
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e("WEBAPPLOG", "PERMISSION DENIED - Location")
                    mGeoLocationCallback?.invoke(mGeoLocationRequestOrigin, false, false)
                }
            }
        }
        // other 'case' lines to check for other
        // permissions this app might request
    }

    override fun onBackPressed() {
        Log.e("WEBAPPLOG", "BACKPRESS ${webView.canGoBack()}")
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}