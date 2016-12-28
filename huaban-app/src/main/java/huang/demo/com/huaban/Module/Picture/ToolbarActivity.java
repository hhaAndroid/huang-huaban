
package huang.demo.com.huaban.Module.Picture;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import huang.demo.com.huabandemo.R;


public abstract class ToolbarActivity extends BaseActivity {

    /**
     * 提供布局
     * @return 布局id
     */
    abstract protected int provideContentViewId();


    public void onToolbarClick() {}


    protected AppBarLayout mAppBar;
    protected Toolbar mToolbar;
    protected boolean mIsHidden = false;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(provideContentViewId());
        //是一个容器， R.id.app_bar_layout在view_toolbar.xml里面
        mAppBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
        //Toolbar用于取代Actionbar，这里toolbar是mAppBar的子view
        mToolbar = (Toolbar) findViewById(R.id.toolbar);//在view_toolbar.xml里面
        if (mToolbar == null || mAppBar == null) {
            throw new IllegalStateException(
                    "The subclass of ToolbarActivity must contain a toolbar.");
        }

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToolbarClick();
            }
        });

        setSupportActionBar(mToolbar);//设置toolbar充当actionbar功能

        if (canBack()) {//默认是false，表示使用actionbar
            ActionBar actionBar = getSupportActionBar();
            //显示返回键
            if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            mAppBar.setElevation(10.6f);
        }
    }


    public boolean canBack() {
        return false;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();//回退，模拟按下了返回键
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    protected void setAppBarAlpha(float alpha) {
        mAppBar.setAlpha(alpha);
    }


    protected void hideOrShowToolbar() {
        mAppBar.animate()
               .translationY(mIsHidden ? 0 : -mAppBar.getHeight())
               .setInterpolator(new DecelerateInterpolator(2))
               .start();
        mIsHidden = !mIsHidden;
    }
}
