package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.exception.UserException;
import com.LMS.Library.Management.System.payload.dto.UserDTO;
import com.LMS.Library.Management.System.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse login(String userName,String password) throws UserException;
    AuthResponse signup(UserDTO req) throws UserException;

    void createPasswordResetToken(String email) throws UserException;
    void resetPassword(String token,String newPassword) throws Exception;
}
