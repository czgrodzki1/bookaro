package pl.sztukakodu.bookaro.catalog.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogControllerApiTest {

    @LocalServerPort
    private int port;
    @MockBean
    private CatalogUseCase catalogUseCase;
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void shouldGetAllBooks() {
        //given
        Book effectiveJava = new Book("Java Concurrency in Practice", 2006, new BigDecimal("99.90"), 50L);
        Book javaConcurrency = new Book("Effective Java", 2005, new BigDecimal("129.90"), 50L);
        given(catalogUseCase.findAll()).willReturn(List.of(effectiveJava, javaConcurrency));
        ParameterizedTypeReference<List<Book>> type = new ParameterizedTypeReference<>(){};

        //when
        RequestEntity<Void> request = RequestEntity.get(URI.create("http://localhost:" + port + "/catalog")).build();
        ResponseEntity<List<Book>> response = testRestTemplate.exchange(request, type);

        //then
        assertThat(response.getBody(), containsInAnyOrder(effectiveJava, javaConcurrency));
    }

}