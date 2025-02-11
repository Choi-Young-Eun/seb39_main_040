package seb39_40.coffeewithme.security.jwt;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate redisTemplate;

    public void save(String jwt, String email, Long exp){
        redisTemplate.opsForValue().set(email, jwt, exp, TimeUnit.MILLISECONDS);
    }

    public String findByEmail(String user){
        String redis = (String) redisTemplate.opsForValue().get("Refresh:"+user);
        if(Objects.isNull(redis))
            throw new JwtException("Refresh Token이 존재하지 않습니다. 재로그인이 필요합니다.");
        return redis;
    }

    public void findByAccessToken(String accessToken){
        String redis = (String) redisTemplate.opsForValue().get("BAN:"+accessToken);
        if(!Objects.isNull(redis))
            throw new JwtException("사용 중지된 토큰입니다. 다른 토큰을 이용해주세요");
    }

    public void remove(String key){
        Object savedRefToken = redisTemplate.opsForValue().getAndDelete("Refresh:"+key);
        if(Objects.isNull(savedRefToken))
            throw new JwtException("현재 요청 처리 중인 유저로 잠시후 다시 로그아웃을 시도해시기 바랍니다.");
    }

}
