package com.LMS.Library.Management.System.service.impl;

import com.LMS.Library.Management.System.configuration.JwtProvider;
import com.LMS.Library.Management.System.domain.UserRole;
import com.LMS.Library.Management.System.exception.UserException;
import com.LMS.Library.Management.System.mapper.UserMapper;
import com.LMS.Library.Management.System.modal.PasswordResetToken;
import com.LMS.Library.Management.System.modal.User;
import com.LMS.Library.Management.System.payload.dto.UserDTO;
import com.LMS.Library.Management.System.payload.response.AuthResponse;
import com.LMS.Library.Management.System.repository.PasswordResetTokenRepository;
import com.LMS.Library.Management.System.repository.UserRepository;
import com.LMS.Library.Management.System.service.AuthService;
import com.LMS.Library.Management.System.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserServiceImpl customUserServiceImpl;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    public AuthResponse login(String userName, String password) throws UserException {
        Authentication authentication = authenticate(userName,password);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);

        User user = userRepository.findByEmail(userName);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        AuthResponse response = new AuthResponse();
        response.setJwt(token);
        response.setTitle("login successfully");
        response.setMessage("welcome back "+userName);
        response.setUser(UserMapper.toDTO(user));

        return response;
    }

    private Authentication authenticate(String userName, String password) throws UserException {
        UserDetails userDetails = customUserServiceImpl.loadUserByUsername(userName);

        if(userDetails==null){
            throw new UserException("user not found with email: "+password);
        }
        if(!passwordEncoder.matches(password,userDetails.getPassword())){
            throw new UserException("password not matche");
        }

        return new UsernamePasswordAuthenticationToken(userName,null,userDetails.getAuthorities());
    }

    @Override
    public AuthResponse signup(UserDTO req) throws UserException {
        User user = userRepository.findByEmail(req.getEmail());

        if(user!=null){
            throw new UserException("email id already registered");
        }

        User createdUser = new User();
        createdUser.setEmail(req.getEmail());
        createdUser.setPassword(passwordEncoder.encode(req.getPassword()));
        createdUser.setPhone(req.getPhone());
        createdUser.setFullName(req.getFullName());
        createdUser.setLastLogin(LocalDateTime.now());
        createdUser.setRole(UserRole.ROLE_USER);

        User savedUser = userRepository.save(createdUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(),savedUser.getPassword()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = jwtProvider.generateToken(auth);

        AuthResponse response = new AuthResponse();
        response.setJwt(jwt);
        response.setTitle("welcome "+createdUser.getFullName());
        response.setMessage("registered successfully");
        response.setUser(UserMapper.toDTO(savedUser));

        return response;
    }

    @Transactional
    public void createPasswordResetToken(String email) throws UserException {

        String frontendUrl="http://locslhost:5173";
        User user = userRepository.findByEmail(email);

        if(user==null){
            throw new UserException("user not found with given email: "+email);
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        passwordResetTokenRepository.save(resetToken);
        String resetLink = frontendUrl+token;
        String subject = "password reset request";
        String body = "you requested to reset your password. Use this link (valid for 5 minutes): "+resetLink;

        emailService.sendEmail(user.getEmail(),subject,body);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(()->new Exception("token not valid"));

        if(resetToken.isExpired()){
            passwordResetTokenRepository.delete(resetToken);
            throw new Exception("token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }
}
