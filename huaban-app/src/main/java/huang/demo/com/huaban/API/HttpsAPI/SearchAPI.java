package huang.demo.com.huaban.API.HttpsAPI;

import huang.demo.com.huaban.Module.Search.SearchHintBean;
import huang.demo.com.huaban.Module.SearchResult.SearchPeopleListBean;
import huang.demo.com.huaban.Util.Constant;
import huang.demo.com.huaban.Module.SearchResult.SearchBoardListBean;
import huang.demo.com.huaban.Module.SearchResult.SearchImageBean;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by LiCola on  2016/05/23  20:56
 * 所有和搜索有关的接口
 */

public interface SearchAPI {
    //https//api.huaban.com/search/hint?q=%E4%BA%BA
    //搜索关键字 提示
    @GET("search/hint")
    Observable<SearchHintBean> httpsSearHintBean(@Header(Constant.Authorization) String authorization, @Query("q") String key);

    //https://api.huaban.com/search/?q=%E7%BE%8E%E9%A3%9F&page=1&per_page=2
    //图片搜索 返回结果跟模板类型差不多
    @GET("search/")
    Observable<SearchImageBean> httpsImageSearchRx(@Header(Constant.Authorization) String authorization, @Query("q") String key, @Query("page") int page, @Query("per_page") int per_page);

    //https://api.huaban.com/search/boards/?q=%E7%BE%8E%E9%A3%9F&page=1&per_page=1
    //画板搜索
    @GET("search/boards/")
    Observable<SearchBoardListBean> httpsBoardSearchRx(@Header(Constant.Authorization) String authorization, @Query("q") String key, @Query("page") int page, @Query("per_page") int per_page);

    //https://api.huaban.com/search/people/?q=%E7%BE%8E%E9%A3%9F&page=1&per_page=2
    //用户搜索
    @GET("search/people/")
    Observable<SearchPeopleListBean> httpsPeopleSearchRx(@Header(Constant.Authorization) String authorization, @Query("q") String key, @Query("page") int page, @Query("per_page") int per_page);

}
