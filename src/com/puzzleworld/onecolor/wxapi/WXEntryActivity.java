package com.puzzleworld.onecolor.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.puzzleworld.onecolor.Constants;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;

/** 微信客户端回调activity示例 */  
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {  
    // IWXAPI 是第三方app和微信通信的openapi接口  
    private IWXAPI api;
    /* APPID only for test */
    //public static final String APP_ID = "wxe288bcf07e6c4a2d";
    
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);  
        api.handleIntent(getIntent(), this);  
        super.onCreate(savedInstanceState);  
    }  
    @Override  
    public void onReq(BaseReq arg0) { }  
  
    @Override  
    public void onResp(BaseResp resp) {  
        /*LogManager.show(TAG, "resp.errCode:" + resp.errCode + ",resp.errStr:"  
                + resp.errStr, 1);*/
    	Log.d("kevin", "resp.errCode:" + resp.errCode + ",resp.errStr:"  
        + resp.errStr);
        switch (resp.errCode) {  
        case BaseResp.ErrCode.ERR_OK:  
            //分享成功  
            break;  
        case BaseResp.ErrCode.ERR_USER_CANCEL:  
            //分享取消  
            break;  
        case BaseResp.ErrCode.ERR_AUTH_DENIED:  
            //分享拒绝  
            break;  
        }  
    }  
}  