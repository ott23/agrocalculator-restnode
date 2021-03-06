package net.tngroup.acrestnode.web.security.filters;

import net.tngroup.acrestnode.web.security.services.TokenAuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static net.tngroup.acrestnode.web.security.TokenData.EXPIRATION_TIME;

public class JwtAuthenticationFilter extends GenericFilterBean {

    private UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService) {
        super();
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain)
            throws IOException, ServletException, AuthenticationException {
        Authentication authentication = TokenAuthenticationService.getAuthentication((HttpServletRequest) request, userDetailsService);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            Long expiration = (Long) authentication.getDetails();
            Long currentTime = System.currentTimeMillis();
            if (expiration - currentTime < EXPIRATION_TIME * 0.8) {
                TokenAuthenticationService.addAuthentication((HttpServletResponse) response, authentication.getName());
            }
        } catch (NullPointerException e) {
            // Empty details
        }

        filterChain.doFilter(request, response);
    }
}