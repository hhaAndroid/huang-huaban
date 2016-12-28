package huang.demo.com.huaban.Util;

import android.text.TextUtils;

/**
 * @Title: Logger.java
 *
 * @Description: 日志打印工具类
 *
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-2 下午8:51.
 */
public final class Logger {

    private static final String TAG = "Demo";

    /**
     * Set true or false if you want read logs or not
     */
    private static boolean logEnabled_d = true;
    private static boolean logEnabled_i = true;
    private static boolean logEnabled_e = true;

    public static void d() {
        if (logEnabled_d) {
            android.util.Log.d(TAG, getLocation());
        }
    }

    public static void d(String msg) {
        if (logEnabled_d) {
            android.util.Log.d(TAG, getLocation() + msg);
        }
    }

    public static void i(String msg) {
        if (logEnabled_i) {
            android.util.Log.i(TAG, getLocation() + msg);
        }
    }

    public static void i() {
        if (logEnabled_i) {
            android.util.Log.i(TAG, getLocation());
        }
    }

    public static void e(String msg) {
        if (logEnabled_e) {
            android.util.Log.e(TAG, getLocation() + msg);
        }
    }

    public static void e(String msg, Throwable e) {
        if (logEnabled_e) {
            android.util.Log.e(TAG, getLocation() + msg, e);
        }
    }

    public static void e(Throwable e) {
        if (logEnabled_e) {
            android.util.Log.e(TAG, getLocation(), e);
        }
    }

    public static void e() {
        if (logEnabled_e) {
            android.util.Log.e(TAG, getLocation());
        }
    }

    private static String getLocation() {
        final String className = Logger.class.getName();
        //代表该线程的堆栈转储堆栈跟踪元素的数组
        final StackTraceElement[] traces = Thread.currentThread()
                .getStackTrace();

        boolean found = false;
        //截取堆栈元素里面的关于Logger的信息，其实traces这个对象里面保存了很多当前运行信息
        //包括当前正在运行哪个方法、运行的这个方法属于哪个类，常用于获取当前正在运行的方法和类
        for (StackTraceElement trace : traces) {
            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + ":"
                                + trace.getMethodName() + ":"
                                + trace.getLineNumber() + "]: ";
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        return "[]: ";
    }
    //写的非常正规
    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

}
