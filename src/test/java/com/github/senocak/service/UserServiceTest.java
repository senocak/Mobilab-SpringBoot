package com.github.senocak.service;

import com.github.senocak.payload.RequestSchema;
import com.github.senocak.payload.ResponseSchema;
import com.github.senocak.util.TestConstants;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.User;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.security.UserPrincipal;
import com.sun.jna.platform.win32.LMAccess;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

class UserServiceTest {
    @InjectMocks UserService userService;
    @Mock UserRepository userRepository;
    @Mock Authentication authentication;
    @Mock SecurityContext securityContext;
    @Mock UserPrincipal userPrincipal;

    @BeforeEach
    void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void givenUsernameOrEmailWithUsernameNotFoundExceptionWhenLoadUserByUsernameThenAssertResult(){
        // When
        Executable result = () -> userService.loadUserByUsername(TestConstants.EMAIL);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(UsernameNotFoundException.class, result);
    }
    @Test
    void givenUsernameOrEmailWhenLoadUserByUsernameThenAssertResult(){
        // Given
        Mockito.doReturn(Optional.of(TestConstants.USER_1)).when(userRepository).findByUsernameOrEmail(TestConstants.EMAIL, TestConstants.EMAIL);
        // When
        UserDetails userDetails = userService.loadUserByUsername(TestConstants.EMAIL);
        // Then
        Assertions.assertThat(userDetails.getUsername()).isEqualTo(TestConstants.USER_1.getUsername());
    }
    @Test
    void givenIdWithServerExceptionWhenLoadUserByIdThenAssertResult() {
        // When
        Executable result = () -> userService.loadUserById(TestConstants.USER_1.getId());
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenIdWhenLoadUserByIdThenAssertResult() throws ServerException {
        // Given
        Mockito.doReturn(Optional.of(TestConstants.USER_1)).when(userRepository).findById(TestConstants.USER_1.getId());
        // When
        UserDetails userDetails = userService.loadUserById(TestConstants.USER_1.getId());
        // Then
        Assertions.assertThat(userDetails.getUsername()).isEqualTo(TestConstants.USER_1.getUsername());
    }
    @Test
    void givenNoneWithServerExceptionWhenLoggedInUserThenAssertResult() {
        // Given
        Mockito.doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        Mockito.doReturn(userPrincipal).when(authentication).getPrincipal();
        Mockito.doReturn(1L).when(userPrincipal).getId();
        // When
        Executable result = () -> userService.loggedInUser();
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenNoneWhenLoggedInUserThenAssertResult() throws ServerException {
        // Given
        Mockito.doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        Mockito.doReturn(userPrincipal).when(authentication).getPrincipal();
        Mockito.doReturn(1L).when(userPrincipal).getId();
        Mockito.doReturn(Optional.of(TestConstants.USER_1)).when(userRepository).findById(1L);
        // When
        User result = userService.loggedInUser();
        // Then
        Assertions.assertThat(result).isEqualTo(TestConstants.USER_1);
    }
    @Test
    void givenUserUpdateProfileWithExistsUsernameWhenLoadUserByIdThenAssertResult() {
        // Given
        RequestSchema.UserUpdateProfile userUpdateProfile = new RequestSchema.UserUpdateProfile();
        userUpdateProfile.setName(TestConstants.NAME);
        userUpdateProfile.setEmail(TestConstants.EMAIL);
        userUpdateProfile.setUsername(TestConstants.NAME);
        Mockito.doReturn(true).when(userRepository).existsByUsername(TestConstants.NAME);
        // When
        Executable result = () -> userService.patchUser(userUpdateProfile);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenUserUpdateProfileWithExistsEmailWhenLoadUserByIdThenAssertResult() {
        // Given
        RequestSchema.UserUpdateProfile userUpdateProfile = new RequestSchema.UserUpdateProfile();
        userUpdateProfile.setName(TestConstants.NAME);
        userUpdateProfile.setEmail(TestConstants.EMAIL);
        userUpdateProfile.setUsername(TestConstants.NAME);
        Mockito.doReturn(true).when(userRepository).existsByEmail(TestConstants.EMAIL);
        // When
        Executable result = () -> userService.patchUser(userUpdateProfile);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenUserUpdateProfileWhenLoadUserByIdThenAssertResult() throws ServerException {
        // Given
        Mockito.doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        Mockito.doReturn(userPrincipal).when(authentication).getPrincipal();
        Mockito.doReturn(1L).when(userPrincipal).getId();
        Mockito.doReturn(Optional.of(TestConstants.USER_1)).when(userRepository).findById(1L);
        // For SecurityContext
        RequestSchema.UserUpdateProfile userUpdateProfile = new RequestSchema.UserUpdateProfile();
        userUpdateProfile.setName(TestConstants.NAME);
        userUpdateProfile.setEmail(TestConstants.EMAIL);
        userUpdateProfile.setUsername(TestConstants.NAME);
        Mockito.doReturn(TestConstants.USER_1).when(userRepository).save(Mockito.any(User.class));
        // When
        User patchUser = userService.patchUser(userUpdateProfile);
        // Then
        Assertions.assertThat(patchUser).isEqualTo(TestConstants.USER_1);
    }
}
