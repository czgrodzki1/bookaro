package pl.sztukakodu.bookaro.users.application;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.sztukakodu.bookaro.user.db.UserEntityRepository;
import pl.sztukakodu.bookaro.user.domain.UserEntity;
import pl.sztukakodu.bookaro.users.application.port.UserRegistrationUseCase;

@Service
@AllArgsConstructor
public class UserRegistrationService implements UserRegistrationUseCase {

    private final UserEntityRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public RegisterResponse register(String username, String password) {
        if (repository.findByUsernameIgnoreCase(username).isPresent()) {
            return RegisterResponse.failure("Account already exists");
        }
        UserEntity entity = new UserEntity(username, encoder.encode(password));
        return RegisterResponse.success(repository.save(entity));
    }
}
