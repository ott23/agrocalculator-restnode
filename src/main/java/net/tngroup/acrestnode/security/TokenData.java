package net.tngroup.acrestnode.security;

public class TokenData {
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String SECRET = "DevelopmentSecret";
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String REQUEST_HEADER_STRING = "Authorization";
    public static final String TOKEN_HEADER_STRING = "X-Token";
}
