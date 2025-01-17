package com.example.unihiveafricav2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.unihiveafricav2.MainActivity.Companion.webView
import com.example.unihiveafricav2.databinding.FragmentWebBinding


class WebFragment : Fragment() {

    private var _binding: FragmentWebBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_REQUEST_CODE = 100


    companion object {
        fun newInstance() = WebFragment()
    }

    private val viewModel: WebViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set WebChromeClient for advanced features (optional)

        webView = binding.webView
        webView!!.webChromeClient = object : WebChromeClient(){}
//        webView!!.settings.allowFileAccess = true
        webView!!.settings.allowContentAccess = true
        webView!!.settings.domStorageEnabled = true
        webView!!.settings.loadsImagesAutomatically = true
        webView!!.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW


        webView!!.settings.javaScriptEnabled= true
        webView!!.loadUrl("https://unihiveafrica.com/joinUs")




        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: android.webkit.WebView, url: String): Boolean {
                // Check if the URL is for WhatsApp or Telegram
                when {
                    url.startsWith("https://api.whatsapp.com/") -> {
                        // Open WhatsApp
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                    url.startsWith("https://t.me/") -> {
                        // Open Telegram
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                    else -> {
                        // Otherwise, let WebView load the URL
                        return super.shouldOverrideUrlLoading(view, url)
                    }
                }
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Stop the refresh indicator if it is active
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        // Set WebChromeClient for file uploads
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@WebFragment.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                try {
                    if (intent != null) {
                        startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                    }
                } catch (e: Exception) {
                    this@WebFragment.filePathCallback = null
                    return false
                }
                return true
            }
        }

        // Configure SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            webView?.reload() // Reload the current page
        }
    }

    // Handle file chooser results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val results: Array<Uri>? = if (resultCode == Activity.RESULT_OK && data != null) {
                data.data?.let { arrayOf(it) }
            } else {
                null
            }
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, you can proceed with the file upload
        } else {
            // Permission denied, inform the user
            Toast.makeText(requireContext(), "Permission denied. File uploads won't work.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


