package co.mewi.webapp

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import android.content.DialogInterface
import androidx.annotation.NonNull


class MainActivityTemp : AppCompatActivity(),
    EasyPermissions.PermissionCallbacks {

    private var mGeoLocationOrigin: String? = null
    private var mGeoLocationCallback: GeolocationPermissions.Callback? = null
    val tag = MainActivityTemp::class.simpleName
    private val TAG = "TEST"
    private var mPermissionRequest: PermissionRequest? = null

    private val REQUEST_CAMERA_PERMISSION: Int = 1
    private val REQUEST_LOCATION_PERMISSION: Int = 2
    private val PERM_CAMERA = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebView()
        webView.loadUrl("https://permission.site")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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
                    Log.i(TAG, "onPermissionRequest")
                    mPermissionRequest = request
                    val requestedResources = request!!.resources
                    for (r in requestedResources) {
                        if (r == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                            // In this sample, we only accept video capture request.
                            if (hasCameraPermission()) {
                                mPermissionRequest!!.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                                Log.d(TAG, "Granted")
                            } else {
                                val alertDialogBuilder = AlertDialog.Builder(this@MainActivityTemp)
                                    .setTitle("Allow Permission to camera")
                                    .setPositiveButton("Allow") { dialog, which ->
                                        dialog.dismiss()
                                        EasyPermissions.requestPermissions(
                                            this@MainActivityTemp,
                                            "This app needs access to your camera so you can take pictures.",
                                            REQUEST_CAMERA_PERMISSION,
                                            Manifest.permission.CAMERA
                                        )
                                    }
                                    .setNegativeButton("Deny") { dialog, which ->
                                        dialog.dismiss()
                                        mPermissionRequest!!.deny()
                                        Log.d(TAG, "Denied")
                                        Toast.makeText(
                                            this@MainActivityTemp,
                                            "Permission Denied",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()

                            }

                            break
                        }
                    }
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String,
                    callback: GeolocationPermissions.Callback
                ) {
                    Log.e(TAG, "Location perm start")
                    mGeoLocationCallback = callback
                    mGeoLocationOrigin = origin
                    // Do We need to ask for permission?
                    if (hasLocationPermission()) {
                        callback.invoke(origin, true, false)
                    } else {
                        val alertDialogBuilder = AlertDialog.Builder(this@MainActivityTemp)
                            .setTitle("Allow Permission to location")
                            .setPositiveButton("Allow") { dialog, which ->
                                dialog.dismiss()
                                EasyPermissions.requestPermissions(
                                    this@MainActivityTemp,
                                    "This app needs access to your location to access",
                                    REQUEST_LOCATION_PERMISSION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            }
                            .setNegativeButton("Deny") { dialog, which ->
                                dialog.dismiss()
                                callback.invoke(origin, false, false)
                                Log.d(TAG, "Denied")
                                Toast.makeText(
                                    this@MainActivityTemp,
                                    "Permission Denied",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }
                }

                override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                    super.onPermissionRequestCanceled(request)
                    Toast.makeText(this@MainActivityTemp, "Permission Denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }


    }


    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this@MainActivityTemp, Manifest.permission.CAMERA)
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            this@MainActivityTemp,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                mPermissionRequest!!.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
            }
            REQUEST_LOCATION_PERMISSION -> {
                mGeoLocationOrigin?.let {
                    mGeoLocationCallback?.invoke(it, true, false)
                }
            }
        }
        Log.d(TAG, "onPermissionsGranted")
        Toast.makeText(this@MainActivityTemp, "Permission Granted", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsDenied")
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                mPermissionRequest!!.deny()
            }
            REQUEST_LOCATION_PERMISSION -> {
                mGeoLocationOrigin?.let {
                    mGeoLocationCallback?.invoke(it, false, false)
                }
            }
        }
        Toast.makeText(this@MainActivityTemp, "Permission Denied", Toast.LENGTH_SHORT)
            .show()
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