package vn.tayjava.dto.respond;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class UserDetailResponse implements Serializable {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public UserDetailResponse(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
