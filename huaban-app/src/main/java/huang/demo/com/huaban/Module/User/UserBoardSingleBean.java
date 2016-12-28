package huang.demo.com.huaban.Module.User;

import huang.demo.com.huaban.Entity.ErrorBaseBean;

/**
 * Created by LiCola on  2016/04/07  17:37
 */
public class UserBoardSingleBean extends ErrorBaseBean {

    private UserBoardItemBean board;

    public UserBoardItemBean getBoards() {
        return board;
    }

    public void setBoards(UserBoardItemBean board) {
        this.board = board;
    }
}
