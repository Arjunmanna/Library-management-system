package com.LMS.Library.Management.System.controller;

import com.LMS.Library.Management.System.payload.dto.WishlistDTO;
import com.LMS.Library.Management.System.payload.response.ApiResponse;
import com.LMS.Library.Management.System.payload.response.PageResponse;
import com.LMS.Library.Management.System.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/add/{bookId}")
    public ResponseEntity<?> addToWishlist(@PathVariable Long bookId, @RequestParam(required = false)String notes) throws Exception {
        WishlistDTO wishlistDTO = wishlistService.addToWishlist(bookId,notes);

        return ResponseEntity.ok(wishlistDTO);
    }

    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<ApiResponse> removeFromWishlist(@PathVariable Long bookId) throws Exception {
        wishlistService.removeFromWishlist(bookId);

        return ResponseEntity.ok(
                new ApiResponse("book removed from wishlist successfully",true)
        );
    }

    @GetMapping("/my-wishlist")
    public ResponseEntity<?> getMyWishlist(
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ) throws Exception {
        PageResponse<WishlistDTO> wishlistDTO = wishlistService.getMyWishlist(page,size);

        return ResponseEntity.ok(wishlistDTO);
    }
}
