package huang.demo.com.huaban.Observable;

import android.animation.Animator;

import huang.demo.com.huaban.Util.Utils;
import rx.Observable;

/**
 * Created by LiCola on  2016/04/10  15:49
 */
public class MyRxObservable {
    //动画数据流 添加后会自动开始
    public static Observable<Void> add(Animator animator){
        Utils.checkNotNull(animator,"Animation is null");
        return Observable.create(new AnimatorOnSubscribe(animator));
    }
}
