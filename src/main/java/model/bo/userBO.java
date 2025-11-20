package model.bo;

import model.bean.user;
import model.dao.userDAO;

public class userBO {
    private userDAO userDAO = null;
    
    public userBO()
    {
        userDAO = new userDAO();
    }
    public user checkLogin(String username, String password)
    {
        return userDAO.checkLogin(username, password);
    }
}
