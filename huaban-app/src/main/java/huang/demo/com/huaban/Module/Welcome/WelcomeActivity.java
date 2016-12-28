package huang.demo.com.huaban.Module.Welcome;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import butterknife.BindString;
import butterknife.BindView;
import huang.demo.com.huaban.API.HttpsAPI.LoginAPI;
import huang.demo.com.huaban.Base.BaseActivity;
import huang.demo.com.huaban.HttpUtils.RetrofitClient;
import huang.demo.com.huaban.Module.Login.LoginActivity;
import huang.demo.com.huaban.Module.Login.TokenBean;
import huang.demo.com.huaban.Module.Main.MainActivity;
import huang.demo.com.huaban.Observable.MyRxObservable;
import huang.demo.com.huaban.Util.Base64;
import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.Util.SPBuild;
import huang.demo.com.huaban.Util.SPUtils;
import huang.demo.com.huaban.Util.Constant;
import huang.demo.com.huaban.Util.TimeUtils;
import huang.demo.com.huabandemo.R;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by LiYi on 2016/04/11  14:26
 * 欢迎页 进行登录判断 和联网重获token
 *
 * 花瓣网支持随便逛逛即不用登录也可以访问数据，如果登录后，会返回
 * 你的个性数据。isLogin=true，表示用户自己进行了登录操作，isLogin=false表示是随便逛逛，无需登录
 * 所有连接全部采用https
 * 如果没有登录，就没有后面的登录授权、用户名、密码等等东西
 */

public class WelcomeActivity extends BaseActivity {
    //登录的报文需要
    private static final String PASSWORD = "password";
    private static final int mTimeDifference = TimeUtils.HOUR;//格式良好


    //8.0以上butterknife关键词变为以下的写法
    @BindString(R.string.text_auto_login_fail)
    String mMessageFail;//这里作者做了国际化
    @BindView(R.id.img_welcome)
    ImageView mImageView;


    //该布局就只有一个imageview，里面放了一张大图
    @Override
    protected int getLayoutId() {
        return R.layout.activity_welcome;
    }

    @Override
    protected String getTAG() {
        return this.toString();
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, WelcomeActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //isLogin是父类的字段，没有登录时候为false,登录成功后为true
        isLogin = (Boolean) SPUtils.get(getApplicationContext(), Constant.ISLOGIN, isLogin);

    }

    //RxJava:一个在 Java VM 上使用可观测的序列来组成异步的、基于事件的程序的库
    //好的介绍网站：http://gank.io/post/560e15be2dca930e00da1083
    @Override
    protected void onResume() {
        super.onResume();
        //通过xml配置属性动画集，里面同时使用了x和Y方向的放大动画
        Animator animation = AnimatorInflater.loadAnimator(mContext, R.animator.welcome_animator);
        animation.setTarget(mImageView);

        MyRxObservable.add(animation)
                /**
                 * 线程控制 —— Scheduler
                 *   在默认情况下，事件的发出和消费都是在同一个线程的，Scheduler就是用于改变线程
                 *   运行的线程调度器
                 *
                 *   在哪个线程调用 subscribe()，就在哪个线程生产事件；在哪个线程生产事件，就在哪个线程消费事件。
                 *   如果需要切换线程，就需要用到 Scheduler （调度器）
                 *
                 *   Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler
                 *   subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。或者叫做事件产生的线程。
                 *   observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
                 */
                //事件产生在主线程，事件消费在IO线程
                .observeOn(Schedulers.io()) //观察者线程
                .subscribeOn(AndroidSchedulers.mainThread())//因为被观察者的call方法里面在运行动画，必须在主线程
                //过滤函数作用是过滤掉不满足这里条件的信息流
                .filter(new Func1<Void, Boolean>() {//对被观察者发出的消息进行过滤，链式调用
                    @Override
                    public Boolean call(Void aVoid) {
                        Logger.d("isLogin=" + isLogin);
                        //第一次启动app，由于没有登录过，所以isLogin为fasle
                        //第二次启动app，由于登录过，会验证登录时间，登录时间超过，则重新登录
                        return isLogin;//返回false,则下面的都不会被调用，而是只会调用onCompleted()
//                        return true;
//                        return false;
                    }
                })
                .filter(new Func1<Void, Boolean>() {//对被观察者发出的消息进行过滤
                    @Override
                    public Boolean call(Void aVoid) {
                        //获取上一次登录时间
                        Long lastTime = (Long) SPUtils.get(getApplicationContext(), Constant.LOGINTIME, 0L);
                        long dTime = System.currentTimeMillis() - lastTime;
                        Logger.d("dTime=" + dTime + " default" + mTimeDifference);
                        return dTime > mTimeDifference;//如果时间超过一定值，那么需要请求网络，重新登录
                    }
                })
                //map函数可以对消息对象流进行变换，返回任意一个新的对象，非常强大
                //这里将void类型转换为了Observable<TokenBean>类型
                //flatMap接收一个Observable的输出作为输入，同时输出另外一个Observable
                .flatMap(new Func1<Void, Observable<TokenBean>>() {//对被观察者发出的消息进行过滤变换
                    @Override
                    public Observable<TokenBean> call(Void aVoid) {
                        Logger.d("flatMap");
                        //密码认证模式
                        String userAccount = (String) SPUtils.get(getApplicationContext(), Constant.USERACCOUNT, "");
                        String userPassword = (String) SPUtils.get(getApplicationContext(), Constant.USERPASSWORD, "");
                        //开始发送post请求，https，返回已经解析好的对象
                        return getUserToken(userAccount, userPassword);
                    }
                })
//                .retryWhen(new RetryWithConnectivityIncremental(WelcomeActivity.this, 4, 15, TimeUnit.SECONDS))
                //事件产生在主线程，事件消费在主线程
                .observeOn(AndroidSchedulers.mainThread())//最后统一回到UI线程中处理

                //Subscriber<TokenBean>是订阅者，当被观察者发送了消息过来，则订阅者可以在这里收到消息
                //Subscriber(抽象类)订阅者实际上就是Observer(接口)观察者的抽象类，所以可以认为订阅者就是观察者
                //通过subscribe连接被观察者和观察者：被观察者.subscribe(观察者)

                .subscribe(new Subscriber<TokenBean>() {
                    @Override
                    public void onCompleted() {
                        Logger.d();
                        MainActivity.launch(WelcomeActivity.this);//跳转到mainActicity
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                        //如果出现错误，则跳转则登录界面
                        LoginActivity.launch(WelcomeActivity.this, mMessageFail);
                        finish();
                    }

                    @Override
                    public void onNext(TokenBean tokenBean) {
                        Logger.d("https success");
                        saveToken(tokenBean);//保存刚才访问得到的结果
                    }
                });

    }

    //保存认证服务器返回的结果
    private void saveToken(TokenBean tokenBean) {
        new SPBuild(getApplicationContext())
                .addData(Constant.LOGINTIME, System.currentTimeMillis())
                .addData(Constant.TOKENACCESS, tokenBean.getAccess_token())
                .addData(Constant.TOKENTYPE, tokenBean.getToken_type())
                .build();
    }

    //网络请求，登录请求认证
    private Observable<TokenBean> getUserToken(String username, String password) {
        //调用，开始进行网络请求，结果会自动解析并封装
        return RetrofitClient.createService(LoginAPI.class)
                .httpsTokenRx(Base64.mClientInto, PASSWORD, username, password);
    }

}
