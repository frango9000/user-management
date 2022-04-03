package dev.kurama.api.core.service;

import static dev.kurama.api.core.utility.UuidUtils.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.kurama.api.core.domain.Role;
import dev.kurama.api.core.domain.User;
import dev.kurama.api.core.domain.UserPrincipal;
import dev.kurama.api.core.exception.domain.RoleCanNotLoginException;
import dev.kurama.api.core.exception.domain.not.found.UserNotFoundException;
import dev.kurama.api.core.utility.JWTTokenProvider;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AuthenticationFacilityTest {

  @InjectMocks
  private AuthenticationFacility facility;

  @Mock
  private UserService userService;

  @Mock
  private JWTTokenProvider jwtTokenProvider;

  @Mock
  private AuthenticationManager authenticationManager;

  @Nested
  class LoginTests {

    @Test
    void login_should_return_authenticated_user_excerpt() throws RoleCanNotLoginException, UserNotFoundException {
      String testToken = "test_token";
      String username = "username";
      String password = "password";
      User user = User.builder()
        .setRandomUUID()
        .role(Role.builder().setRandomUUID().canLogin(true).build())
        .username(username)
        .locked(false)
        .build();
      when(userService.findUserByUsername(username)).thenReturn(Optional.ofNullable(user));
      when(jwtTokenProvider.generateJWTToken(any(UserPrincipal.class))).thenReturn(testToken);

      Pair<User, String> authenticatedUser = facility.login(username, password);

      verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(username, password));
      verify(jwtTokenProvider, times(2)).generateJWTToken(any(UserPrincipal.class));
      verify(jwtTokenProvider).getDecodedJWT(anyString());
      assertThat(authenticatedUser).isNotNull()
        .hasFieldOrPropertyWithValue("left", user)
        .hasFieldOrPropertyWithValue("right", testToken);
    }

    @Test()
    void login_should_throw_if_user_is_locked() {
      String username = "username";
      String password = "password";
      User user = User.builder()
        .setRandomUUID()
        .role(Role.builder().setRandomUUID().canLogin(true).build())
        .username(username)
        .locked(true)
        .build();
      when(userService.findUserByUsername(username)).thenReturn(Optional.ofNullable(user));

      assertThrows(LockedException.class, () -> {
        facility.login(username, password);
      });
    }

    @Test
    void login_should_throw_if_role_can_not_login() throws RoleCanNotLoginException {
      String username = "username";
      String password = "password";
      User user = User.builder()
        .setRandomUUID()
        .role(Role.builder().setRandomUUID().canLogin(false).build())
        .username(username)
        .locked(false)
        .build();
      when(userService.findUserByUsername(username)).thenReturn(Optional.ofNullable(user));

      assertThrows(RoleCanNotLoginException.class, () -> {
        facility.login(username, password);
      });
    }
  }

  @Nested
  class RefreshTokenTests {

    @Test
    void login_should_return_authenticated_user_excerpt() throws RoleCanNotLoginException, UserNotFoundException {
      String testToken = "test_token";
      User user = User.builder()
        .setRandomUUID()
        .role(Role.builder().setRandomUUID().canLogin(true).build())
        .username(randomAlphanumeric(8))
        .locked(false)
        .build();
      when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));
      when(jwtTokenProvider.generateJWTToken(any(UserPrincipal.class))).thenReturn(testToken);

      Pair<User, String> authenticatedUser = facility.refreshToken(user.getId());

      verify(jwtTokenProvider, times(2)).generateJWTToken(any(UserPrincipal.class));
      verify(jwtTokenProvider).getDecodedJWT(anyString());
      assertThat(authenticatedUser).isNotNull()
        .hasFieldOrPropertyWithValue("left", user)
        .hasFieldOrPropertyWithValue("right", testToken);
    }

    @Test()
    void refresh_token_should_throw_if_user_is_locked() {
      User user = User.builder()
        .setRandomUUID()
        .role(Role.builder().setRandomUUID().canLogin(true).build())
        .username(randomAlphanumeric(8))
        .locked(true)
        .build();
      when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));

      assertThrows(LockedException.class, () -> facility.refreshToken(user.getId()));
    }

    @Test
    void refresh_token_should_throw_if_role_can_not_login() {
      User user = User.builder()
        .setRandomUUID()
        .role(Role.builder().setRandomUUID().canLogin(false).build())
        .username(randomAlphanumeric(8))
        .locked(false)
        .build();
      when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));

      assertThrows(RoleCanNotLoginException.class, () -> facility.refreshToken(user.getId()));
    }

    @Test
    void refresh_token_should_throw_if_user_not_found() {
      String userid = randomUUID();
      when(userService.findUserById(userid)).thenReturn(Optional.empty());

      assertThrows(UserNotFoundException.class, () -> facility.refreshToken(userid));
    }
  }

  @Test
  void should_validate_user_credentials() {
    String username = "username";
    String password = "password";

    facility.validateCredentials(username, password);

    verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(username, password));
  }
}