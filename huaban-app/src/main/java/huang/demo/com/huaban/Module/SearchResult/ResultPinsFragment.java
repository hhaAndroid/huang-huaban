package huang.demo.com.huaban.Module.SearchResult;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.List;
import org.greenrobot.eventbus.EventBus;

import huang.demo.com.huaban.API.Fragment.OnPinsFragmentInteractionListener;
import huang.demo.com.huaban.API.HttpsAPI.SearchAPI;
import huang.demo.com.huaban.Adapter.RecyclerPinsHeadCardAdapter;
import huang.demo.com.huaban.Base.BaseRecyclerHeadFragment;
import huang.demo.com.huaban.Entity.PinsMainEntity;
import huang.demo.com.huaban.HttpUtils.RetrofitClient;
import huang.demo.com.huaban.Observable.ErrorHelper;
import huang.demo.com.huaban.Util.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by LiCola on  2016/04/05  16:48
 */
public class ResultPinsFragment extends BaseRecyclerHeadFragment<RecyclerPinsHeadCardAdapter, List<PinsMainEntity>> {
    private static final String TAG = "ResultPinsFragment";

    private int mIndex = 1;//联网的起始页 默认1


    private OnPinsFragmentInteractionListener mListener;

    @Override
    protected String getTAG() {
        return this.toString();
    }

    //只需要一个Key作为关键字联网
    public static ResultPinsFragment newInstance(String key) {
        ResultPinsFragment fragment = new ResultPinsFragment();
        Bundle args = new Bundle();
        args.putString(TYPE_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Subscription getHttpFirst() {
        return RetrofitClient.createService(SearchAPI.class)
                .httpsImageSearchRx(mAuthorization,mKey, mIndex, mLimit)
                .flatMap(new Func1<SearchImageBean, Observable<SearchImageBean>>() {
                    @Override
                    public Observable<SearchImageBean> call(SearchImageBean searchImageBean) {
                        return ErrorHelper.getCheckNetError(searchImageBean);
                    }
                })
                .map(new Func1<SearchImageBean, List<PinsMainEntity>>() {
                    @Override
                    public List<PinsMainEntity> call(SearchImageBean searchImageBean) {
                        return searchImageBean.getPins();//取出list对象
                    }
                })
                .filter(getFilterFunc1())
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
                        checkException(e);

                    }

                    @Override
                    public void onNext(List<PinsMainEntity> pinsAndUserEntities) {
                        Logger.d();
                        mAdapter.addListNotify(pinsAndUserEntities);
                        mIndex++;
                    }
                });
    }

    @Override
    protected Subscription getHttpScroll() {
        return getHttpFirst();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPinsFragmentInteractionListener) {
            mListener = (OnPinsFragmentInteractionListener) context;
        } else {
            throwRuntimeException(context);
        }

        if (context instanceof SearchResultActivity){
            mAuthorization=((SearchResultActivity) context).mAuthorization;
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        mAdapter.setOnClickItemListener(new RecyclerPinsHeadCardAdapter.OnAdapterListener() {
            @Override
            public void onClickImage(PinsMainEntity bean, View view) {
                EventBus.getDefault().postSticky(bean);
                mListener.onClickPinsItemImage(bean, view);
            }

            @Override
            public void onClickTitleInfo(PinsMainEntity bean, View view) {
                EventBus.getDefault().postSticky(bean);
                mListener.onClickPinsItemText(bean, view);
            }

            @Override
            public void onClickInfoGather(PinsMainEntity bean, View view) {
                Logger.d();
            }

            @Override
            public void onClickInfoLike(PinsMainEntity bean, View view) {
                Logger.d();
            }
        });
    }

    @Override
    protected View getHeadView() {
        return null;
    }

    @Override
    protected int getAdapterPosition() {
        return mAdapter.getAdapterPosition();
    }

    @Override
    protected RecyclerPinsHeadCardAdapter setAdapter() {
        return new RecyclerPinsHeadCardAdapter(mRecyclerView);
    }

}
