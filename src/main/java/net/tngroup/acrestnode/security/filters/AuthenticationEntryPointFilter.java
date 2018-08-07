package net.tngroup.acrestnode.security.filters;

import io.jsonwebtoken.Jwts;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static net.tngroup.acrestnode.security.TokenData.REQUEST_HEADER_STRING;

@Component
public class AuthenticationEntryPointFilter implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        try {
            String token = request.getHeader(REQUEST_HEADER_STRING);
            Jwts.parser().parse(token);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_SEE_OTHER, "Token expired");
        }
    }
}