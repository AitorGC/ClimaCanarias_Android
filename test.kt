import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient

val client = object : WebViewClient() {
    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        // Return true to handle the crash and prevent the app from exiting
        return true
    }
}
