package net.tngroup.acrestnode.web.security.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static net.tngroup.acrestnode.web.security.TokenData.*;

public class TokenAuthenticationService {


    public static void addAuthentication(HttpServletResponse res, String username) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode subjectJson = mapper.createObjectNode();
        subjectJson.put("username", username);
        String subject = subjectJson.toString();

        String JWT = Jwts.builder()
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, SECRET)
                .compact();

        res.addHeader(TOKEN_HEADER_STRING, JWT);
    }


    public static Authentication getAuthentication(HttpServletRequest request, UserDetailsService userDetailsService)  {

        try {
            String token = request.getHeader(REQUEST_HEADER_STRING);

            if (token != null) {

                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET)
                        .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                        .getBody();

                String subject = claims.getSubject();
                Long expiration = claims.getExpiration().getTime();

                UserDetails user;
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String username = objectMapper.readTree(subject).get("username").textValue();
                    user = userDetailsService.loadUserByUsername(username);
                } catch (Exception e) {
                    return null;
                }

                if (user == null) return null;
                Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
                ((UsernamePasswordAuthenticationToken) authentication).setDetails(expiration);
                return authentication;
            }

            return null;
        } catch (SignatureException | ExpiredJwtException e) {
            return null;
        }
    }
}