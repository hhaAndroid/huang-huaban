package huang.demo.com.huaban.HttpUtils.Converter;

import com.google.gson.Gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.*;
import retrofit2.Converter;

/**
 * @Title: AvatarConverter.java
 *
 * @Description: 自定义转换器--参见http://blog.csdn.net/zr940326/article/details/51549310
 *
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-30 下午8:17.
 */
public class AvatarConverter extends retrofit2.Converter.Factory  {
    private static final String TAG = "AvatarConverter";


    public static AvatarConverter create() {
        return create(new Gson());
    }


    public static AvatarConverter create(Gson gson) {
        return new AvatarConverter(gson);
    }

    private final Gson gson;

    private AvatarConverter(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new AvatarResponseBodyConverter<>(gson,type);//响应
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        //请求--这里只自定义响应体，不修改请求体转换器
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }

}
