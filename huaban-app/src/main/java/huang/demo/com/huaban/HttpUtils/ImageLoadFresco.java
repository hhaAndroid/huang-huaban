package huang.demo.com.huaban.HttpUtils;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.AutoRotateDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by LiCola on  2016/01/16  15:26
 * 用Fresco加载图片的类
 * 针对这个Demo已经配置很多默认的值
 * 用构造器模式便于设置更多形式
 * <p/>
 * 使用示例：
 * new ImageLoadFresco.LoadImageFrescoBuilder(mContext,img_image_user,url_head)
 * .setIsCircle(true)
 * .build();
 *
 * 核心类--作者进行了高度的订制
 */
public class ImageLoadFresco {
    private static final String TAG = "ImageLoadFresco";

    //必要参数
    private SimpleDraweeView mSimpleDraweeView;
    private Context mContext;


    /**
     * 私有化的构造函数 得到builder的参数 构造对象
     *  所有的配置主要就是配置C层和M层
     * @param frescoBuilder 构造器
     */
    private ImageLoadFresco(LoadImageFrescoBuilder frescoBuilder) {
        this.mContext = frescoBuilder.mContext;
        this.mSimpleDraweeView = frescoBuilder.mSimpleDraweeView;

        //初始化M层 用于初始化图片中包含的数据
        //自定义显示效果-核心类-http://www.fresco-cn.org/docs/using-drawees-code.html
        //这些属性也可以通过XML设置
        GenericDraweeHierarchyBuilder builderM=new GenericDraweeHierarchyBuilder(mContext.getResources());

        //请求参数 主要配置url 和C层相关
        //图片请求参数封装
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(frescoBuilder.mUrl))
                .setResizeOptions(frescoBuilder.mResizeOptions)
                .build();

        //初始化C层 用于控制图片的加载 是主要的实现控制类
        //ControllerBuilder:对加载显示的图片做更多的控制和定制
        PipelineDraweeControllerBuilder builderC = Fresco.newDraweeControllerBuilder();

        if (frescoBuilder.mUrlLow != null) {
            //一个imageview可以设置两个uri，一个是低分辨率的缩略图，一个是高分辨率的图
            builderC.setLowResImageRequest(ImageRequest.fromUri(frescoBuilder.mUrlLow));
        }

        builderC.setImageRequest(request);

        //模型层参数设置
        setViewPerformance(frescoBuilder, builderM, builderC);

        if (frescoBuilder.mControllerListener != null) {
            //监听图片下载事件
            builderC.setControllerListener(frescoBuilder.mControllerListener);
        }

        DraweeController draweeController=builderC.build();

        //订阅数据源
        if (frescoBuilder.mBitmapDataSubscriber!=null){
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            //获取已解码的图片
            DataSource<CloseableReference<CloseableImage>> dataSource =
                    imagePipeline.fetchDecodedImage(request, mSimpleDraweeView.getContext());
            //订阅数据源，才可以获取bitmap对象
            dataSource.subscribe(frescoBuilder.mBitmapDataSubscriber,CallerThreadExecutor.getInstance());
        }

        //必须的，才生效
        mSimpleDraweeView.setHierarchy(builderM.build());
        mSimpleDraweeView.setController(draweeController);
    }

    /**
     * 配置DraweeView的各种表现效果
     * 如 失败图 重试图 圆角或圆形
     * @param frescoBuilder
     * @param builderM
     * @param builderC
     */
    private void setViewPerformance(LoadImageFrescoBuilder frescoBuilder, GenericDraweeHierarchyBuilder builderM, PipelineDraweeControllerBuilder builderC) {

        //设置图片的缩放形式-centerCrop
        builderM.setActualImageScaleType(frescoBuilder.mActualImageScaleType);
        //下面意思是：如果mActualImageScaleType设置的是focusCrop，那么设置居中点为(0,0)
        //我们的项目没有这种情况
        if (frescoBuilder.mActualImageScaleType == ScalingUtils.ScaleType.FOCUS_CROP) {
            builderM.setActualImageFocusPoint(new PointF(0f, 0f));
        }

        if (frescoBuilder.mPlaceHolderImage != null) {
            //设置加载图片中的占位图
            builderM.setPlaceholderImage(frescoBuilder.mPlaceHolderImage, ScalingUtils.ScaleType.CENTER);
        }

        if (frescoBuilder.mProgressBarImage != null) {
            //设置加载中进度条，可以自定义
            //旋转周期是2s
            Drawable progressBarDrawable = new AutoRotateDrawable(frescoBuilder.mProgressBarImage, 2000);
            builderM.setProgressBarImage(progressBarDrawable);
        }

        //设置重试图 同时需要C层支持点击控制
        if (frescoBuilder.mRetryImage != null) {
            //点击图片重试
            builderC.setTapToRetryEnabled(true);
            builderM.setRetryImage(frescoBuilder.mRetryImage);
        }

        if (frescoBuilder.mFailureImage != null) {
            //加载失败时候图片
            builderM.setFailureImage(frescoBuilder.mFailureImage);
        }

        if (frescoBuilder.mBackgroundImage != null) {
            //背景图片
            builderM.setBackground(frescoBuilder.mBackgroundImage);
        }

        //如果显示为圆形
        if (frescoBuilder.mIsCircle) {

            if (frescoBuilder.mIsBorder) {
                //默认白色包边
                builderM.setRoundingParams(RoundingParams.asCircle().setBorder(0xFFFFFFFF, 2));
            }else {
                builderM.setRoundingParams(RoundingParams.asCircle());
            }
        }
        //如果显示为圆角
        //如果圆角取默认值10 或者是已经修改过的mRadius值
        if (frescoBuilder.mIsRadius) {
            builderM.setRoundingParams(RoundingParams.fromCornersRadius(frescoBuilder.mRadius));
        }

    }

    //构造器 作为类级内部类
    public static class LoadImageFrescoBuilder {
        //必要参数
        private Context mContext;
        private SimpleDraweeView mSimpleDraweeView;
        private String mUrl;

        //非必要参数
        private String mUrlLow;//低分率图地址

        private Drawable mPlaceHolderImage;//占位图
        private Drawable mProgressBarImage;//loading图
        private Drawable mRetryImage;//重试图
        private Drawable mFailureImage;//失败图
        private Drawable mBackgroundImage;//背景图

        //图片显示模式：center-crop
        private ScalingUtils.ScaleType mActualImageScaleType = ScalingUtils.ScaleType.CENTER_CROP;
        private boolean mIsCircle = false;//是否圆形图片
        private boolean mIsRadius = false;//是否圆角
        private boolean mIsBorder = false;//是否有包边
        private float mRadius = 10;//圆角度数 默认10
        private ResizeOptions mResizeOptions = new ResizeOptions(3000, 3000);//图片的大小限制

        private ControllerListener mControllerListener;//图片加载的回调

        private BaseBitmapDataSubscriber mBitmapDataSubscriber;//数据订阅，可以获得图片的bitmap对象
        /**
         * 构造器的构造方法 传入必要参数
         *
         * @param mContext
         * @param mSimpleDraweeView
         * @param mUrl
         */
        public LoadImageFrescoBuilder(Context mContext, SimpleDraweeView mSimpleDraweeView, String mUrl) {
            this.mContext = mContext;
            this.mSimpleDraweeView = mSimpleDraweeView;
            this.mUrl = mUrl;
        }

        /**
         * 构造器的build方法 构造真正的对象 并返回
         * 构造之前需要检查
         *
         * @return
         */
        public ImageLoadFresco build() {

            //不能同时设定 圆形圆角
            if (mIsCircle && mIsRadius) {
                throw new IllegalArgumentException("图片不能同时设置圆角和圆形");
            }

            return new ImageLoadFresco(this);
        }

        public LoadImageFrescoBuilder setBitmapDataSubscriber(BaseBitmapDataSubscriber mBitmapDataSubscriber){
            this.mBitmapDataSubscriber = mBitmapDataSubscriber;
            return this;
        }

        public LoadImageFrescoBuilder setUrlLow(String urlLow) {
            this.mUrlLow = urlLow;
            return this;
        }

        public LoadImageFrescoBuilder setActualImageScaleType(ScalingUtils.ScaleType mActualImageScaleType) {
            this.mActualImageScaleType = mActualImageScaleType;
            return this;
        }

        public LoadImageFrescoBuilder setPlaceHolderImage(Drawable mPlaceHolderImage) {
            this.mPlaceHolderImage = mPlaceHolderImage;
            return this;
        }

        public LoadImageFrescoBuilder setProgressBarImage(Drawable mProgressBarImage) {
            this.mProgressBarImage = mProgressBarImage;
            return this;
        }

        public LoadImageFrescoBuilder setRetryImage(Drawable mRetryImage) {
            this.mRetryImage = mRetryImage;
            return this;
        }

        public LoadImageFrescoBuilder setFailureImage(Drawable mFailureImage) {
            this.mFailureImage = mFailureImage;
            return this;
        }

        public LoadImageFrescoBuilder setBackgroundImage(Drawable mBackgroundImage) {
            this.mBackgroundImage = mBackgroundImage;
            return this;
        }

        public LoadImageFrescoBuilder setBackgroupImageColor(int colorId) {
            Drawable color = ContextCompat.getDrawable(mContext, colorId);
            this.mBackgroundImage = color;
            return this;
        }

        public LoadImageFrescoBuilder setIsCircle(boolean mIsCircle) {
            this.mIsCircle = mIsCircle;
            return this;
        }

        public LoadImageFrescoBuilder setIsCircle(boolean mIsCircle, boolean mIsBorder) {
            this.mIsBorder = mIsBorder;
            this.mIsCircle = mIsCircle;
            return this;
        }

        public LoadImageFrescoBuilder setIsRadius(boolean mIsRadius) {
            this.mIsRadius = mIsRadius;
            return this;
        }

        public LoadImageFrescoBuilder setIsRadius(boolean mIsRadius, float mRadius) {
            this.mRadius = mRadius;
            return setIsRadius(mIsRadius);
        }

        public LoadImageFrescoBuilder setRadius(float mRadius) {
            this.mRadius = mRadius;
            return this;
        }

        public LoadImageFrescoBuilder setResizeOptions(ResizeOptions mResizeOptions) {
            this.mResizeOptions = mResizeOptions;
            return this;
        }

        public LoadImageFrescoBuilder setControllerListener(ControllerListener mControllerListener) {
            this.mControllerListener = mControllerListener;
            return this;
        }

    }
}
