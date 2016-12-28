package huang.demo.com.huaban.Module.SearchResult;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import huang.demo.com.huaban.API.HttpsAPI.SearchAPI;
import huang.demo.com.huaban.Base.BaseRecyclerHeadFragment;
import huang.demo.com.huaban.HttpUtils.RetrofitClient;
import huang.demo.com.huaban.Observable.ErrorHelper;
import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.Adapter.RecyclerPeopleAdapter;
import huang.demo.com.huaban.API.Fragment.OnPeopleFragmentInteraction;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by LiCola on  2016/04/05  22:09
 */
public class ResultPeopleFragment extends
        BaseRecyclerHeadFragment<RecyclerPeopleAdapter, List<SearchPeopleListBean.UsersBean>> {

    private int mIndex = 1;//联网的起始页 默认1

    private OnPeopleFragmentInteraction<SearchPeopleListBean.UsersBean> mListener;

    @Override
    protected String getTAG() {
        return this.toString();
    }


    //只需要一个Key作为关键字联网
    public static ResultPeopleFragment newInstance(String key) {
        ResultPeopleFragment fragment = new ResultPeopleFragment();
        Bundle args = new Bundle();
        args.putString(TYPE_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Subscription getHttpFirst() {
        return RetrofitClient.createService(SearchAPI.class)
                .httpsPeopleSearchRx(mAuthorization,mKey,mIndex,mLimit)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<SearchPeopleListBean, Observable<SearchPeopleListBean>>() {
                    @Override
                    public Observable<SearchPeopleListBean> call(SearchPeopleListBean searchPeopleListBean) {
                        return ErrorHelper.getCheckNetError(searchPeopleListBean);
                    }
                })
                .map(SearchPeopleListBean::getUsers)
                .filter(getFilterFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SearchPeopleListBean.UsersBean>>() {
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
                    public void onNext(List<SearchPeopleListBean.UsersBean> usersBeen) {
                        mAdapter.addListNotify(usersBeen);
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
        if (context instanceof OnPeopleFragmentInteraction) {
            mListener = (OnPeopleFragmentInteraction<SearchPeopleListBean.UsersBean>) context;
        } else {
            throwRuntimeException(context);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        mAdapter.setOnClickItemListener(new RecyclerPeopleAdapter.OnAdapterListener() {
            @Override
            public void onClickUser(SearchPeopleListBean.UsersBean bean, View view) {
                mListener.onClickItemUser(bean, view);
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
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    @Override
    protected RecyclerPeopleAdapter setAdapter() {
        return new RecyclerPeopleAdapter(mRecyclerView);
    }


}
