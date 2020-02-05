package cn.niter.forum.interceptor;

import cn.niter.forum.enums.AdPosEnum;
import cn.niter.forum.mapper.UserAccountMapper;
import cn.niter.forum.mapper.UserMapper;
import cn.niter.forum.model.User;
import cn.niter.forum.model.UserAccount;
import cn.niter.forum.model.UserAccountExample;
import cn.niter.forum.model.UserExample;
import cn.niter.forum.service.AdService;
import cn.niter.forum.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class SessionInterceptor implements HandlerInterceptor {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AdService adService;

   /* @Value("${github.redirect.uri}")
    private String redirectUri;
    @Value("${baidu.redirect.uri}")
    private String baiduRedirectUri;
    @Value("${weibo.redirect.uri}")
    private String weiboRedirectUri;
    @Value("${qq.redirect.uri}")
    private String qqRedirectUri;*/

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       /* request.getServletContext().setAttribute("redirectUri", redirectUri);
        request.getServletContext().setAttribute("baiduRedirectUri", baiduRedirectUri);
        request.getServletContext().setAttribute("weiboRedirectUri", weiboRedirectUri);
        request.getServletContext().setAttribute("qqRedirectUri", qqRedirectUri);
*/
       if (handler instanceof ResourceHttpRequestHandler)
            return true;
        //设置广告
        for (AdPosEnum adPos : AdPosEnum.values()) {
            request.getServletContext().setAttribute(adPos.name(), adService.list(adPos.name()));
        }

        Cookie[] cookies = request.getCookies();
        if(cookies!=null&&cookies.length!=0){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("token")){
                    String token=cookie.getValue();
                    UserExample userExample = new UserExample();
                    userExample.createCriteria()
                            .andTokenEqualTo(token);
                    List<User> users = userMapper.selectByExample(userExample);

                    if(users.size()!=0){
                        User user = users.get(0);
                        UserAccountExample userAccountExample = new UserAccountExample();
                        userAccountExample.createCriteria().andUserIdEqualTo(user.getId());
                        List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
                        UserAccount userAccount = userAccounts.get(0);
                        request.getSession().setAttribute("user",user);
                        request.getSession().setAttribute("userAccount",userAccount);
                        Long unreadCount = notificationService.unreadCount(users.get(0).getId());
                        request.getSession().setAttribute("unreadCount", unreadCount);
                    //    System.out.println("用户ID："+userAccount.getGroupId());
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }
}