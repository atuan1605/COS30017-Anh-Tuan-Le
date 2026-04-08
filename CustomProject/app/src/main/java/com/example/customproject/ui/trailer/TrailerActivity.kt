package com.example.customproject.ui.trailer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.customproject.databinding.ActivityTrailerBinding

class TrailerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrailerBinding
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        binding = ActivityTrailerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val youtubeKey = intent.getStringExtra(EXTRA_YOUTUBE_KEY)
        if (youtubeKey.isNullOrBlank()) {
            finish()
            return
        }

        binding.btnClose.setOnClickListener {
            if (customView != null) exitFullscreen()
            finish()
        }
        setupWebView(youtubeKey)
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(youtubeKey: String) {
        binding.webViewTrailer.apply {
            setBackgroundColor(0xFF000000.toInt())
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    return !(url.contains("youtube.com") || url.contains("youtu.be") || url.contains("google.com"))
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    // Hide YouTube page UI, keep only the video player visible
                    view?.evaluateJavascript(HIDE_UI_JS, null)
                    // Poll for play button and auto-click it + then auto-fullscreen
                    view?.evaluateJavascript(AUTO_PLAY_AND_FULLSCREEN_JS, null)
                    binding.progressTrailer.visibility = View.GONE
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (customView != null) { callback?.onCustomViewHidden(); return }
                    customView = view
                    customViewCallback = callback
                    binding.webViewTrailer.visibility = View.GONE
                    binding.btnClose.visibility = View.GONE
                    binding.fullscreenContainer.addView(view)
                    binding.fullscreenContainer.visibility = View.VISIBLE
                    hideSystemBars()
                }

                override fun onHideCustomView() {
                    exitFullscreen()
                }
            }

            loadUrl("https://m.youtube.com/watch?v=$youtubeKey")
        }
    }

    private fun exitFullscreen() {
        binding.fullscreenContainer.removeAllViews()
        binding.fullscreenContainer.visibility = View.GONE
        binding.webViewTrailer.visibility = View.VISIBLE
        binding.btnClose.visibility = View.VISIBLE
        customViewCallback?.onCustomViewHidden()
        customView = null
        customViewCallback = null
        hideSystemBars()
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        if (customView != null) { exitFullscreen(); return }
        super.onBackPressed()
    }

    override fun onDestroy() {
        binding.webViewTrailer.destroy()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_YOUTUBE_KEY = "extra_youtube_key"

        // Hide YouTube page chrome, center the player on a black background
        private const val HIDE_UI_JS = """
            (function() {
                var s = document.createElement('style');
                s.textContent = `
                    /* hide everything except the player */
                    header, #header, .mobile-topbar-header,
                    .slim-video-information-renderer,
                    .watch-below-the-player, ytm-related-videos-renderer,
                    ytm-comments-entry-point-header-renderer,
                    ytm-engagement-panel-section-list-renderer,
                    .single-column-watch-next-results,
                    ytm-item-section-renderer,
                    .watch-next-feed, #bottom-bar, ytm-pivot-bar-renderer,
                    .ytm-autonav-toggle-button-container,
                    ytm-compact-video-renderer,
                    .video-secondary-info-renderer,
                    .metadata-container, #related, #comments, #chat,
                    ytm-watch-next-app-engagement-panel,
                    .watch-next-navbar {
                        display: none !important;
                    }
                    /* black bg, center the player vertically */
                    html, body {
                        background: #000 !important;
                        margin: 0 !important; padding: 0 !important;
                        height: 100% !important;
                        overflow: hidden !important;
                        display: flex !important;
                        align-items: center !important;
                        justify-content: center !important;
                    }
                    /* player fills viewport width */
                    #player, .player-container {
                        width: 100vw !important;
                        background: #000 !important;
                    }
                    /* landscape: stretch player to full viewport height */
                    @media (orientation: landscape) {
                        #player, .player-container, .html5-video-player {
                            height: 100vh !important;
                            max-height: 100vh !important;
                        }
                        video {
                            width: 100% !important;
                            height: 100% !important;
                            object-fit: contain !important;
                        }
                    }
                `;
                document.head.appendChild(s);
            })();
        """

        // Poll until the play button or video element appears, then auto-play + fullscreen
        private const val AUTO_PLAY_AND_FULLSCREEN_JS = """
            (function() {
                var attempts = 0;
                var timer = setInterval(function() {
                    attempts++;
                    if (attempts > 30) { clearInterval(timer); return; }
                    
                    // try clicking any play button
                    var playBtn = document.querySelector('button.ytp-large-play-button')
                        || document.querySelector('button[aria-label="Play"]')
                        || document.querySelector('.ytp-cued-thumbnail-overlay');
                    if (playBtn) playBtn.click();
                    
                    // try to play the video element directly
                    var video = document.querySelector('video');
                    if (video && video.paused) {
                        video.play().catch(function(){});
                    }

                    // after video starts, try fullscreen
                    if (video && !video.paused) {
                        var fsBtn = document.querySelector('button.ytp-fullscreen-button')
                            || document.querySelector('[aria-label="Full screen"]');
                        if (fsBtn) { fsBtn.click(); clearInterval(timer); }
                    }
                }, 500);
            })();
        """
    }
}
