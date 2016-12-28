package huang.demo.com.huaban.Adapter;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import huang.demo.com.huaban.Entity.PinsMainEntity;
import huang.demo.com.huaban.HttpUtils.ImageLoadFresco;
import huang.demo.com.huaban.Util.CompatUtils;
import huang.demo.com.huaban.Base.BaseRecyclerAdapter;
import huang.demo.com.huabandemo.R;
import huang.demo.com.huaban.Util.Utils;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.ViewGroup.VISIBLE;


/**
 * @Title: RecyclerPinsHeadCardAdapter.java
 *
 * @Description: 不带头部和底部的适配器，核心类
 *
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-14 下午4:04.
 */
public class RecyclerPinsHeadCardAdapter extends BaseRecyclerAdapter<PinsMainEntity>  {

    private boolean mIsShowUser = false;//是否显示用户头像和名字的标志位

    private OnAdapterListener mListener;


    public interface OnAdapterListener {
        /**
         * 条目中的图片点击事件
         * @param bean
         * @param view
         */
        void onClickImage(PinsMainEntity bean, View view);

        /**
         * 图片下面的文字点击事件
         * @param bean
         * @param view
         */
        void onClickTitleInfo(PinsMainEntity bean, View view);

        /**
         * 图片下方，采集按钮的点击事件
         * @param bean
         * @param view
         */
        void onClickInfoGather(PinsMainEntity bean, View view);

        /**
         * 图片下方，喜欢按钮的点击事件
         * @param bean
         * @param view
         */
        void onClickInfoLike(PinsMainEntity bean, View view);

    }


    public RecyclerPinsHeadCardAdapter(RecyclerView mRecyclerView) {
        super(mRecyclerView);
    }



    //多一个标志位的 构造函数
    public RecyclerPinsHeadCardAdapter(RecyclerView recyclerView, boolean isShowUser) {
        this(recyclerView);
        this.mIsShowUser = isShowUser;
    }


    public void setOnClickItemListener(OnAdapterListener mListener) {
        this.mListener = mListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolderGeneral holder = null;//ViewHolder的子类


        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_item_image, parent, false);
        holder = new ViewHolderGeneral(view);//使用子类初始化ViewHolder

        //给文字设置左边图片，非常好
        holder.tv_card_like.setCompoundDrawablesWithIntrinsicBounds(
                CompatUtils.getTintListDrawable(mContext, R.drawable.ic_favorite_black_18dp, R.color.tint_list_grey),
                null,
                null,

                null);
        holder.tv_card_gather.setCompoundDrawablesWithIntrinsicBounds(
                CompatUtils.getTintListDrawable(mContext, R.drawable.ic_camera_black_18dp, R.color.tint_list_grey),
                null,
                null,
                null);


        //子类可以自动转型为父类
        return holder;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final PinsMainEntity bean = mList.get(position);

        //父类强制转换成子类 因为这个holder本来就是子类初始化的 所以可以强转
        ViewHolderGeneral viewHolder = (ViewHolderGeneral) holder;//强制类型转换 转成内部的ViewHolder

        onBindData(viewHolder, bean);
        onBindListener(viewHolder, bean);//初始化点击事件

    }


    private void onBindData(final ViewHolderGeneral holder, PinsMainEntity bean) {
        //检查图片信息--如果内容不够，那么就只显示图片，图片下面的文字不显示
        if (checkInfoContext(bean)) {
            holder.ll_title_info.setVisibility(VISIBLE);

            String title = bean.getRaw_text();//图片的文字描述
            int like = bean.getLike_count();//被喜欢数量
            int gather = bean.getRepin_count();//被转采的数量
            if (!TextUtils.isEmpty(title)) {
                holder.tv_card_title.setVisibility(VISIBLE);
                holder.tv_card_title.setText(title);
            } else {
                holder.tv_card_title.setVisibility(GONE);
            }
            holder.tv_card_like.setText(" " + like);
            holder.tv_card_gather.setText(" " + gather);
        } else {
            holder.ll_title_info.setVisibility(GONE);
        }


//        String url_img = mUrlFormat + bean.getFile().getKey()+"_fw320sf";
        //拼接字符串--图片的url
        String url_img = String.format(mUrlGeneralFormat, bean.getFile().getKey());

        if (Utils.checkIsGif(bean.getFile().getType())) {
            holder.ibtn_card_gif.setVisibility(VISIBLE);
        } else {
            holder.ibtn_card_gif.setVisibility(INVISIBLE);
        }

        //有可能返回的是长图，这里需要进行裁剪，确定宽、高比例
        float ratio = Utils.getAspectRatio(bean.getFile().getWidth(), bean.getFile().getHeight());
        //长图 "width":440,"height":5040,
        holder.img_card_image.setAspectRatio(ratio);//设置宽高比
        //获取加载中背景图片的矢量图
        Drawable dProgressImage =
                CompatUtils.getTintListDrawable(mContext, R.drawable.ic_toys_black_48dp, R.color.tint_list_pink);

        //功能非常强大，自动加载背景图片(风车)--而且是有动画效果
        new ImageLoadFresco.LoadImageFrescoBuilder(mContext, holder.img_card_image, url_img)
                .setProgressBarImage(dProgressImage)
                .build();
    }

    /**
     * 检查PinsMainEntity内容中的三项信息 任何一项不为空 都返回true
     *
     * @param bean
     * @return
     */
    private boolean checkInfoContext(PinsMainEntity bean) {

        String title = bean.getRaw_text();//图片的文字描述
        int like = bean.getLike_count();//被喜欢数量
        int gather = bean.getRepin_count();//被转采的数量

        if (!TextUtils.isEmpty(title)) {
            return true;
        } else if (like > 0 || gather > 0) {
            return true;
        }
        return false;
    }

    //条目的所有点击事件监听器
    private void onBindListener(ViewHolderGeneral holder, final PinsMainEntity bean) {

        //所有的点击事件响应全部在子类实现(TypeNewFragment)
        holder.rl_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mListener.onClickImage(bean, holder.img_card_image);
            }
        });

        holder.ll_title_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickTitleInfo(bean, v);
            }
        });

        holder.tv_card_gather.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickInfoGather(bean, v);
            }
        });

        holder.tv_card_like.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickInfoLike(bean, v);
//                    RxBus.getDefault().post(bean);
            }
        });

    }



    /**
     * 设置ibtn_card_gif的显示
     * 传true就显示暂停 传false显示播放
     *
     */
    private void setPlayDrawable(ViewHolderGeneral holder, boolean isRunning) {
        if (!isRunning) {
            Drawable drawable = holder.ibtn_card_gif.getResources().getDrawable(android.R.drawable.ic_media_play);
            holder.ibtn_card_gif.setImageDrawable(drawable);
        } else {
            Drawable drawable = holder.ibtn_card_gif.getResources().getDrawable(android.R.drawable.ic_media_pause);
            holder.ibtn_card_gif.setImageDrawable(drawable);
        }
    }

    private void setPlayListener(final ViewHolderGeneral holder, final Animatable animatable) {
        holder.ibtn_card_gif.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!animatable.isRunning()) {
                    animatable.start();
                    setPlayDrawable(holder, true);
                } else {
                    animatable.stop();
                    setPlayDrawable(holder, false);
                }
            }
        });

    }


    public static class ViewHolderGeneral extends RecyclerView.ViewHolder {
        //这个CardView采用两层操作
        public final View mView;

        public final FrameLayout rl_image;//第一层 包含图片和gif
        public final SimpleDraweeView img_card_image;
        public final ImageButton ibtn_card_gif;

        public final LinearLayout ll_title_info;//第二层 包含描述 图片信息
        public final TextView tv_card_title;//第二层 描述title

        public final LinearLayout ll_info;//第二层的子类 包含图片被采集和喜爱的信息
        public final TextView tv_card_gather;
        public final TextView tv_card_like;

        public ViewHolderGeneral(View view) {
            super(view);
            mView = view;
            //布局文件是card_image_item.xml
            rl_image = (FrameLayout) view.findViewById(R.id.framelayout_image);
            img_card_image = (SimpleDraweeView) view.findViewById(R.id.img_card_image);//主图
            ibtn_card_gif = (ImageButton) view.findViewById(R.id.ibtn_card_gif);

            ll_title_info = (LinearLayout) view.findViewById(R.id.linearlayout_title_info);//图片所有文字信息
            tv_card_title = (TextView) view.findViewById(R.id.tv_card_title);//描述的title

            ll_info = (LinearLayout) view.findViewById(R.id.linearlayout_info);//文字子类
            tv_card_gather = (TextView) view.findViewById(R.id.tv_card_gather);
            tv_card_like = (TextView) view.findViewById(R.id.tv_card_like);
        }

    }

}
