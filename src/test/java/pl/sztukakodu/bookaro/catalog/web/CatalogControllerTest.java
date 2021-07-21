package pl.sztukakodu.bookaro.catalog.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CatalogController.class})
class CatalogControllerTest {


    @MockBean
    private CatalogUseCase catalogUseCase;
    @Autowired
    private CatalogController catalogController;

    @Test
    public void getAllBooksHappyPath() {
        //given
        Book effectiveJava = new Book("Java Concurrency in Practice", 2006, new BigDecimal("99.90"), 50L);
        Book javaConcurrency = new Book("Effective Java", 2005, new BigDecimal("129.90"), 50L);
        given(catalogUseCase.findAll()).willReturn(List.of(effectiveJava, javaConcurrency));

        //when
        List<Book> all = catalogController.getAll(Optional.empty(), Optional.empty());

        //then
        assertEquals(2, all.size());
        assertThat(all, (containsInAnyOrder(effectiveJava, javaConcurrency)));
    }

}