package huang.demo.com.huaban.Base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import huang.demo.com.huaban.Widget.MyRecyclerview.RecyclerViewUtils;
import huang.demo.com.huabandemo.R;

/**
 * Created by LiCola on  2016/04/07  19:30
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter   {
    protected final String life = "AdapterLife";
    protected RecyclerView mRecyclerView;
    protected Context mContext;
    protected List<T> mList =new ArrayList<>(20);
    protected int mAdapterPosition = 0;
    protected final String mUrlSmallFormat;//小图地址
    protected final String mUrlGeneralFormat;//普通地址
    protected final String mUrlBigFormat;//大图地址
    
    public List<T> getList() {
        return mList;
    }

    /**
     * 请求以前数据，并重新添加数据和notifyDataSetChanged---重新加载
     * @param mList
     */
    public void setListNotify(List<T> mList) {
        this.mList.clear();
        this.mList=mList;
        notifyDataSetChanged();
    }

    /**
     * 在原来数据基础上添加更多数据---加载更多
     * @param mList
     */
    public void addListNotify(List<T> mList){
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    //已经经过了去除头部布局的操作
    public int getAdapterPosition() {
        return mAdapterPosition;
    }


    public BaseRecyclerAdapter(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
        this.mContext=mRecyclerView.getContext();
        this.mUrlSmallFormat = mContext.getResources().getString(R.string.url_image_small);
        this.mUrlGeneralFormat = mContext.getResources().getString(R.string.url_image_general);
        this.mUrlBigFormat = mContext.getResources().getString(R.string.url_image_big);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        //已经去掉了添加的头部和底部，封装性较好
        //返回当前的holder，客户端不用考虑头部
        mAdapterPosition = RecyclerViewUtils.getAdapterPosition(mRecyclerView, holder);
    }

}
