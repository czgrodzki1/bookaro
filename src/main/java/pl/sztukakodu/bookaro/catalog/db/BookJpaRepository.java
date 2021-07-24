package pl.sztukakodu.bookaro.catalog.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<Book, Long> {

    @Query("SELECT DISTINCT b FROM Book b JOIN FETCH b.authors")
    List<Book> findAllEager();

    List<Book> findBookByAuthors_nameContainsIgnoreCase(String name);

    List<Book> findByTitleStartingWithIgnoreCase(String name);

    Optional<Book> findDistinctFirstByTitle_ContainsIgnoreCase(String title);

    @Query("SELECT b FROM Book b JOIN b.authors a " +
            " WHERE " +
            " lower(a.name) LIKE lower(concat('%', :name, '%')) ")
    List<Book> findByAuthor(String name);


    @Query("SELECT b FROM Book b JOIN b.authors a " +
            " WHERE " +
            " b.title LIKE lower(concat('%', :title, '%'))  AND " +
            " lower(a.name) LIKE lower(concat('%', :name, '%')) ")
    List<Book> findByTitleAndAuthor(String title, String name);

    @Override
    Optional<Book> findById(Long id);
}
