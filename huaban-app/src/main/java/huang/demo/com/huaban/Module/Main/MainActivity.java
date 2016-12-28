package huang.demo.com.huaban.Module.Main;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import huang.demo.com.huaban.API.Fragment.OnPinsFragmentInteractionListener;
import huang.demo.com.huaban.Base.BaseRecyclerHeadFragment;
import huang.demo.com.huaban.Entity.PinsMainEntity;
import huang.demo.com.huaban.Module.Login.LoginActivity;
import huang.demo.com.huaban.Module.Picture.PictureActivity;
import huang.demo.com.huaban.Module.User.UserActivity;
import huang.demo.com.huaban.Util.CompatUtils;
import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.Util.SPUtils;
import huang.demo.com.huaban.Module.Setting.SettingsActivity;
import huang.demo.com.huaban.API.Fragment.OnRefreshFragmentInteractionListener;
import huang.demo.com.huaban.API.OnFragmentRefreshListener;
import huang.demo.com.huaban.Base.BaseActivity;
import huang.demo.com.huaban.HttpUtils.ImageLoadFresco;
import huang.demo.com.huaban.Module.Follow.FollowActivity;
import huang.demo.com.huaban.Module.ImageDetail.ImageDetailActivity;
import huang.demo.com.huaban.Module.Search.SearchAndTypeActivity;
import huang.demo.com.huaban.Module.Type.TypeNewFragment;
import huang.demo.com.huabandemo.R;
import huang.demo.com.huaban.Util.Constant;
import rx.functions.Action1;

/**
 * @Title: MainActivity.java
 * @Description: 主界面
 * @Company:南京航空航天大学
 * @author：黄海安
 * @date： 16-7-5 下午3:13.
 */
public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnClickListener,
        OnPinsFragmentInteractionListener, //条目点击监听器
        OnRefreshFragmentInteractionListener,//下拉刷新监听器
        SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.navigation_view)
    NavigationView mNavigation;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.fab_operate)
    FloatingActionButton mFabOperate;
    @BindView(R.id.swipe_refresh_widget)
    SwipeRefreshLayout mSwipeRefresh;
    //注意SwipeRefreshLayout虽然是mainActicity布局里面，但是具体的操作实际在TypeNewFragment
    //为了能够下拉刷新，采用接口的方式进行耦合OnRefreshFragmentInteractionListener

    private SimpleDraweeView img_nav_head;//头像
    private TextView tv_nav_username;//用户名
    private TextView tv_nav_email;//用户邮箱

    private FragmentManager fragmentManager;
    private BaseRecyclerHeadFragment fragment;

    private final int mDrawableList[] = {R.drawable.ic_loyalty_black_36dp, R.drawable.ic_camera_black_36dp,
            R.drawable.ic_message_black_36dp, R.drawable.ic_people_black_36dp};
    private String[] types;
    private String[] titles;

    private Boolean isLogin;
    private String mUserName = Constant.EMPTY_STRING;
    private String mUserId = Constant.EMPTY_STRING;


    //刷新的接口 子Fragment实现
    private OnFragmentRefreshListener mListenerRefresh;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected String getTAG() {
        return this.toString();
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    public static void launch(Activity activity, int flag) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(flag);
        activity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        getData();

        //这也可以注册监听器，回调方法是OnSharedPreferenceChange()
        getSharedPreferences(SPUtils.FILE_NAME, SPUtils.MODE).registerOnSharedPreferenceChangeListener(this);
        intiDrawer(toolbar);//初始化DrawerLayout
        //作者没有使用xml定义头部，而是使用代码填充
        initHeadView();//为Drawer添加头部
        //menu在xml里面定义了
        intiMenuView();//为Drawer添加menu菜单项目

        selectFragment(0);//默认选中0


    }

    //取出各种需要用的全局变量
    private void getData() {
        types = getResources().getStringArray(R.array.type_array);
        titles = getResources().getStringArray(R.array.title_array);
        isLogin = (Boolean) SPUtils.get(mContext, Constant.ISLOGIN, false);
        if (isLogin) {
            //如果登录才有取以下值的意义
            getDataByLogin();
        }

    }

    //取出用户名和密码
    private void getDataByLogin() {
        mUserName = (String) SPUtils.get(mContext, Constant.USERNAME, mUserName);
        mUserId = (String) SPUtils.get(mContext, Constant.USERID, mUserId);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d(intent.toString());
    }

    @Override
    protected void initResAndListener() {
        //设置fab的图片，此处采用vector矢量图
        mFabOperate.setImageResource(R.drawable.ic_search_black_24dp);
        //RxBind
        RxView.clicks(mFabOperate)
                .throttleFirst(Constant.throttDuration, TimeUnit.MILLISECONDS)//防止抖动处理
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        //启动新activity
                        SearchAndTypeActivity.launch(MainActivity.this);
                    }
                });

        mSwipeRefresh.setColorSchemeResources(ints);//刷新时候的颜色变化
        //刷新监听器
        mSwipeRefresh.setOnRefreshListener(() -> mListenerRefresh.getHttpRefresh());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNavUserInfo();//设置导航栏用户信息
    }

    /**
     * 根据登录状态 显示头像和用户名
     */
    private void setNavUserInfo() {

        Logger.d("isLogin=" + isLogin);
        if (isLogin) {
            String key = (String) SPUtils.get(mContext, Constant.USERHEADKEY, "");
            if (!TextUtils.isEmpty(key)) {
                key = getString(R.string.urlImageRoot) + key;
                //图片裁剪库，显示圆形图片--建造者模式
                new ImageLoadFresco.LoadImageFrescoBuilder(mContext, img_nav_head, key)
                        .setIsCircle(true, true)
                        .build();
            } else {
                Logger.d("user head key is empty");
            }


            if (!TextUtils.isEmpty(mUserName)) {
                tv_nav_username.setText(mUserName);
            }

            String email = (String) SPUtils.get(mContext, Constant.USEREMAIL, "");
            if (!TextUtils.isEmpty(email)) {
                tv_nav_email.setText(email);
            }
        }
    }

    private void initHeadView() {
        /**
         * 代码手动填充 view作为头部布局
         * 得到view之后 就可以对headView进行操作
         */
        View headView = mNavigation.inflateHeaderView(R.layout.nav_header_main);
        LinearLayout group = ButterKnife.findById(headView, R.id.ll_nav_operation);
        tv_nav_username = ButterKnife.findById(headView, R.id.tv_nav_username);
        tv_nav_email = ButterKnife.findById(headView, R.id.tv_nav_email);
        img_nav_head = ButterKnife.findById(headView, R.id.img_nav_head);

        addButtonDrawable(group);//给里面的按钮填充背景图片

        tv_nav_username.setOnClickListener(this);
        img_nav_head.setOnClickListener(this);

    }

    /**
     * 取出父视图中的button 动态添加的Drawable资源
     * 使用了V4兼容包的Tint方法
     *
     * @param group
     */
    private void addButtonDrawable(LinearLayout group) {
        Button btn = null;
        for (int i = 0, size = group.getChildCount(); i < size; i++) {
            btn = (Button) group.getChildAt(i);
            //填充背景颜色--比较好的写法
            btn.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    CompatUtils.getTintListDrawable(mContext, mDrawableList[i], R.color.tint_list_pink),
                    null,
                    null);
            btn.setOnClickListener(this);
        }
    }


    /**
     * 手动填充Menu 方便以后对menu的调整
     */
    private void intiMenuView() {

        Menu menu = mNavigation.getMenu();
        String titleList[] = getResources().getStringArray(R.array.title_array);
        int order = 0;
        for (String title : titleList) {
            //xml里面定义了一个空的group,没有填充内容，这里通过代码填充内容
            menu.add(R.id.menu_group_type, order++, Menu.NONE, title).setIcon(mDrawableList[0]).setCheckable(true);
        }
        menu.getItem(0).setChecked(true);//默认选中第一项

    }

    private void intiDrawer(Toolbar toolbar) {
        //关联
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigation.setNavigationItemSelectedListener(this);
    }


    //点击导航栏不同按钮，切换不同的fragment
    private void selectFragment(int position) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        String type = types[position];
        String title = titles[position];
        fragment = TypeNewFragment.newInstance(type, title);//新的fragment
        if (fragment != null) {
            mListenerRefresh = fragment;//切换刷新监听器对象
        }
        transaction.replace(R.id.container_with_refresh, fragment);
        transaction.commit();
        setTitle(title);//设置标题
    }


    @Override
    public void onBackPressed() {
        //监听返回键
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            //如果DrawerLayout 拦截点击 关闭Drawer
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSharedPreferences(SPUtils.FILE_NAME, SPUtils.MODE).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //导航栏，按钮点击事件
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getGroupId() == R.id.menu_group_type) {
            //点击了组里面的按钮
            selectFragment(item.getItemId());
        } else {
            if (item.getItemId() == R.id.nav_set) {
                //切换设置界面
                SettingsActivity.launch(this);
            } else if (item.getItemId() == R.id.nav_exit) {
                //退出整个app
                exitOperate();//退出操作
            }
            //// TODO: 2016/3/24 0024 处理 设置 关于 界面
            Logger.d(item.getTitle().toString());
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void exitOperate() {
        //清除所有数据，包括用户名等等
        SPUtils.clear(mContext);
        finish();
    }


    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.img_nav_head://头像图片
                if (isLogin) {
                    //如果已经登录了，则切换到用户信息页面
                    UserActivity.launch(MainActivity.this, mUserId, mUserName);
                } else {
                    //否则切换到的登录页面
                    LoginActivity.launch(MainActivity.this);
                }
                break;
            case R.id.tv_nav_username://用户名

                break;

            case R.id.btn_nav_attention://关注
                FollowActivity.launch(MainActivity.this);//切换到关注页面
                break;

            default:
                break;

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private boolean mTouched;

    //点击某个具体的图片条目，跳转图片详情页
    @Override
    public void onClickPinsItemImage(PinsMainEntity bean, View view) {
        Logger.d();
//        ImageDetailActivity.launch(this, ImageDetailActivity.ACTION_MAIN,view);
        StartPictureActivity(bean, view);
    }


    private void StartPictureActivity(PinsMainEntity bean, View view) {
        if (view != null && !mTouched) {//防止没必要的处理
            String mUrlGeneralFormat = mContext.getResources().getString(R.string.url_image_general);
            String url_img = String.format(mUrlGeneralFormat, bean.getFile().getKey());

            mTouched = true;
            //填充图片，但是没有显示--非常有用，等待图片已经可以加载再跳转页面，可以改善交互
            Picasso.with(this).load(url_img).fetch(new Callback(){

                @Override
                public void onSuccess () {
                    mTouched = false;
                    //跳转页面
                    Intent intent = PictureActivity.newIntent(mContext, url_img, "无");
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MainActivity.this, view, PictureActivity.TRANSIT_PIC);
                    try {
                        ActivityCompat.startActivity(MainActivity.this, intent, optionsCompat.toBundle());
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        //如果无效，则直接启动activity，没有动画效果
                        //5.0以下会抛异常
                        startActivity(intent);
                    }
                }

                @Override
                public void onError () {
                    mTouched = false;
                }
            });

        }


    }


    //点击某个具体的图片下方的文字条目，跳转图片详情页
    @Override
    public void onClickPinsItemText(PinsMainEntity bean, View view) {
        Logger.d();
        ImageDetailActivity.launch(this);
    }

    //切换刷新状态
    @Override
    public void OnRefreshState(boolean isRefreshing) {

        mSwipeRefresh.setRefreshing(isRefreshing);
    }

    //前面注册了sp内容改变的监听器，当内容改变的时候，会回调这个方法
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Logger.d(key);
        if (Constant.ISLOGIN.equals(key)) {
            isLogin = sharedPreferences.getBoolean(Constant.ISLOGIN, false);
        }
    }


}
