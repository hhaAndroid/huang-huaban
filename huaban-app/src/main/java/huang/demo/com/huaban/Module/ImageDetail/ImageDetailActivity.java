package huang.demo.com.huaban.Module.ImageDetail;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;


import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import huang.demo.com.huaban.API.HttpsAPI.OperateAPI;
import huang.demo.com.huaban.Entity.PinsMainEntity;
import huang.demo.com.huaban.Module.Main.MainActivity;
import huang.demo.com.huaban.Module.User.UserActivity;
import huang.demo.com.huaban.Observable.MyRxObservable;
import huang.demo.com.huaban.Service.DownloadService;
import huang.demo.com.huaban.Util.IntentUtils;
import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.Util.SPUtils;
import huang.demo.com.huaban.Widget.MyDialog.GatherDialogFragment;
import huang.demo.com.huaban.API.Fragment.OnImageDetailFragmentInteractionListener;
import huang.demo.com.huaban.Util.AnimatorUtils;
import huang.demo.com.huaban.Util.Constant;
import huang.demo.com.huaban.API.Dialog.OnGatherDialogInteractionListener;
import huang.demo.com.huaban.Base.BaseActivity;
import huang.demo.com.huaban.HttpUtils.ImageLoadFresco;
import huang.demo.com.huaban.HttpUtils.RetrofitClient;
import huang.demo.com.huaban.Module.BoardDetail.BoardDetailActivity;
import huang.demo.com.huaban.Module.Type.TypeActivity;
import huang.demo.com.huabandemo.R;
import huang.demo.com.huaban.Util.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ImageDetailActivity extends BaseActivity
        implements OnImageDetailFragmentInteractionListener, OnGatherDialogInteractionListener {

    //定义调用ImageDetailActivity的类 来自什么类型 在结束作为判断条件

    private static final String KEYPARCELABLE = "Parcelable";

    private int mActionFrom;
    public static final String ACTION_KEY = "key";//key值
    public static final int ACTION_DEFAULT = -1;//默认值
    public static final int ACTION_THIS = 0;//来自自己的跳转
    public static final int ACTION_MAIN = 1;//来自主界面的跳转
    public static final int ACTION_MODULE = 2;//来自模块界面的跳转
    public static final int ACTION_BOARD = 3;//来自画板界面的跳转
    public static final int ACTION_ATTENTION = 4;//来自我的关注界面的跳转
    public static final int ACTION_SEARCH = 5;//来自搜索界面的跳转

    @BindDrawable(R.drawable.ic_cancel_black_24dp) //都是矢量图
    Drawable mDrawableCancel;
    @BindDrawable(R.drawable.ic_refresh_black_24dp)
    Drawable mDrawableRefresh;

    //小图的后缀
    @BindString(R.string.url_image_big)
    String mFormatImageUrlBig;
    //大图的后缀
    @BindString(R.string.url_image_general)
    String mFormatImageGeneral;

    @BindView(R.id.appbar_image_detail)
    AppBarLayout mAppBar;
    @BindView(R.id.colltoolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.toolbar_image)
    Toolbar toolbar;
    @BindView(R.id.fab_image_detail)
    FloatingActionButton mFabOperate;
    @BindView(R.id.img_image_big)
    SimpleDraweeView mImgImageBig;

    public PinsMainEntity mPinsBean;

    public String mImageUrl;//图片地址
    public String mImageType;//图片类型
    public String mPinsId;

    private boolean isLike = false;//该图片是否被喜欢操作 默认false 没有被操作过
    private boolean isGathered = false;//该图片是否被采集过

    private String[] mBoardIdArray;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_image_detail;
    }

    @Override
    protected String getTAG() {
        return this.toString();
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, ImageDetailActivity.class);
        activity.startActivity(intent);
    }

    public static void launch(Activity activity, int action,View view) {
        Intent intent = new Intent(activity, ImageDetailActivity.class);
        //action=1，表示来自mainActivity的跳转
        intent.putExtra(ACTION_KEY, action);//有了这个就使能了home返回功能，奇葩设置
        activity.startActivity(intent);
    }

    //fasle也一样
    @Override
    protected boolean isTranslucentStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //过渡动画--目前没有使用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //该方法：暂时阻止启动共享元素 Transition，共享元素就是我们的imageview
            postponeEnterTransition();//延迟共享元素的过渡动画
        }


        EventBus.getDefault().register(this);//注册
        mActionFrom = getIntent().getIntExtra(ACTION_KEY, ACTION_DEFAULT);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);//设置打开时的文字颜色

        recoverData(savedInstanceState);//恢复数据


        mImageUrl = mPinsBean.getFile().getKey();
        mImageType = mPinsBean.getFile().getType();
        mPinsId = String.valueOf(mPinsBean.getPin_id());
        isLike = mPinsBean.isLiked();

        //设置图片空间的宽高比
        int width = mPinsBean.getFile().getWidth();
        int height = mPinsBean.getFile().getHeight();
        mImgImageBig.setAspectRatio(Utils.getAspectRatio(width, height));
        Logger.d("aspect=" + mImgImageBig.getAspectRatio());

        getSupportFragmentManager().
                beginTransaction().replace(R.id.framelayout_info_recycler, ImageDetailFragment.newInstance(mPinsId)).commit();

    }


    //应该没有效果的
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (hasFocus) {
                //恢复过渡效果
                startPostponedEnterTransition();
            }
        }
    }


    private void recoverData(Bundle savedInstanceState) {
        Logger.d(TAG);
        //被销毁之后 恢复数据
        if (savedInstanceState != null) {
            if ((savedInstanceState.getParcelable(KEYPARCELABLE) != null) && (mPinsBean == null)) {
                Logger.d();
                mPinsBean = savedInstanceState.getParcelable(KEYPARCELABLE);
            }
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.d(TAG);
        outState.putParcelable(KEYPARCELABLE, mPinsBean);
    }

    @Override
    protected void initResAndListener() {
        mFabOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGatherDialog();

//                boolean isEquals=RetrofitClient.retrofit()==RetrofitClient.retrofit();
//                Logger.d("isEquals="+isEquals);
            }
        });

        mImgImageBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d();
//                ImageScaleDialogFragment fragment = ImageScaleDialogFragment.create();
//                fragment.show(getSupportFragmentManager(), null);

            }
        });
    }


    /**
     * 创建对话框
     */
    private void showGatherDialog() {

        String boardTitleArray = (String) SPUtils.get(mContext, Constant.BOARDTILTARRAY, "");
        String mBoardId = (String) SPUtils.get(mContext, Constant.BOARDIDARRAY, "");
        Logger.d("title is " + boardTitleArray);

        String[] array = boardTitleArray != null ? boardTitleArray.split(Constant.SEPARATECOMMA) : new String[0];
        mBoardIdArray = mBoardId != null ? mBoardId.split(Constant.SEPARATECOMMA) : new String[0];
        GatherDialogFragment fragment = GatherDialogFragment.create(mAuthorization, mPinsId, mPinsBean.getRaw_text(), array);
        fragment.show(getSupportFragmentManager(), null);//显示自定义对话框

    }


    @Override
    protected void onResume() {
        super.onResume();

        showImage();//显示图片

    }

    /**
     * 显示图片的操作
     * 主要逻辑 根据图片类型 git图 使fab旋转 表示loading
     */
    private void showImage() {
        final ObjectAnimator objectAnimator;
        if (Utils.checkIsGif(mImageType)) {
            //fab图片旋转
            objectAnimator = AnimatorUtils.getRotationFS(mFabOperate);
            objectAnimator.start();
        } else {
            objectAnimator = null;
        }


        String url = String.format(mFormatImageUrlBig, mImageUrl);
        String url_low = String.format(mFormatImageGeneral, mImageUrl);
        //加载大图
        new ImageLoadFresco.LoadImageFrescoBuilder(mContext, mImgImageBig, url)
//                .setActualImageScaleType(ScalingUtils.ScaleType.FOCUS_CROP)
                .setUrlLow(url_low)
                .setRetryImage(mDrawableRefresh)
                .setFailureImage(mDrawableCancel)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        //图片加载成功回调
                        super.onFinalImageSet(id, imageInfo, animatable);
                        Logger.d("onFinalImageSet " + Thread.currentThread().toString());

                        if (animatable != null) {
                            animatable.start();
                        }
                        if (objectAnimator != null && objectAnimator.isRunning()) {
                            mImgImageBig.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    objectAnimator.cancel();
                                }
                            }, 600);
                        }

                    }

                    @Override
                    public void onSubmit(String id, Object callerContext) {
                        super.onSubmit(id, callerContext);
                        //提交申请回调 相当于开始
                        Logger.d("onSubmit " + Thread.currentThread().toString());

                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        //失败的回调
                        super.onFailure(id, throwable);
                        Logger.d(throwable.toString());

                    }
                })
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //创建menu视图
        super.onCreateOptionsMenu(menu);
        Logger.d();
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //onCreateOptionsMenu的后续
        Logger.d();
        //menu文件中默认 选择没有选中的drawable
        setIconDynamic(menu.findItem(R.id.action_like), isLike);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                actionHome(mActionFrom);//根据action值 选择向上键的 操作结果
                break;
            case R.id.action_like:
                actionLike(item);

                break;
            case R.id.action_download:
                actionDownload(item);
                break;
            case R.id.action_gather:
                showGatherDialog();
                break;
        }

        // boolean Return false to allow normal menu processing to
        // proceed, true to consume it here.
        // false：允许继续事件传递  true：就自己消耗事件 不再传递
        return true;
    }

    private void actionDownload(MenuItem item) {
        Logger.d();
        DownloadService.launch(this, mImageUrl, mImageType);
    }

    /**
     * 设置动态的icon图标 反向设置
     * 如果为true 显示undo图片
     * 为false 显示do图标
     * 所以传入当前状态值就可以 内部已经做判断
     *
     * @param item
     * @param isLike
     */
    private void setIconDynamic(MenuItem item, boolean isLike) {
        AnimatedVectorDrawableCompat drawableCompat;
        drawableCompat = AnimatedVectorDrawableCompat.create(mContext,
                isLike ? R.drawable.drawable_animation_favorite_undo : R.drawable.drawable_animation_favorite_do);
        item.setIcon(drawableCompat);
    }


    private void actionHome(int mActionFrom) {
        switch (mActionFrom) {
            case ACTION_MAIN:
                //在maxifest已经定义 默认处理
                MainActivity.launch(this);
                break;
            case ACTION_MODULE:
                TypeActivity.launch(this, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            case ACTION_BOARD:
//                BoardDetailActivity.launch(this, );
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true)
    public void onEventReceiveBean(PinsMainEntity bean) {
        //接受EvenBus传过来的数据
        Logger.d(TAG + " receive bean");
        this.mPinsBean = bean;
    }

    @Override
    public void onClickPinsItemImage(PinsMainEntity bean, View view) {
        ImageDetailActivity.launch(this, mActionFrom,view);
    }

    @Override
    public void onClickPinsItemText(PinsMainEntity bean, View view) {
        ImageDetailActivity.launch(this, mActionFrom,view);
    }

    @Override
    public void onClickBoardField(String key, String title) {
        BoardDetailActivity.launch(this, key, title);
    }

    @Override
    public void onClickUserField(String key, String title) {
        UserActivity.launch(this, key, title);
    }

    /**
     * 警告：用户可能没有任何应用处理您发送到 startActivity() 的隐式 Intent。
     * 如果出现这种情况，则调用将会失败，且应用会崩溃。
     * 要验证 Activity 是否会接收 Intent，请对 Intent 对象调用 resolveActivity()。
     * 如果结果为非空，则至少有一个应用能够处理该 Intent，且可以安全调用 startActivity()。
     * 如果结果为空，则不应使用该 Intent。如有可能，您应禁用发出该 Intent 的功能。
     *
     * @param link
     */
    @Override
    public void onClickImageLink(String link) {
        //点击图片链接的回调
        //打开选择浏览器 再浏览界面
        Intent intent = IntentUtils.startUriLink(link);
        if (IntentUtils.checkResolveIntent(this, intent)) {
            startActivity(intent);
        } else {
            Logger.d("checkResolveIntent = null");
        }

    }

    @Override
    public void onDialogPositiveClick(String describe, int selectPosition) {
        Logger.d("describe=" + describe + " selectPosition=" + selectPosition);

        actionGather(describe, selectPosition);
    }

    private void actionLike(MenuItem item) {
        Logger.d();
        //根据当前值 取操作符
        String operate = isLike ? Constant.OPERATEUNLIKE : Constant.OPERATELIKE;
        RetrofitClient.createService(OperateAPI.class)
                .httpsLikeOperate(mAuthorization, mPinsId, operate)
                .subscribeOn(Schedulers.io())
                .delay(600, TimeUnit.MILLISECONDS)//延迟 使得能够完成动画
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<LikePinsOperateBean>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        Logger.d();
                        item.setEnabled(false);//不可点击
                        AnimatedVectorDrawableCompat drawable = (AnimatedVectorDrawableCompat) item.getIcon();
                        if (drawable != null) {
                            drawable.start();
                        }

                    }

                    @Override
                    public void onCompleted() {
                        Logger.d();
                        item.setEnabled(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                        item.setEnabled(true);
                        checkException(e, mAppBar);
                    }

                    @Override
                    public void onNext(LikePinsOperateBean likePinsOperateBean) {
                        Logger.d();
                        //网络操作成功 标志位取反 然后重设图标
                        isLike = !isLike;
                        setIconDynamic(item, isLike);
                    }
                });
    }

    private void actionGather(String describe, int selectPosition) {

        Animator animation = AnimatorUtils.getRotationAD(mFabOperate);
        //运行旋转动画
        MyRxObservable.add(animation)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Void, Observable<GatherResultBean>>() {
                    @Override
                    public Observable<GatherResultBean> call(Void aVoid) {
                        //如果没有登录，这个也是没有用的，而且根本没有使用，不用关心
                        return RetrofitClient.createService(OperateAPI.class)
                                .httpsGatherPins(mAuthorization, mBoardIdArray[selectPosition], describe, mPinsId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//最后统一回到UI线程中处理
                .subscribe(new Subscriber<GatherResultBean>() {
                    @Override
                    public void onCompleted() {
                        //动画执行完成后，FAB隐藏，然后FAB立刻显示
                        //完整的动画效果：FAB旋转600ms，FAB隐藏，FAB立刻显示
                        //并修改FAB的图标
                        setFabDrawableAnimator(R.drawable.ic_done_white_24dp, mFabOperate);
                    }

                    @Override
                    public void onError(Throwable e) {
//                        Logger.d(e.toString());
                        checkException(e, mAppBar);
                        setFabDrawableAnimator(R.drawable.ic_report_white_24dp, mFabOperate);
                    }

                    @Override
                    public void onNext(GatherResultBean gatherResultBean) {
                        //成功后取反，定义了但是从来没有使用
                        isGathered = !isGathered;
                    }
                });
    }

    /**
     * 配置fab的drawable 和动画显示
     *
     * @param resId
     * @param mFabActionBtn
     */
    private void setFabDrawableAnimator(int resId, FloatingActionButton mFabActionBtn) {

        mFabActionBtn.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                Logger.d("onHidden");
                fab.setImageResource(resId);
                fab.show();//隐藏后立刻再次显示出来

            }
        });
    }
}
