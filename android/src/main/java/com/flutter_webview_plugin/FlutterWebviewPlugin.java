package com.flutter_webview_plugin;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.Display;
import android.webkit.WebStorage;
import android.widget.FrameLayout;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FlutterWebviewPlugin
 */
public class FlutterWebviewPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler {
    private Activity activity;
    private WebviewManager webViewManager;
    private Context context;
    static MethodChannel channel;
    private static final String CHANNEL_NAME = "flutter_webview_plugin";
    private static final String JS_CHANNEL_NAMES_FIELD = "javascriptChannelNames";

//    public static void registerWith(PluginRegistry.Registrar registrar) {
//        if (registrar.activity() != null) {
//            channel = new MethodChannel(registrar.messenger(), CHANNEL_NAME);
//            final FlutterWebviewPlugin instance = new FlutterWebviewPlugin(registrar.activity(), registrar.activeContext());
//            registrar.addActivityResultListener(instance);
//            channel.setMethodCallHandler(instance);
//        }
//    }



    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "launch":
                openUrl(call, result);
                break;
            case "close":
                close(call, result);
                break;
            case "eval":
                eval(call, result);
                break;
            case "resize":
                resize(call, result);
                break;
            case "reload":
                reload(call, result);
                break;
            case "back":
                back(call, result);
                break;
            case "forward":
                forward(call, result);
                break;
            case "hide":
                hide(call, result);
                break;
            case "show":
                show(call, result);
                break;
            case "reloadUrl":
                reloadUrl(call, result);
                break;
            case "stopLoading":
                stopLoading(call, result);
                break;
            case "cleanCookies":
                cleanCookies(call, result);
                break;
            case "canGoBack":
                canGoBack(result);
                break;
            case "canGoForward":
                canGoForward(result);
                break;
            case "cleanCache":
                cleanCache(result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void cleanCache(MethodChannel.Result result) {
        webViewManager.cleanCache();
        WebStorage.getInstance().deleteAllData();
        result.success(null);
    }

    void openUrl(MethodCall call, MethodChannel.Result result) {
        boolean hidden = Boolean.TRUE.equals(call.argument("hidden"));
        String url = call.argument("url");
        String userAgent = call.argument("userAgent");
        boolean withJavascript = Boolean.TRUE.equals(call.argument("withJavascript"));
        boolean clearCache = Boolean.TRUE.equals(call.argument("clearCache"));
        boolean clearCookies = Boolean.TRUE.equals(call.argument("clearCookies"));
        boolean mediaPlaybackRequiresUserGesture = Boolean.TRUE.equals(call.argument("mediaPlaybackRequiresUserGesture"));
        boolean withZoom = Boolean.TRUE.equals(call.argument("withZoom"));
        boolean displayZoomControls = Boolean.TRUE.equals(call.argument("displayZoomControls"));
        boolean withLocalStorage = Boolean.TRUE.equals(call.argument("withLocalStorage"));
        boolean withOverviewMode = Boolean.TRUE.equals(call.argument("withOverviewMode"));
        boolean supportMultipleWindows = Boolean.TRUE.equals(call.argument("supportMultipleWindows"));
        boolean appCacheEnabled = Boolean.TRUE.equals(call.argument("appCacheEnabled"));
        Map<String, String> headers = call.argument("headers");
        boolean scrollBar = Boolean.TRUE.equals(call.argument("scrollBar"));
        boolean allowFileURLs = Boolean.TRUE.equals(call.argument("allowFileURLs"));
        boolean useWideViewPort = Boolean.TRUE.equals(call.argument("useWideViewPort"));
        String invalidUrlRegex = call.argument("invalidUrlRegex");
        boolean geolocationEnabled = Boolean.TRUE.equals(call.argument("geolocationEnabled"));
        boolean debuggingEnabled = Boolean.TRUE.equals(call.argument("debuggingEnabled"));
        boolean ignoreSSLErrors = Boolean.TRUE.equals(call.argument("ignoreSSLErrors"));

        if (webViewManager == null || webViewManager.closed) {
            Map<String, Object> arguments = (Map<String, Object>) call.arguments;
            List<String> channelNames = new ArrayList<>();
            if (arguments.containsKey(JS_CHANNEL_NAMES_FIELD)) {
                channelNames = (List<String>) arguments.get(JS_CHANNEL_NAMES_FIELD);
            }
            webViewManager = new WebviewManager(activity, context, channelNames);
        }

        FrameLayout.LayoutParams params = buildLayoutParams(call);

        activity.addContentView(webViewManager.webView, params);

        webViewManager.openUrl(withJavascript,
                clearCache,
                hidden,
                clearCookies,
                mediaPlaybackRequiresUserGesture,
                userAgent,
                url,
                headers,
                withZoom,
                displayZoomControls,
                withLocalStorage,
                withOverviewMode,
                scrollBar,
                supportMultipleWindows,
                appCacheEnabled,
                allowFileURLs,
                useWideViewPort,
                invalidUrlRegex,
                geolocationEnabled,
                debuggingEnabled,
                ignoreSSLErrors
        );
        result.success(null);
    }

    private FrameLayout.LayoutParams buildLayoutParams(MethodCall call) {
        Map<String, Number> rc = call.argument("rect");
        FrameLayout.LayoutParams params;
        if (rc != null) {
            params = new FrameLayout.LayoutParams(
                    dp2px(activity, Objects.requireNonNull(rc.get("width")).intValue()), dp2px(activity, Objects.requireNonNull(rc.get("height")).intValue()));
            params.setMargins(dp2px(activity, Objects.requireNonNull(rc.get("left")).intValue()), dp2px(activity, Objects.requireNonNull(rc.get("top")).intValue()),
                    0, 0);
        } else {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            params = new FrameLayout.LayoutParams(width, height);
        }

        return params;
    }

    private void stopLoading(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.stopLoading(call, result);
        }
        result.success(null);
    }

    void close(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.close(call, result);
            webViewManager = null;
        }
    }

    /**
     * Checks if can navigate back
     *
     * @param result
     */
    private void canGoBack(MethodChannel.Result result) {
        if (webViewManager != null) {
            result.success(webViewManager.canGoBack());
        } else {
            result.error("Webview is null", null, null);
        }
    }

    /**
     * Navigates back on the Webview.
     */
    private void back(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.back(call, result);
        }
        result.success(null);
    }

    /**
     * Checks if can navigate forward
     * @param result
     */
    private void canGoForward(MethodChannel.Result result) {
        if (webViewManager != null) {
            result.success(webViewManager.canGoForward());
        } else {
            result.error("Webview is null", null, null);
        }
    }

    /**
     * Navigates forward on the Webview.
     */
    private void forward(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.forward(call, result);
        }
        result.success(null);
    }

    /**
     * Reloads the Webview.
     */
    private void reload(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.reload(call, result);
        }
        result.success(null);
    }

    private void reloadUrl(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            String url = call.argument("url");
            Map<String, String> headers = call.argument("headers");
            if (headers != null) {
                webViewManager.reloadUrl(url, headers);
            } else {
                webViewManager.reloadUrl(url);
            }

        }
        result.success(null);
    }

    private void eval(MethodCall call, final MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.eval(call, result);
        }
    }

    private void resize(MethodCall call, final MethodChannel.Result result) {
        if (webViewManager != null) {
            FrameLayout.LayoutParams params = buildLayoutParams(call);
            webViewManager.resize(params);
        }
        result.success(null);
    }

    private void hide(MethodCall call, final MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.hide(call, result);
        }
        result.success(null);
    }

    private void show(MethodCall call, final MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.show(call, result);
        }
        result.success(null);
    }

    private void cleanCookies(MethodCall call, final MethodChannel.Result result) {
        CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean aBoolean) {

            }
        });
        result.success(null);
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }



    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_webview_plugin");
        context = binding.getApplicationContext();
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel.setMethodCallHandler(null);
        channel = null;
    }


    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();

        // Initialize webViewManager now or defer to first usage
        if (this.webViewManager == null) {
            this.webViewManager = new WebviewManager(activity, context, new ArrayList<>());
        }

        binding.addActivityResultListener((requestCode, resultCode, data) -> {
            if (webViewManager != null && webViewManager.resultHandler != null) {
                return webViewManager.resultHandler.handleResult(requestCode, resultCode, data);
            }
            return false;
        });
    }

    @Override
    public void onDetachedFromActivity() {
        this.activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }
}
