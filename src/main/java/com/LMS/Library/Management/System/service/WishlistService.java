package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.payload.dto.WishlistDTO;
import com.LMS.Library.Management.System.payload.response.PageResponse;

public interface WishlistService {

    WishlistDTO addToWishlist(Long bookId,String notes) throws Exception;
    void removeFromWishlist(Long bookId) throws Exception;
    PageResponse<WishlistDTO> getMyWishlist(int page,int size) throws Exception;
}
