package com.LMS.Library.Management.System.controller;

import com.LMS.Library.Management.System.exception.BookException;
import com.LMS.Library.Management.System.payload.dto.BookDTO;
import com.LMS.Library.Management.System.payload.request.BookSearchRequest;
import com.LMS.Library.Management.System.payload.response.ApiResponse;
import com.LMS.Library.Management.System.payload.response.PageResponse;
import com.LMS.Library.Management.System.service.BookService;
import jakarta.validation.Valid;
import jdk.jshell.spi.ExecutionControl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @PostMapping("/bulk")
    public ResponseEntity<?> createBooksBulk(@Valid @RequestBody List<BookDTO> bookDTOS) throws BookException {
        List<BookDTO> createdBooks = bookService.createBooksBulk(bookDTOS);
        return ResponseEntity.ok(createdBooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) throws BookException {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) throws BookException {
        BookDTO updatedBook = bookService.updateBook(id,bookDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBook(@PathVariable Long id) throws BookException {
        bookService.deleteBook(id);
        return ResponseEntity.ok(new ApiResponse("Book Deleted Successfully",true));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse> hardDeleteBook(@PathVariable Long id) throws BookException {
        bookService.hardDeleteBook(id);
        return ResponseEntity.ok(new ApiResponse("Book Permanently Deleted",true));
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookDTO>> searchBooks(
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection){

        BookSearchRequest searchRequest = new BookSearchRequest();
        searchRequest.setGenreId(genreId);
        searchRequest.setAvailableOnly(availableOnly);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);

        PageResponse<BookDTO> books = bookService.searchBooksWithFilters(searchRequest);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/search")
    public ResponseEntity<PageResponse<BookDTO>> advanceSearch(@RequestBody BookSearchRequest searchRequest) {
        PageResponse<BookDTO> books = bookService.searchBooksWithFilters(searchRequest);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/state")
    public ResponseEntity<BookStateResponse> getBookState() {
        long totalActive = bookService.getTotalActiveBooks();
        long totalAvailable = bookService.getTotalAvailableBooks();

        BookStateResponse state = new BookStateResponse(totalActive,totalAvailable);
        return ResponseEntity.ok(state);
    }

    public static class BookStateResponse{
        public long totalActiveBooks;
        public long totalAvailableBooks;

        public BookStateResponse(long totalActiveBooks,long totalAvailableBooks){
            this.totalActiveBooks = totalActiveBooks;
            this.totalAvailableBooks = totalAvailableBooks;
        }
    }
}
