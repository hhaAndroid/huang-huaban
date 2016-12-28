package huang.demo.com.huaban.Module.Picture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import huang.demo.com.huabandemo.R;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PictureActivity  extends ToolbarActivity{
    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_IMAGE_TITLE = "image_title";
    public static final String TRANSIT_PIC = "transitionString";

    @BindView(R.id.picture)
    ImageView mImageView;

    PhotoViewAttacher mPhotoViewAttacher;
    String mImageUrl, mImageTitle;


    @Override protected int provideContentViewId() {
        return R.layout.activity_picture;
    }


    @Override public boolean canBack() {
        return true;
    }


    public static Intent newIntent(Context context, String url, String desc) {
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra(PictureActivity.EXTRA_IMAGE_URL, url);
        intent.putExtra(PictureActivity.EXTRA_IMAGE_TITLE, desc);
        return intent;
    }


    //获取传递过来的图片的url和图片的描述
    private void parseIntent() {
        mImageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        mImageTitle = getIntent().getStringExtra(EXTRA_IMAGE_TITLE);
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        parseIntent();
        //这样也可以，不一定需要在布局文件里面定义属性
        ViewCompat.setTransitionName(mImageView, TRANSIT_PIC);
        //填充图片
        Picasso.with(this).load(mImageUrl).into(mImageView);

//        Glide.with(this)
//                .load(VersionData.getOsDrawable(VersionData.getOsNum(mImageUrl)))
//                .fitCenter()
//                .into(mImageView);

        setAppBarAlpha(0.7f);//toolbar透明度
        setTitle(mImageTitle);//toolbar标题
        setupPhotoAttacher();
    }


    private void setupPhotoAttacher() {
        //PhotoView是第三方控件，非常强大
        //支持通过单点/多点触摸来进行图片缩放的智能控件
        //https://github.com/chrisbanes/PhotoView
        mPhotoViewAttacher = new PhotoViewAttacher(mImageView);
        //轻轻的点击事件-显示和隐藏toolbar
        mPhotoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                hideOrShowToolbar();
            }
        });
        // @formatter:off
        mPhotoViewAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                new AlertDialog.Builder(PictureActivity.this)
//                        .setMessage(getString(R.string.ask_saving_picture))
//                        .setNegativeButton(android.R.string.cancel,
//                                (dialog, which) -> dialog.dismiss())
//                        .setPositiveButton(android.R.string.ok,
//                                (dialog, which) -> {
//                                    saveImageToGallery();//保存图片
//                                    dialog.dismiss();
//                                })
//                        .show();
//                // @formatter:on
                return true;
            }
        });
    }


    //保存图片
    private void saveImageToGallery() {
        // @formatter:off
        //使用RX方式
//        Subscription s = RxMeizhi.saveImageAndGetPathObservable(this, mImageUrl, mImageTitle)
//            .observeOn(AndroidSchedulers.mainThread())//观察者运行在主线程
//            .subscribe(uri -> {
//                //只是简单的土司而已，保存操作也被封装了
//                File appDir = new File(Environment.getExternalStorageDirectory(), "Meizhi");
//                String msg = String.format(getString(R.string.picture_has_save_to),
//                        appDir.getAbsolutePath());
////                Toasts.showShort(msg);
//            }, error -> Toasts.showLong(error.getMessage() + "\n再试试..."));
//        // @formatter:on
//        addSubscription(s);//添加都管理器里面
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture, menu);
        // TODO: 把图片的一些信息，比如 who，加载到 Overflow 当中
        return true;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share://友盟分享
//                RxMeizhi.saveImageAndGetPathObservable(this, mImageUrl, mImageTitle)
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(uri -> Shares.shareImage(this, uri,
//                                getString(R.string.share_meizhi_to)),
//                                error -> Toasts.showLong(error.getMessage()));
//                return true;
            case R.id.action_save://保存
                saveImageToGallery();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override public void onResume() {
        super.onResume();
//        MobclickAgent.onResume(this);//友盟里面要求的
    }


    @Override public void onPause() {
        super.onPause();
//        MobclickAgent.onPause(this);//友盟里面要求的
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        mPhotoViewAttacher.cleanup();
    }
}
