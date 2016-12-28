package huang.demo.com.huaban.Module.Type;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import org.greenrobot.eventbus.EventBus;

import huang.demo.com.huaban.API.Fragment.OnPinsFragmentInteractionListener;
import huang.demo.com.huaban.API.Fragment.OnRefreshFragmentInteractionListener;
import huang.demo.com.huaban.API.HttpsAPI.TypeAPI;
import huang.demo.com.huaban.Adapter.RecyclerPinsHeadCardAdapter;
import huang.demo.com.huaban.Base.BaseRecyclerHeadFragment;
import huang.demo.com.huaban.Entity.ListPinsBean;
import huang.demo.com.huaban.Entity.PinsMainEntity;
import huang.demo.com.huaban.HttpUtils.RetrofitClient;
import huang.demo.com.huaban.Module.Main.MainActivity;
import huang.demo.com.huaban.Util.Logger;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @Title: TypeNewFragment.java
 *
 * @Description: TypeNewFragment是BaseRecyclerHeadFragment的子类
 *  父类定义模版，子类实现模版里面的方法即可
 *
 * 要理解这里面的方法调用，则必须要看父类模版BaseRecyclerHeadFragment
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-14 下午3:25.
 */
public class TypeNewFragment
        extends BaseRecyclerHeadFragment<RecyclerPinsHeadCardAdapter, List<PinsMainEntity>> {
    private static final String TAG = "TypeNewFragment";

    private int mMaxId = 0;

    //多定义的字段 一共两个 另一个在父类中继承得到
    protected static final String TYPE_TITLE = "TYPE_TITLE";
    protected String mTitle;

    //两个与MainActivity 交互接口
    private OnPinsFragmentInteractionListener mListener;
    private OnRefreshFragmentInteractionListener mRefreshListener;

    @Override
    protected String getTAG() {
        return this.toString();
    }

    //外部调用
    public static TypeNewFragment newInstance(String type, String title) {
        TypeNewFragment fragment = new TypeNewFragment();
        Bundle args = new Bundle();
        args.putString(TYPE_KEY, type);
        args.putString(TYPE_TITLE, title);
        fragment.setArguments(args);//将数据保存起来
        return fragment;
    }

    public TypeNewFragment(){

    }

    //取出保存的数据--模版里面的方法(BaseRecyclerHeadFragment)
    @Override
    protected void getBundleData(Bundle args) {
        if (args != null) {
            mKey = args.getString(TYPE_KEY);//这个字段在父类中
            mTitle = args.getString(TYPE_TITLE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //当下拉时候，会进入下列刷新，会调用这个方法，相当于重新请求数据
    //模版方法(BaseRecyclerHeadFragment)
    @Override
    protected Subscription getHttpFirst() {
        return RetrofitClient.createService(TypeAPI.class)
                .httpsTypeLimitRx(mAuthorization, mKey, mLimit)
                .map(new Func1<ListPinsBean, List<PinsMainEntity>>() {
                    @Override
                    public List<PinsMainEntity> call(ListPinsBean listPinsBean) {
                        //在何处进行了setPins()？
                        return listPinsBean.getPins();
                    }
                })
                .subscribeOn(Schedulers.io())//发布者的运行线程 联网操作属于IO操作
                .observeOn(AndroidSchedulers.mainThread())//订阅者的运行线程 在main线程中才能修改UI
                .subscribe(new Subscriber<List<PinsMainEntity>>() {
                    @Override
                    public void onCompleted() {

                        mRefreshListener.OnRefreshState(false);//MainActivity实现
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                        mRefreshListener.OnRefreshState(false);//MainActivity实现
                        checkException(e);
                    }

                    @Override
                    public void onNext(List<PinsMainEntity> result) {
                        //保存maxId值 后续加载需要
                        mMaxId = getMaxId(result);
                        //设置适配器
                        mAdapter.setListNotify(result);

//                        for (PinsMainEntity pin: result) {
//                            Logger.e(pin.toString());
//                        }
                    }
                });


    }


    /**
     * 从返回联网结果中保存max值 用于下次联网的关键
     *
     * @param result
     * @return
     */
    private int getMaxId(List<PinsMainEntity> result) {
        return result.get(result.size() - 1).getPin_id();
    }

    //当下滑到底部的80%条目时候，说明马上到底部了，需要分页加载
    @Override
    protected Subscription getHttpScroll() {
        return RetrofitClient.createService(TypeAPI.class)
                .httpsTypeMaxLimitRx(mAuthorization, mKey, mMaxId, mLimit)
                .map(new Func1<ListPinsBean, List<PinsMainEntity>>() {
                    @Override
                    public List<PinsMainEntity> call(ListPinsBean listPinsBean) {
                        //取出list对象
                        return listPinsBean.getPins();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<PinsMainEntity>>() {
                    @Override
                    public void onCompleted() {
                        Logger.d();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                        checkException(e);//检查错误 弹出提示
                    }

                    @Override
                    public void onNext(List<PinsMainEntity> pinsEntities) {
                        Logger.d();
                        mMaxId = getMaxId(pinsEntities);
                        mAdapter.addListNotify(pinsEntities);
                    }
                });
    }


    //主要用途是获取一些上下文，和全局参数
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d(context.toString());
        if ((context instanceof OnRefreshFragmentInteractionListener)
                && (context instanceof OnPinsFragmentInteractionListener)) {
            mListener = (OnPinsFragmentInteractionListener) context;
            mRefreshListener = (OnRefreshFragmentInteractionListener) context;
        } else {
            throwRuntimeException(context);
        }

        if (context instanceof MainActivity) {
            mAuthorization = ((MainActivity) context).mAuthorization;
        } else if (context instanceof TypeActivity) {
            mAuthorization = ((TypeActivity) context).mAuthorization;
        }
    }

    //adapter的监听器
    @Override
    protected void initListener() {

        //条目点击事件--父类方法
        mAdapter.setOnClickItemListener(new RecyclerPinsHeadCardAdapter.OnAdapterListener() {
            @Override
            public void onClickImage(PinsMainEntity bean, View view) {
                Logger.d();
                EventBus.getDefault().postSticky(bean);//使用事件总线发射出了bean对象实体
               //由于mListener本质上是mainActivity，故会调用mainActivity里面的onClickPinsItemImage
                mListener.onClickPinsItemImage(bean, view);
            }

            @Override
            public void onClickTitleInfo(PinsMainEntity bean, View view) {
                Logger.d();
                EventBus.getDefault().postSticky(bean);
                mListener.onClickPinsItemText(bean, view);
            }

            @Override
            public void onClickInfoGather(PinsMainEntity bean, View view) {
                Logger.d();
            }

            @Override
            public void onClickInfoLike(PinsMainEntity bean, View view) {
                Logger.d(bean.toString());
            }

        });
    }

    @Override
    protected View getHeadView() {
        return null;
    }//没有头布局


    @Override
    protected int getAdapterPosition() {
        return mAdapter.getAdapterPosition();
    }

    //设置适配器
    @Override
    protected RecyclerPinsHeadCardAdapter setAdapter() {
        //不带头部和底部的适配器，是本应用的核心类
        return new RecyclerPinsHeadCardAdapter(mRecyclerView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter=null;
        mListener=null;
    }
}
