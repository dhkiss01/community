package com.ktb.lukas.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {
// 5/31 내 생각 코드리뷰
    /*
    jwtProperties 라는 클래스를 만들어 역할을 나눔
    토큰을 담아놓을 jwtProperties , 토큰을 만들어내는건 JwtProvider
     */
    private final JwtProperties jwtProperties;
    private Key key;                            // 토큰 암호화 복호화할때 사용할 비밀키 선언

    // 평문 키를 암호학적으로 안전한 Secret키로 변환하는 메서드
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }


    // jwt 토큰을 발급하는 로직 담당
    private String createToken(
            String type,                            // role 설정
            Long userId,                            // user_id 가져옴 유저 아이디가 있어야 그 사람한테 주니까
            Map<String, Object> claims,             //
            long expSeconds                         // 발행시간 만료시간을 위해
    ) {
        Instant now = Instant.now();                // 현재시간을 알려주기 위한 UTC 기준 표현 객체


        // 토큰으로 바꾸는 순서는 Header -> Payroad -> Signature 순으로 토큰화 된다.
        // Header는 토큰의 타입 -> Payroad는 토큰에 담을 정보들이 있는 곳, 입력하면 안될 것은 입력하지 말아야함 예: 주민번호
        // Signature 이 토큰이 위조되었는지 검증하는 영역
        return Jwts.builder()
                .id(java.util.UUID.randomUUID().toString())
                .subject(String.valueOf(userId))                    //userId를 스트링 객체로 형변환 하여 가져옴
                .claim("typ", type)                           // role 파악을 위해 문자열을 넣어둡니다 "typ"
                .claims(claims)                                     // 권한을 파악하기 위한 선언
                .issuedAt(Date.from(now))                           // 토큰 발급 시간
                .expiration(Date.from(now.plusSeconds(expSeconds))) // 토큰 만료 시간
                .signWith((SecretKey) key, Jwts.SIG.HS256)          // 서버만 알고있는 대칭키로 서명 후 , 토큰 위 변조 방지
                .compact();
    }
    // 이제 진짜 토큰 발급
    public String createAccessToken(Long userId, String email, String nickname) {
        return createToken(
                "access",              // 토큰 용도 구분
                userId,                     // 유저 식별
                Map.of("email", email, "nickname", nickname),

                /*
                닉네임과 이메일을 추가해서 db에서 따로 조회하지 않고, 바로 보여주도록 설정 (추가 사용자 정보)ㅌ
                 */

                jwtProperties.getAccessTokenExpSeconds() // 토큰 만료 시간을 초 단위로 변환해 클라이언트 전달용 만료값으로 사용
        );
    }

    public String createRefreshToken(Long userId) {
        return createToken(
                "refresh",
                userId,
                Map.of(),                                   // 굳이 또 넣을 필요는 없다. 토큰에 적혀있는데? 리프레시 토큰에 또?
                jwtProperties.getRefreshTokenExpSeconds()
        );
    }
    // 클레임을 파싱해서 만료/서명 오류가 있다면 예외를 발생시킴
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)            // 서명 검증할 키 설정
                .build()                                // 토큰을 열기 위한 도구
                .parseSignedClaims(token);              // token열어서 서명이 잘되어있는지 확인
    }

    // 파싱된 클레임의 type값이 access인지 확인 왜? Refresh 토큰인지 구별해야하니까 왜? 둘이 만료기간이 다르니까


    public boolean isAccessToken(String token) {
        return "access".equals(parse(token).getPayload().get("typ", String.class));
    }
    // 파싱된 subject를 long으로 변환해서 사용자 아이디 가져옴 subject가 가지고 있는것 = 사용자 id, 페이로드는 사용자 정보 가지고있음
    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    // 액세스 토큰 만료시간을 반환
    public Long getAccessTokenValidityInMilliseconds() {
        return jwtProperties.getAccessTokenExpSeconds() * 1000;
    }
}