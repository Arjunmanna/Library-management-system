package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.exception.BookException;
import com.LMS.Library.Management.System.modal.Book;
import com.LMS.Library.Management.System.payload.dto.BookDTO;
import com.LMS.Library.Management.System.payload.request.BookSearchRequest;
import com.LMS.Library.Management.System.payload.response.PageResponse;

import java.util.List;

public interface BookService {

    BookDTO createBook(BookDTO bookDTO) throws BookException;
    List<BookDTO> createBooksBulk(List<BookDTO> bookDTOs) throws BookException;
    BookDTO getBookById(Long bookId) throws BookException;
    BookDTO getBookByISBN(String isbn) throws BookException;
    BookDTO updateBook(Long bookId,BookDTO bookDTO) throws BookException;
    void deleteBook(Long bookId) throws BookException;
    void hardDeleteBook(Long bookId) throws BookException;

    PageResponse<BookDTO> searchBooksWithFilters(BookSearchRequest searchRequest);

    long getTotalActiveBooks();
    long getTotalAvailableBooks();
}
