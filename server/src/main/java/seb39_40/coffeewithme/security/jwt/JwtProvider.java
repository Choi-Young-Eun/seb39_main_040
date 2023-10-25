package seb39_40.coffeewithme.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final long ACCESS_EXPIRATION= 1000 * 60 * 10;
    private final long REFRESH_EXPIRATION= 1000 * 60 * 60;
    private final String SECRET_KEY="cwmsecretkeycwmsecretkeycwmsecretkeycwmsecretkey";
    private final RedisRepository redisRepository;

    public String createToken(String type, String email){
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        long expiration = 0;
        String subject=type;
        if(type.equals("Access Token")){
            expiration = ACCESS_EXPIRATION;
        }else if(type.equals("Refresh Token")){
            expiration = REFRESH_EXPIRATION;
        }
        else throw new JwtException("존재하는 토큰 타입이 아닙니다.");

        return Jwts.builder()
                .signWith(key)
                .setHeaderParam("typ","JWT")
                .setSubject(subject)
                .claim("email",email)
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .compact();
    }

    public void saveToken(String email,String token, Long expiration){
        redisRepository.save(token, email, expiration);
    }

    public void removeRefreshToken(String email){
        redisRepository.remove(email);
    }

    public String substringToken(String token){
        if(!token.startsWith("Bearer "))
            throw new JwtException("JWT 토큰 형식이 올바르지 않습니다.");
        else return token.replace("Bearer ", "");
    }

    public Claims parseToken(String jwt){
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    public void validationTheSameToken(String email,String token){
        String result = redisRepository.findByEmail(email);
        if(result==null || !result.equals(token))
            throw new JwtException("토큰이 올바르지 않습니다.");
    }

    public Long getExpirationToClaims(Claims claims){
        Long time = claims.getExpiration().getTime();
        return time- new Date().getTime();
    }

    public String getEmailToClaims(Claims claims){
        return claims.get("email").toString();
    }
}
