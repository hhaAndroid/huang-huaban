package huang.demo.com.huaban.Module.Type;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import huang.demo.com.huaban.API.Fragment.OnPinsFragmentInteractionListener;
import huang.demo.com.huaban.API.OnFragmentRefreshListener;
import huang.demo.com.huaban.Base.BaseActivity;
import huang.demo.com.huaban.Base.BaseRecyclerHeadFragment;
import huang.demo.com.huaban.Entity.PinsMainEntity;
import huang.demo.com.huaban.Module.ImageDetail.ImageDetailActivity;
import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.API.Fragment.OnRefreshFragmentInteractionListener;
import huang.demo.com.huabandemo.R;

/**
 * Created by LiCola on  2016/03/20  12:00
 * 负责显示各个模块
 * 从Search模块跳转
 * 显示用Fragment展示UI
 *
 * 这个模块是从搜索模块点击跳转过来的，布局和MainActivity一模一样
 * 几乎就是MainActivy的微缩版
 */
public class TypeActivity extends BaseActivity
        implements OnPinsFragmentInteractionListener,
        OnRefreshFragmentInteractionListener {

    protected static final String TYPE_KEY = "TYPE_KEY";
    protected static final String TYPE_TITLE = "TYPE_TITLE";

    protected String mType;
    protected String mTitle;

    @BindView(R.id.swipe_refresh_widget)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.fab_operate)
    FloatingActionButton mFabOperate;

    //刷新的接口 子Fragment实现
    private OnFragmentRefreshListener mListenerRefresh;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_module;
    }

    @Override
    protected String getTAG() {
        return this.toString();
    }

    public static void launch(Activity activity, String title, String type) {
        Intent intent = new Intent(activity, TypeActivity.class);
        intent.putExtra(TYPE_TITLE, title);
        intent.putExtra(TYPE_KEY, type);
        activity.startActivity(intent);
    }

    public static void launch(Activity activity, int flag) {
        Intent intent = new Intent(activity, TypeActivity.class);
        intent.setFlags(flag);
        activity.startActivity(intent);
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, TypeActivity.class);
        activity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_module);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getData();
        setTitle(mTitle);
        BaseRecyclerHeadFragment fragment = TypeNewFragment.newInstance(mType, mTitle);
        if (fragment != null) {
            mListenerRefresh = fragment;
        }
        getSupportFragmentManager().
                beginTransaction().replace(R.id.container_with_refresh, fragment).commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void initResAndListener() {
        mSwipeRefresh.setColorSchemeResources(ints);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListenerRefresh.getHttpRefresh();
            }
        });

        mFabOperate.setImageResource(R.drawable.ic_search_black_24dp);
        mFabOperate.setOnClickListener(v -> finish());
    }

    private void getData() {
        mTitle = getIntent().getStringExtra(TYPE_TITLE);
        mType = getIntent().getStringExtra(TYPE_KEY);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d(intent.toString());
    }

    @Override
    public void onClickPinsItemImage(PinsMainEntity bean, View view) {
        ImageDetailActivity.launch(this, ImageDetailActivity.ACTION_MODULE,view);
    }

    @Override
    public void onClickPinsItemText(PinsMainEntity bean, View view) {
        ImageDetailActivity.launch(this, ImageDetailActivity.ACTION_MODULE,view);
    }

    @Override
    public void OnRefreshState(boolean isRefreshing) {
        mSwipeRefresh.setRefreshing(isRefreshing);
    }
}
