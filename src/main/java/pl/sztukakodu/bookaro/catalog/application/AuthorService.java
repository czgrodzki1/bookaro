package pl.sztukakodu.bookaro.catalog.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sztukakodu.bookaro.catalog.application.port.AuthorUseCase;
import pl.sztukakodu.bookaro.catalog.db.AuthorJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Author;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthorService implements AuthorUseCase {

    private final AuthorJpaRepository repository;


    @Override
    public List<Author> getAll() {
        return repository.findAll();
    }
}
