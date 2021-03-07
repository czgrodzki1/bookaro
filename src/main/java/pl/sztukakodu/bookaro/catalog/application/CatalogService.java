package pl.sztukakodu.bookaro.catalog.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.AuthorJpaRepository;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Author;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.uploads.application.ports.UploadUseCase;
import pl.sztukakodu.bookaro.uploads.domain.Upload;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static pl.sztukakodu.bookaro.uploads.application.ports.UploadUseCase.SaveUploadCommand;

@Service
@AllArgsConstructor
class CatalogService implements CatalogUseCase {

    private final BookJpaRepository bookRepository;
    private final AuthorJpaRepository authorRepository;
    private final UploadUseCase upload;

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<Book> findByTitle(String title) {
        return bookRepository.findByTitleStartingWithIgnoreCase(title);
    }

    @Override
    public Optional<Book> findOneByTitle(String title) {
        return bookRepository.findDistinctFirstByTitle_ContainsIgnoreCase(title);
    }

    @Override
    public List<Book> findByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    @Override
    public List<Book> findByTitleAndAuthor(String title, String author) {
        return bookRepository.findByTitleAndAuthor(title, author);
    }

    @Override
    public Book addBook(CreateBookCommand command) {
        Book book = toBook(command);
        return bookRepository.save(book);
    }

    private Book toBook(CreateBookCommand command) {
        Book book = new Book(command.getTitle(), command.getYear(), command.getPrice());
        Set<Author> authors = getAuthorsByIds(command.getAuthors());
        updateBooks(book, authors);
        return book;
    }

    private void updateBooks(Book book, Set<Author> authors) {
        book.removeAuthors();
        authors.forEach(book::addAuthor);
    }

    private Set<Author> getAuthorsByIds(Set<Long> authors) {
        return authors
                .stream()
                .map(authorId -> authorRepository
                        .findById(authorId)
                        .orElseThrow(() -> new IllegalArgumentException("Unable to find author with id " + authorId))
                )
                .collect(Collectors.toSet());
    }

    @Override
    public UpdateBookResponse updateBook(UpdateBookCommand command) {
        return bookRepository
            .findById(command.getId())
            .map(book -> {
                Book updatedBook = updateFields(command, book);
                bookRepository.save(updatedBook);
                return UpdateBookResponse.SUCCESS;
            })
            .orElseGet(() -> new UpdateBookResponse(false, Collections.singletonList("Book not found with id: " + command.getId())));
    }

    private Book updateFields(UpdateBookCommand command, Book book){


            if (command.getTitle() != null) {
                book.setTitle(command.getTitle());
            }
            if (command.getAuthors() != null && !command.getAuthors().isEmpty()) {
                updateBooks(book, getAuthorsByIds(command.getAuthors()));
            }
            if (command.getYear() != null) {
                book.setYear(command.getYear());
            }
            if(command.getPrice() != null) {
                book.setPrice(command.getPrice());
            }
            return book;

    }

    @Override
    public void removeById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public void updateBookCover(UpdateBookCoverCommand command) {
        bookRepository.findById(command.getId())
                  .ifPresent(book -> {
                      Upload savedUpload = upload.save(new SaveUploadCommand(command.getFilename(), command.getFile(), command.getContentType()));
                      book.setCoverId(savedUpload.getId());
                      bookRepository.save(book);
                  });
    }

    @Override
    public void removeBookCover(Long id) {
        bookRepository.findById(id)
                  .ifPresent(book -> {
                      if (book.getCoverId() != null) {
                          upload.removeById(book.getCoverId());
                          book.setCoverId(null);
                          bookRepository.save(book);
                      }
                  });
    }

}
