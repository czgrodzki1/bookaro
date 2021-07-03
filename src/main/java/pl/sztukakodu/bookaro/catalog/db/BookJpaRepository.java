package pl.sztukakodu.bookaro.catalog.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<Book, Long> {

    @Query("SELECT DISTINCT b FROM Book b JOIN FETCH b.authors")
    List<Book> findAllEager();

    List<Book> findBookByAuthors_firstNameContainsIgnoreCaseOrAuthors_LastNameContainsIgnoreCase(String firstName, String lastName);

    List<Book> findByTitleStartingWithIgnoreCase(String name);

    Optional<Book> findDistinctFirstByTitle_ContainsIgnoreCase(String title);

    @Query("SELECT b FROM Book b JOIN b.authors a " +
            " WHERE " +
            " lower(a.firstName) LIKE lower(concat('%', :name, '%')) " +
            " OR lower(a.lastName) LIKE lower(concat('%', :name, '%'))")
    List<Book> findByAuthor(String name);


    @Query("SELECT b FROM Book b JOIN b.authors a " +
            " WHERE " +
            " b.title LIKE lower(concat('%', :title, '%'))  AND " +
            " (lower(a.firstName) LIKE lower(concat('%', :author, '%')) " +
            " OR lower(a.lastName) LIKE lower(concat('%', :author, '%'))) ")
    List<Book> findByTitleAndAuthor(String title, String author);

}
