package net.tngroup.acrestnode.web.security.filters;

import net.tngroup.acrestnode.nodeclient.components.ChannelComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UnavailableFilter implements HandlerInterceptor {

    private ChannelComponent channelComponent;

    @Autowired
    public UnavailableFilter(ChannelComponent channelComponent) {
        this.channelComponent = channelComponent;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (channelComponent.isStatus()) {
            return true;
        } else {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
