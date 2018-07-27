package net.tngroup.acrestnode.security.components;

import net.tngroup.acrestnode.nodeclient.components.ChannelComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private ChannelComponent channelComponent;

    @Autowired
    public RestAuthenticationEntryPoint(ChannelComponent channelComponent) {
        super();
        this.channelComponent = channelComponent;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        if (!channelComponent.isStatus()) response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        else response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}