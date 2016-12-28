package huang.demo.com.huaban.Base;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;

import huang.demo.com.huaban.Util.Logger;

/**
 * Created by LiCola on  2015/12/02  13:25
 */
public class HuaBanApplication extends Application {
    private static final String TAG = "HuaBanApplication";

    private static HuaBanApplication instance;

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //LeakCanary:开源的在debug版本中检测内存泄漏的java库
        //简介：http://www.liaohuqiu.net/cn/posts/leak-canary-read-me/
        refWatcher = LeakCanary.install(this);//初始化 内存检测工具

        Fresco.initialize(HuaBanApplication.getInstance());//初始化Fresco图片加载框架

        //腾讯出品，第二个参数是注册腾讯开发者应用帐号时申请的APPID
        //当出现Crash，bug会自动上传到腾讯服务器，开发人员可以实时看到所有bug问题
        //就只需要这一条代码即可
        CrashReport.initCrashReport(getApplicationContext(), "900041074", true);


        //chrome 调试工具 ，集成了该facebook的安卓调试工具，连接USB，只需要在
        //chrome或者360浏览器里面输入chrome://inspect/#devices，就可以向调试网页一样看到各种
        //调试信息，但是使用该功能需要访问facebook网站，所以被强了，点击后是空白页面，被GFW了
        //要想使用该功能，只能翻墙
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

    }

    public static HuaBanApplication getInstance() {
        if (null == instance) {
            instance = new HuaBanApplication();
        }
        return instance;
    }

    /**
     * 获得内存监视器 监视任何对象
     * 使用 refWatcher.watch(object);
     * 只需要调用refWatcher.watch(object);就可以监视任何对象
     * @return 全局的refWatcher
     */
    public static RefWatcher getRefwatcher(Context context) {
        HuaBanApplication huaBanApplication = (HuaBanApplication) context.getApplicationContext();
        return huaBanApplication.refWatcher;
    }

}
