package huang.demo.com.huaban.Observable;

import android.support.annotation.NonNull;

import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.Entity.ErrorBaseBean;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by LiCola on  2016/05/31  17:04
 */

public class ErrorHelper {

    private ErrorHelper() {

    }

    @NonNull
    public static <T extends ErrorBaseBean> Observable<T> getCheckNetError(final T bean) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                if (bean != null){
                    String msg = bean.getMsg();
                    if (msg != null) {
                        Logger.d("onError=" + msg);

                        subscriber.onError(new RuntimeException(bean.getMsg()));
                    } else {
                        Logger.d("onNext");
                        subscriber.onNext(bean);
                    }
                }else {
                    subscriber.onError(new RuntimeException());
                }

            }
        });
    }

}
