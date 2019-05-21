package cn.wiskind.cordova.plugin.zltdsdkscanner;

import android.util.Log;
import android.view.KeyEvent;

import com.zltd.industry.ScannerManager;
import com.zltd.industry.ScannerManager.IScannerStatusListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_BUTTON_A;
import static com.zltd.industry.ScannerManager.SCAN_CONTINUOUS_MODE;
import static com.zltd.industry.ScannerManager.SCAN_SINGLE_MODE;

/**
 * 智联天地软件开发工具包扫描器插件
 */
public class ZltdSdkScannerPlugin extends CordovaPlugin {

    /**
     * 日志标签
     */
    private static final String LOG_TAG = "ZltdSdkScannerPlugin";

    /**
     * 扫描器管理器
     */
    private ScannerManager mScannerManager;

    /**
     * 声音工具
     */
    private SoundUtils mSoundUtils;

    /**
     * 回调上下文
     */
    private CallbackContext callbackContext;

    /**
     * 扫描器状态监听器
     */
    private IScannerStatusListener mScannerStatusListener = new IScannerStatusListener() {

        @Override
        public void onScannerStatusChanage(int status) {
        }

        @Override
        public void onScannerResultChanage(final byte[] result) {
            try {
                String text = new String(result, "UTF-8");

                Log.i(LOG_TAG, "扫描结果：" + text);

                if (callbackContext == null) {
                    Log.w(LOG_TAG, "回调上下文为空：" + mScannerManager.getScanMode() + "，" + text);
                    return;
                }

                boolean invalid = text.isEmpty() || "Decode is interruptted or timeout ...".equals(text);

                if (mScannerManager.getScanMode() == SCAN_SINGLE_MODE) {
                    if (invalid) {
                        callbackContext.error("扫描中断或超时");
                    } else {
                        mSoundUtils.success();
                        callbackContext.success("{\"text\":\"" + text + "\"}");
                    }
                    callbackContext = null;
                } else if (mScannerManager.getScanMode() == SCAN_CONTINUOUS_MODE) {
                    if (!invalid) {
                        mSoundUtils.success();
                        sendKeepCallback(callbackContext, "{\"status\":2,\"text\":\"" + text + "\"}");
                    }
                } else {
                    Log.e(LOG_TAG, "扫描模式错误：" + mScannerManager.getScanMode());
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "扫描结果异常：" + mScannerManager.getScanMode(), e);
                if (mScannerManager.getScanMode() == SCAN_SINGLE_MODE && callbackContext != null) {
                    callbackContext.error("扫描结果异常");
                    callbackContext = null;
                }
            }
        }
    };

    @Override
    protected void pluginInitialize() {
        mScannerManager = ScannerManager.getInstance();
        mScannerManager.connectDecoderSRV();
        mScannerManager.addScannerStatusListener(mScannerStatusListener);
        mSoundUtils = new SoundUtils(cordova.getActivity());
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        mScannerManager.connectDecoderSRV();
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        mScannerManager.disconnectDecoderSRV();
        if (callbackContext != null) {
            if (mScannerManager.getScanMode() == SCAN_SINGLE_MODE) {
                callbackContext.error("扫描中断或超时");
            } else {
                callbackContext.success("{\"status\":3}");
            }
            callbackContext = null;
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("singleScan")) {
            singleScan(callbackContext);
            return true;
        } else if (action.equals("startContinuousScan")) {
            startContinuousScan(callbackContext);
            return true;
        } else if (action.equals("stopContinuousScan")) {
            stopContinuousScan(callbackContext);
            return true;
        }
        return false;
    }

    /**
     * 单扫
     *
     * @param callbackContext 回调上下文
     */
    private void singleScan(CallbackContext callbackContext) {
        if (this.callbackContext != null) {
            callbackContext.error("扫描中，请稍后");
            return;
        }
        mScannerManager.setScanMode(SCAN_SINGLE_MODE);
        mScannerManager.dispatchScanKeyEvent(new KeyEvent(ACTION_DOWN, KEYCODE_BUTTON_A));
        this.callbackContext = callbackContext;
    }

    /**
     * 开始连扫
     *
     * @param callbackContext 回调上下文
     */
    private void startContinuousScan(CallbackContext callbackContext) {
        if (this.callbackContext != null) {
            callbackContext.error("扫描中，请稍后");
            return;
        }
        mScannerManager.setScanMode(SCAN_CONTINUOUS_MODE);
        mScannerManager.dispatchScanKeyEvent(new KeyEvent(ACTION_DOWN, KEYCODE_BUTTON_A));
        sendKeepCallback(callbackContext, "{\"status\":1}");
        this.callbackContext = callbackContext;
    }

    /**
     * 结束连扫
     *
     * @param callbackContext 回调上下文
     */
    private void stopContinuousScan(CallbackContext callbackContext) {
        if (this.callbackContext == null) {
            callbackContext.error("尚未开始扫描");
            return;
        }
        int scanMode = mScannerManager.getScanMode();
        if (scanMode == SCAN_SINGLE_MODE) {
            callbackContext.error("扫描中，请稍后");
            return;
        } else if (scanMode != SCAN_CONTINUOUS_MODE) {
            callbackContext.error("扫描模式错误，请重试");
            return;
        }
        mScannerManager.dispatchScanKeyEvent(new KeyEvent(ACTION_DOWN, KEYCODE_BUTTON_A));
        callbackContext.success();
        this.callbackContext.success("{\"status\":3}");
        this.callbackContext = null;
    }

    @Override
    public void onDestroy() {
        mScannerManager.removeScannerStatusListener(mScannerStatusListener);
        mScannerManager.disconnectDecoderSRV();
        mSoundUtils.release();
        super.onDestroy();
    }

    /**
     * 发送持续性回调
     *
     * @param callbackContext 回调上下文
     * @param message         消息
     */
    private static void sendKeepCallback(CallbackContext callbackContext, String message) {
        PluginResult pluginResult = new PluginResult(Status.OK, message);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

}
