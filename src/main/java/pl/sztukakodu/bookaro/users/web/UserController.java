package pl.sztukakodu.bookaro.users.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.sztukakodu.bookaro.users.application.port.UserRegistrationUseCase;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserRegistrationUseCase registration;

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegisterCommand command) {
       return registration
               .register(command.username, command.password)
               .handle(
                       userEntity -> ResponseEntity.accepted().build(),
                       error -> ResponseEntity.badRequest().body(error)
               );
    }

    @Data
    static class RegisterCommand {
        @Email
        String username;
        @Size(min = 3, max = 100)
        String password;
    }

}
