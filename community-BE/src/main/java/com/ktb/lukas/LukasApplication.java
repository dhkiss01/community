package com.ktb.lukas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LukasApplication {

	public static void main(String[] args) {
		SpringApplication.run(LukasApplication.class, args);
	}

}


// Spring 순서
// Controller (클라이언트에 URL을 받아서 맞는 서비스 메서드로 전달하는 역할) ->
// DTO (클라이언트가 보낸 Requset Body를 검증함 계층 간 데이터 전 값만 담는 전용 객체) ->
// Service (비즈니스 로직을 실행한다. 정보를 받아 데이터베이스에 저장한다던지 없으면 Throw예외 처리하거나 등등 물론 예외처리는 다른 곳에서해야함)
// 예를 들면 "야 얘 들어왔는데 내가 적은 조건에 부합해?" 라고 만들어 놓은 Exception 클래스에서 확인하고
// Exception 클래스에서 어 얘 틀려 오류 보내" 라고 다시 Service로 보내면 "ok 오류 보낼게" 하고 보내는 역할->
// Entity (데이터베이스에 테이블이랑 1:1 매핑 실제 데이터 값을 DB에 저장하기 위한 클래스) ->
// Repository (데이터베이스의 접근하고 서비스에 명령을 받아 Entity를 DB에 저장, 조회, 수정 하는 부분을 담당함)
