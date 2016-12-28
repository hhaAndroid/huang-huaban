package huang.demo.com.huaban.Observable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import huang.demo.com.huaban.Util.Logger;
import rx.Observable;
import rx.Subscriber;

import static com.jakewharton.rxbinding.internal.Preconditions.checkUiThread;


public class AnimatorOnSubscribe implements Observable.OnSubscribe<Void> {
    final Animator animator;

    //构造器传入Animator
    public AnimatorOnSubscribe(Animator animator) {
        this.animator = animator;
    }

    //Subscriber这个就是订阅者/观察者
    @Override
    public void call(final Subscriber<? super Void> subscriber) {
        checkUiThread();//检查运行线程
        //动画执行的监听器
        AnimatorListenerAdapter adapter=new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                subscriber.onNext(null);//发送信息流，发送消息类型是void
                Logger.d("onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                subscriber.onCompleted();//发送信息流完成，然后消息流会传递给订阅者
                Logger.d("onAnimationEnd");
            }
        };

        animator.addListener(adapter);//不能少
        animator.start();//先绑定监听器再开始
    }
}
