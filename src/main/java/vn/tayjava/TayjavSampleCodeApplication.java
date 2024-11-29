package vn.tayjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class TayjavSampleCodeApplication {

	public static void main(String[] args) {
//		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//		String rawPassword = "password";
//		String encodedPassword = passwordEncoder.encode(rawPassword);
//
//		System.out.println(encodedPassword);
		SpringApplication.run(TayjavSampleCodeApplication.class, args);
	}

}
