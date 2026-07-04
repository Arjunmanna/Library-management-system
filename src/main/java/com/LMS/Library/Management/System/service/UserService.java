package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.modal.User;
import com.LMS.Library.Management.System.payload.dto.UserDTO;

import java.util.List;

public interface UserService {

    public User getCurrentUser() throws Exception;

    public List<UserDTO> getAllUsers();

    User findById(Long id) throws Exception;
}
