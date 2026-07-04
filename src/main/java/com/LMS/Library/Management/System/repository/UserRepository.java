package com.LMS.Library.Management.System.repository;

import com.LMS.Library.Management.System.modal.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByEmail(String email);
}
