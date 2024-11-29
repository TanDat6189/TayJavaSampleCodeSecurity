package vn.tayjava.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import vn.tayjava.util.Platform;

import java.io.Serializable;

@Getter
public class SignInRequest implements Serializable {

    @NotBlank(message = "username must be not null")
    private String username;

    @NotBlank(message = "password must be not blank")
    private String password;

    @NotNull(message = "platform must be not null")
    private Platform platform;

    private String deviceToken;

    private String version;
}
