package huang.demo.com.huaban.HttpUtils;

import com.google.gson.Gson;

import huang.demo.com.huaban.HttpUtils.Converter.AvatarConverter;
import huang.demo.com.huaban.API.OnProgressResponseListener;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;


/**
 * @Title: RetrofitClient.java
 * @Description: 对Retrofit网络请求进行简单封装
 * @Company:南京航空航天大学
 * @author：黄海安
 * @date： 16-7-4 下午8:46.
 */
public class RetrofitClient {
    /**
     * Retrofit的使用就是以下几步：
     * 1.定义接口，参数声明，Url都通过Annotation指定:LoginAPI
     * 2.通过RestAdapter生成一个接口的实现类(动态代理): retrofit.create
     * 3.调用接口请求数据:httpsTokenRx
     */

    /**
     * Retrofit 是一个 RESTful 的 HTTP 网络请求框架的封装。注意这里并没有说它是网络请求框架，
     * 主要原因在于网络请求的工作并不是 Retrofit 来完成的。
     * Retrofit 2.0 开始内置 OkHttp，前者专注于接口的封装，后者专注于网络请求的高效，二者分工协作
     */

    //所有的联网地址 统一成https
    public final static String mBaseUrl = "https://api.huaban.com/";

    public static Gson gson = new Gson();

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(mBaseUrl) //如果LoginAPI里面的地址包含了https,那么这行无效
            //RxJavaCallAdapterFactory这个解析类是官方封装好的，直接使用
            //增加这行，返回的数据就可以以RXjava方式操作了 call->Rxjava
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {

        Retrofit retrofit = builder
                .client(OkHttpHelper.addLogClient(httpClient).build())
                .addConverterFactory(AvatarConverter.create(gson))//自定义Converter 解析数据的类
                .build();
        //传入一个接口serviceClass，会返回该接口的实现类S
        return retrofit.create(serviceClass); //动态代理设计模式
    }

    public static <S> S createService(Class<S> serviceClass, OnProgressResponseListener listener) {
        Retrofit retrofit = builder
                .client(OkHttpHelper.addProgressClient(httpClient, listener).build())
                .build();
        return retrofit.create(serviceClass);
    }


}
