package huang.demo.com.huaban.Base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import butterknife.ButterKnife;
import huang.demo.com.huaban.Util.Base64;
import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.Util.NetUtils;
import huang.demo.com.huaban.Util.SPUtils;
import huang.demo.com.huaban.Util.Constant;
import huang.demo.com.huabandemo.R;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @Title: BaseActivity.java
 * @Description:
 * @Company:南京航空航天大学
 * @author：黄海安
 * @date： 16-7-2 下午8:42.
 */
public abstract class BaseActivity extends AppCompatActivity {


    protected String TAG = getTAG();

    /**
     * 子类需要提供自身布局的ID
     *
     * @return 布局ID
     */
    protected abstract int getLayoutId();

    /**
     * 该类自身的TAG标签
     */
    protected abstract String getTAG();

    protected Context mContext;

    //关键的是否登录 由父类提供
    public boolean isLogin = false;
    //关键的https联网字段 由父类提供,这个是认证号，好像是作者反编译花瓣apk得到的
    public String mAuthorization;//没有登录的时候就是Base64.mClientInto

    protected static final int[] ints = new int[]{R.color.pink_300, R.color.pink_500, R.color.pink_700, R.color.pink_900};

    @Override
    public String toString() {
        return getClass().getSimpleName() + " @" + Integer.toHexString(hashCode());
    }

    //Composite意思是复合、综合、合成、混合
    private CompositeSubscription mCompositeSubscription;

    //得到订阅对象，可以管理所有的订阅者，没有使用，因为addSubscription也new了一次
//    public CompositeSubscription getCompositeSubscription() {
//        if (this.mCompositeSubscription == null) {
//            this.mCompositeSubscription = new CompositeSubscription();
//        }
//
//        return this.mCompositeSubscription;
//    }

    //添加订阅者
    public void addSubscription(Subscription s) {
        if (s == null) {
            return;
        }
        //写的非常规范
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }

        this.mCompositeSubscription.add(s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/1122/3712.html
         * 在BaseActivity.java里：我们通过判断当前sdk_int大于4.4(kitkat),则通过代码的形式设置status bar为透明
         * (这里其实可以通过values-v19 的sytle.xml里设置windowTranslucentStatus属性为true来进行设置，但是在某些手机会不起效，所以采用代码的形式进行设置)。
         * 还需要注意的是我们这里的AppCompatAcitivity是android.support.v7.app.AppCompatActivity支持包中的AppCompatAcitivity,也是为了在低版本的android系统中兼容toolbar。
         */
        //以下代码主要是做兼容，对于我们的手机系统，大于4.4，不用判断也可以，在xml里面设置了该属性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isTranslucentStatusBar()) {//全透明状态条
                Window window = getWindow();
                // Translucent status bar
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }

        setContentView(getLayoutId());

        ButterKnife.bind(this);//ButterKnife使用的第一步，必须的步骤

        mContext = this;
        getNecessaryData();//就是得到授权号
        initResAndListener();
        Logger.d(TAG);

    }

    /**
     * 空方法 规定子类 初始化监听器 和定义显示资源 的步骤
     */
    protected void initResAndListener() {

    }

    protected void getNecessaryData() {
//        UserSingleton.getInstance().isLogin(getApplication());
        //如果用户登录了，那么里面就会保存值
        isLogin = (boolean) SPUtils.get(mContext, Constant.ISLOGIN, false);
        mAuthorization = getAuthorizations(isLogin);
    }

    //是否statusBar 状态栏为透明 的方法 默认为真
    //没有效果的，我们在xml已经设置了
    protected boolean isTranslucentStatusBar() {
        return true;
    }

    protected String getAuthorizations(boolean isLogin) {

        String temp = " ";
        if (isLogin) { //没有登录时候肯定是false
            //如果用户登录过，那么里面肯定保存了这两个字段
            return SPUtils.get(mContext, Constant.TOKENTYPE, temp)
                    + temp
                    + SPUtils.get(mContext, Constant.TOKENACCESS, temp);
        }
        //否则直接返回Authorization字段，用于http基本认证，否则无法访问服务器资源
        return Base64.mClientInto;
    }


    @Override
    protected void onStart() {
        super.onStart();
        Logger.d(TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG);
        if (this.mCompositeSubscription != null) {
            //取消综合订阅者
            this.mCompositeSubscription.unsubscribe();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.d(TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG);
    }

    /**
     * 检查异常情况
     * @param e
     * @param mRootView
     */
    protected void checkException(Throwable e, View mRootView) {
        NetUtils.checkHttpException(mContext, e, mRootView);
    }
}
