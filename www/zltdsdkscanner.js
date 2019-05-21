var exec = require('cordova/exec');

/**
 * 单扫
 * @param { result=>void }    success 成功回调:result:{text:string(文本)}
 * @param { message=>void }   error   失败回调
 */
function singleScan(success, error) {
    exec(success, error, 'ZltdSdkScannerPlugin', 'singleScan');
}

/**
 * 开始连扫
 * @param { result=>void }    success 成功回调:result:{status:number(状态:1:开始,2:扫描,3:结束), text?:string(文本)}
 * @param { message=>void }   error   失败回调
 */
function startContinuousScan(success, error) {
    exec(success, error, 'ZltdSdkScannerPlugin', 'startContinuousScan');
}

/**
 * 结束连扫
 * @param { ()=>void }        success 成功回调
 * @param { message=>void }   error   失败回调
 */
function stopContinuousScan(success, error) {
    exec(success, error, 'ZltdSdkScannerPlugin', 'stopContinuousScan');
}

exports.singleScan = singleScan;
exports.startContinuousScan = startContinuousScan;
exports.stopContinuousScan = stopContinuousScan;
