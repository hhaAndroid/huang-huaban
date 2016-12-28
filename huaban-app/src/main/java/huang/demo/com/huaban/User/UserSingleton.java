package huang.demo.com.huaban.User;

import android.app.Application;

import huang.demo.com.huaban.Util.SPUtils;
import huang.demo.com.huaban.Util.Constant;

/**
 * @Title: UserSingleton.java
 *
 * @Description:
 *
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-2 下午8:37.
 */
public class UserSingleton {

    private String mAuthorization;

    private Boolean isLogin;

    private volatile static UserSingleton instance=new UserSingleton();

    private UserSingleton(){

    }

    public static UserSingleton getInstance(){
        return instance;
    }

    public String getAuthorization() {
        if (isLogin){
            if (mAuthorization==null){

            }
            return mAuthorization;
        }

        return mAuthorization;
    }

    public void setAuthorization(String mAuthorization) {

        this.mAuthorization = mAuthorization;
    }

    public boolean isLogin(Application mContext) {
        if (isLogin==null){
            //通过KEY，从SP里面获取相应的值
            isLogin = (boolean) SPUtils.get(mContext, Constant.ISLOGIN, false);
        }
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }
}
