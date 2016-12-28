package huang.demo.com.huaban.API.Fragment;

/**
 * Created by LiCola on  2016/04/04  23:38
 */
public interface OnImageDetailFragmentInteractionListener
        extends OnPinsFragmentInteractionListener {
    void onClickBoardField(String key, String title);

    void onClickUserField(String key,String title);

    void onClickImageLink(String link);
}
