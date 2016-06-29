package service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class AuthorizeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String appId = getServletContext().getInitParameter("fb_app_id");
        String redirectUri = getServletContext().getInitParameter("redirect_uri");

        String fbLoginUrl = "https://graph.facebook.com/oauth/authorize?" + "client_id="
                            + appId + "&redirect_uri="
                            + redirectUri
                            + "&scope=email,public_profile,user_friends,user_birthday,user_hometown,user_about_me";

        System.out.println("FB Auth URL: "+fbLoginUrl);
        resp.sendRedirect(fbLoginUrl);
    }
}
