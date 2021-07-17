package pl.sztukakodu.bookaro.catalog.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.AuthorJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Author;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CatalogControllerIT {

    @Autowired
    private AuthorJpaRepository authorJpaRepository;
    @Autowired
    private CatalogUseCase catalogUseCase;
    @Autowired
    private CatalogController catalogController;

    @Test
    void getAllBooks() {
        //given
        setUpEffectiveJavaWithAuthor();
        setUpJavaConcurrencyInPracticeBookWithAuthor();
        //when
        List<Book> books = catalogController.getAll(Optional.empty(), Optional.empty());

        //then
        assertEquals(2, books.size());
    }

    @Test
    void getAllBooksByAuthor() {
        //given
        setUpEffectiveJavaWithAuthor();
        setUpJavaConcurrencyInPracticeBookWithAuthor();
        //when
        List<Book> books = catalogController.getAll(Optional.empty(), Optional.of("Bloch"));

        //then
        assertEquals(1, books.size());
        assertEquals("Effective Java", books.get(0).getTitle());
    }

    @Test
    void getAllBooksByTitle() {
        //given
        setUpEffectiveJavaWithAuthor();
        setUpJavaConcurrencyInPracticeBookWithAuthor();
        //when
        List<Book> books = catalogController.getAll(Optional.of("Java Concurrency in Practice"), Optional.empty());

        //then
        assertEquals(1, books.size());
        assertEquals("Java Concurrency in Practice", books.get(0).getTitle());
    }

    private void setUpJavaConcurrencyInPracticeBookWithAuthor() {
        Author goetz = authorJpaRepository.save(new Author("Goetz"));
        catalogUseCase.addBook(new CatalogUseCase.CreateBookCommand(
                        "Java Concurrency in Practice",
                        Set.of(goetz.getId()),
                        2006,
                        new BigDecimal("129.90"),
                        50L
                )
        );
    }

    private void setUpEffectiveJavaWithAuthor() {
        Author bloch = authorJpaRepository.save(new Author("Bloch"));
        catalogUseCase.addBook(new CatalogUseCase.CreateBookCommand(
                        "Effective Java",
                        Set.of(bloch.getId()),
                        2005,
                        new BigDecimal("99.00"),
                        50L
                )
        );
    }

}