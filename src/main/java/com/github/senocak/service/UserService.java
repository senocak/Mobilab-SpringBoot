package com.github.senocak.service;

import com.github.senocak.model.User;
import com.github.senocak.payload.RequestSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.github.senocak.security.UserPrincipal;
import com.github.senocak.util.ErrorMessageType;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.exception.ServerException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email : " + usernameOrEmail)
        );
        return UserPrincipal.create(user);
    }
    @Transactional
    public UserDetails loadUserById(Long id) throws ServerException {
        return UserPrincipal.create(findById(id));
    }
    public User loggedInUser() throws ServerException {
        long id = ((UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return findById(id);
    }
    private User findById(Long id) throws ServerException {
        return userRepository.findById(id).orElseThrow(() -> new ServerException(ErrorMessageType.NOT_FOUND, new String[]{"User", "id", String.valueOf(id)}, HttpStatus.NOT_FOUND));
    }
    public User findByEmail(String email) throws ServerException {
        return userRepository.findByEmail(email).orElseThrow(() -> new ServerException(
            ErrorMessageType.NOT_FOUND, new String[]{"User:" + email}, HttpStatus.NOT_FOUND));
    }
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    public User patchUser(RequestSchema.UserUpdateProfile userUpdateProfile) throws ServerException {
        User user = loggedInUser();
        if (userRepository.existsByEmail(userUpdateProfile.getEmail()) && !userUpdateProfile.getEmail().equals(user.getEmail()))
            throw new ServerException(ErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{"Email is already taken!"}, HttpStatus.BAD_REQUEST);
        if (userRepository.existsByUsername(userUpdateProfile.getUsername()) && !userUpdateProfile.getUsername().equals(user.getUsername()))
            throw new ServerException(ErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{"Username is already in use!"}, HttpStatus.BAD_REQUEST);
        user.setEmail(userUpdateProfile.getEmail());
        user.setUsername(userUpdateProfile.getUsername());
        user.setName(userUpdateProfile.getName());
        return save(user);
    }
    public User save(User user) {
        return userRepository.save(user);
    }
}
