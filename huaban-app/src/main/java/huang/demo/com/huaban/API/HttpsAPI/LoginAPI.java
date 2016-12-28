package huang.demo.com.huaban.API.HttpsAPI;

import huang.demo.com.huaban.Module.Login.UserMeAndOtherBean;
import huang.demo.com.huaban.Module.Login.TokenBean;
import huang.demo.com.huaban.Util.Constant;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import rx.Observable;


/**
 * @Title: LoginAPI.java
 *
 * @Description: 登录操作流程：1.使用https，申请的帐号、密码进行授权，授权成功返回临时令牌token
 *  2.使用得到的令牌，再次使用https登录，此时不需要输入帐号密码
 *
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-5 下午3:10.
 */
public interface LoginAPI {
    /**
     * 注意：如果想完全理解作者所有请求网络和登录操作的过程，首先需要了解第三方登录协议：OAuth2.0
     *
     * 很明显，作者这里使用获取令牌的方式是：四种授权模式中的密码模式
     * 该模式的特点：用户向客户端提供自己的用户名和密码。客户端使用这些信息，向"服务商提供商"索要授权。
        （A）用户向客户端提供用户名和密码。
        （B）客户端将用户名和密码发给认证服务器，向后者请求令牌。
        （C）认证服务器确认无误后，向客户端提供访问令牌。
       具体见博客：http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html
     */



    //https 用户登录  的第一步  @Field是表单项
    // Authorization 授权信息，报头一个固定的值, 内容 grant_type=password&password=密码&username=账号
    //获取token  必须添加Authorization请求头部信息

    /**密码授权认证流程：
     1.客户端发送给认证服务器的post请求内容
         POST /token HTTP/1.1
         Host: server.example.com
         Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
         Content-Type: application/x-www-form-urlencoded
         grant_type=password&username=johndoe&password=A3ddj3w

     2.认证服务器返回给客户端的内容：授权码
         "access_token":"2YotnFZFEjr1zCsicMWpAA",
         "token_type":"example",
         "expires_in":3600,
         "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
         "example_parameter":"example_value"

    3. 如果用户访问的时候，客户端的"访问令牌"已经过期，则需要使用"更新令牌"申请一个新的访问令牌
       此时客户端应该向认证服务器post请求获取新的token:
         POST /token HTTP/1.1
         Host: server.example.com
         Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
         Content-Type: application/x-www-form-urlencoded
         grant_type=refresh_token&refresh_token=tGzv3JOkF0XG5Qx2TlKWIA
     */


    @FormUrlEncoded
    @POST("https://huaban.com/oauth/access_token/")
    Observable<TokenBean> httpsTokenRx(@Header(Constant.Authorization) String authorization, @Field("grant_type") String grant,
                                       @Field("username") String username, @Field("password") String password);

    //登录第二步 用上一步结果联网，获取用户信息
    @GET("users/me")
    Observable<UserMeAndOtherBean> httpsUserRx(@Header(Constant.Authorization) String authorization);
}
