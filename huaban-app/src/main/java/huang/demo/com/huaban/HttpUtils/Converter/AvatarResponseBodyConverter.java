package huang.demo.com.huaban.HttpUtils.Converter;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.Util.Utils;
import okhttp3.ResponseBody;

/**
 * @Title: AvatarResponseBodyConverter.java
 *
 * @Description: 自定义转换器中的响应体转换器
 *
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-30 下午8:33.
 */
public class AvatarResponseBodyConverter<T> implements retrofit2.Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;

    private String mSKey = "\"key\":\"([^\"]*)\"";
    private Pattern mPkey = Pattern.compile(mSKey);//正则截取avatar数据里面的key字段

    public AvatarResponseBodyConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
        // Logger.d();
    }

    //真正的解析数据方法--核心，自动回调
    @Override
    public T convert(ResponseBody value) throws IOException {

        //返回的就是没有解析的Json数据
//      Logger.e("服务器返回的内容："+new String(value.bytes()));

        Reader reader = value.charStream();//取出字节流
        String result;
        try {
            BufferedReader in = new BufferedReader(reader);//读取
            StringBuffer buffer = new StringBuffer();//构造buffer对象用于拼接
            String line;
            while ((line = in.readLine()) != null) {//读行
                if (Thread.interrupted()) {
                    break;
                }
                buffer.append(line);//写入buffer
            }
            result = buffer.toString();
        } catch (InterruptedIOException e) {
            Logger.d(e.toString());
            result = "{}";
        } finally {
            Utils.closeQuietly(reader);//记得关闭流
        }
        //对json数据串进行处理，并返回处理后的json，fromjson并且解析为对象
        //type就是TypeAPI.class
        return gson.fromJson(regexChange(result), type);//返回解析后的对象
    }

    /**
     * 对输入的字符串进行处理
     *
     * @param input 传入的需要处理的字符串
     * @return
     */
    private String regexChange(String input) {
        String result = input;//是原始JSON数据
        //匹配规则是当avatar是{}包装的对象

        if (!TextUtils.isEmpty(result)&&(!"{}".equals(result))) {
            Pattern mPAvatar = Pattern.compile("\"avatar\":\\{([^\\}]*)\\}");
            Matcher mMAvatar = mPAvatar.matcher(result);
            while (mMAvatar.find()) {//如果找到 开始替换
                result = result.replaceFirst("\"avatar\":\\{([^\\}]*)\\}", getKey(mMAvatar.group()));
            }
        }
//        Logger.e("正则解析后json数据："+result);
        //注意：如果直接使用GSON解析数据成PinsMainEntity对象，那是不可以的
        //因为作者写的PinsMainEntity和返回的数据没有一一对应
        //所以作者自定义了转换器，将JSON流转换为PinsMainEntity结构，然后可以直接GSON解析了
        //纯粹是无聊...........
        return result;//正则后就是正好符合PinsMainEntity结构，然后采用GSON解析就正好OK了
    }

    /**
     * 取出关键值返回
     * 取出key值 统一拼接成 avatar:"key" 作为String返回
     *
     * @param group
     * @return
     */
    private String getKey(String group) {
        Matcher matcher = mPkey.matcher(group);
        StringBuffer buffer = new StringBuffer();
//            Logger.d(TAG);
//            buffer.append("\"avatar\":\"http://img.hb.aicdn.com/");
        buffer.append("\"avatar\":\"");//替换成不带http头的avatar
        while (matcher.find()) {
            buffer.append(matcher.group(1));
        }
        buffer.append("\"");//添加 " 做最后一个字符 完成拼接
        return buffer.toString();
    }
}
