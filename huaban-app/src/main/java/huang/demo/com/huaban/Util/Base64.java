package huang.demo.com.huaban.Util;

/**
 * Created by LiCola on  2015/12/16  14:27
 * Https 登录使用的 base64 加密类
 */
public class Base64 {
    private static final String mBasic="Basic ";
    //通过反编译源代码，在APIBase.java的setup()发现mClientInfo就是appid+Secret经过base64加密的值
    //解码后：appid(APP KEY)=1d912cae47144fa09d88， Secret(App Secret)=f94fcc09b59b4349a148a203ab2f20c7
    private static final String mClientInfo = "MWQ5MTJjYWU0NzE0NGZhMDlkODg6Zjk0ZmNjMDliNTliNDM0OWExNDhhMjAzYWIyZjIwYzc=";
    public static final String mClientInto=mBasic+mClientInfo;

}
