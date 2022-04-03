package dev.kurama.api.core.rest;

import static dev.kurama.api.core.constant.RestPathConstant.AUTHENTICATION_PATH;
import static dev.kurama.api.core.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static dev.kurama.api.core.rest.AuthenticationController.ACTIVATE_PATH;
import static dev.kurama.api.core.rest.AuthenticationController.LOGIN_PATH;
import static dev.kurama.api.core.rest.AuthenticationController.SIGNUP_PATH;
import static dev.kurama.api.core.rest.AuthenticationController.TOKEN_PATH;
import static dev.kurama.api.core.utility.UuidUtils.randomUUID;
import static dev.kurama.support.JsonUtils.asJsonString;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.kurama.api.core.domain.User;
import dev.kurama.api.core.facade.AuthenticationFacade;
import dev.kurama.api.core.hateoas.input.AccountActivationInput;
import dev.kurama.api.core.hateoas.input.LoginInput;
import dev.kurama.api.core.hateoas.input.RequestActivationTokenInput;
import dev.kurama.api.core.hateoas.input.SignupInput;
import dev.kurama.api.core.service.AuthenticationFacility;
import dev.kurama.api.core.service.UserService;
import dev.kurama.api.core.utility.JWTTokenProvider;
import dev.kurama.support.ImportMappers;
import dev.kurama.support.ImportTestSecurityConfiguration;
import dev.kurama.support.MockAuthorizedUser;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(controllers = AuthenticationController.class)
@Import({AuthenticationFacade.class})
@ImportMappers
@ImportTestSecurityConfiguration
class AuthenticationControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JWTTokenProvider jwtTokenProvider;

  @MockBean
  private UserService userService;

  @MockBean
  private AuthenticationFacility authenticationFacility;

  @Test
  void should_signup_new_user() throws Exception {
    SignupInput input = SignupInput.builder()
      .firstname("firstname")
      .lastname("lastname")
      .username(randomAlphanumeric(8))
      .email("em@i.l")
      .build();

    mockMvc.perform(post(AUTHENTICATION_PATH + SIGNUP_PATH).accept(MediaTypes.HAL_FORMS_JSON_VALUE)
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJsonString(input))).andExpect(status().isNoContent());

    verify(userService, times(1)).signup(input);
  }

  @Test
  void should_login_and_return_user_and_jwt_header() throws Exception {
    LoginInput input = LoginInput.builder().username(randomAlphanumeric(8)).password(randomUUID()).build();

    User user = User.builder().setRandomUUID().username(randomAlphanumeric(8)).build();
    String token = randomUUID();
    doReturn(Pair.of(user, token)).when(authenticationFacility).login(input.getUsername(), input.getPassword());

    mockMvc.perform(post(AUTHENTICATION_PATH + LOGIN_PATH).accept(MediaTypes.HAL_FORMS_JSON_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(input)))
      .andExpect(status().isOk())
      .andExpect(header().stringValues(JWT_TOKEN_HEADER, token))
      .andExpect(jsonPath("$.id", equalTo(user.getId())))
      .andExpect(jsonPath("$.username", equalTo(user.getUsername())));
  }

  @Test
  void should_refresh_token_and_return_user_and_jwt_header() throws Exception {
    User expected = User.builder().setRandomUUID().username(randomAlphanumeric(8)).build();
    String token = randomUUID();
    doReturn(Pair.of(expected, token)).when(authenticationFacility).refreshToken(expected.getId());

    mockMvc.perform(get(AUTHENTICATION_PATH + TOKEN_PATH).accept(MediaTypes.HAL_FORMS_JSON_VALUE)
        .headers(MockAuthorizedUser.builder()
          .username(expected.getUsername())
          .id(expected.getId())
          .buildAuthorizationHeader(jwtTokenProvider)))
      .andExpect(status().isOk())
      .andExpect(header().stringValues(JWT_TOKEN_HEADER, token))
      .andExpect(jsonPath("$.id", equalTo(expected.getId())))
      .andExpect(jsonPath("$.username", equalTo(expected.getUsername())));
  }

  @Test
  void should_request_activation_token() throws Exception {
    RequestActivationTokenInput input = RequestActivationTokenInput.builder().email("em@i.l").build();

    mockMvc.perform(post(AUTHENTICATION_PATH + TOKEN_PATH).accept(MediaTypes.HAL_FORMS_JSON_VALUE)
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJsonString(input))).andExpect(status().isNoContent());

    verify(userService, times(1)).requestActivationTokenByEmail(input.getEmail());
  }

  @Test
  void should_activate_account() throws Exception {
    AccountActivationInput input = AccountActivationInput.builder()
      .email("em@i.l")
      .password(randomUUID())
      .token(randomUUID())
      .build();

    mockMvc.perform(post(AUTHENTICATION_PATH + ACTIVATE_PATH).accept(MediaTypes.HAL_FORMS_JSON_VALUE)
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJsonString(input))).andExpect(status().isNoContent());
    verify(userService, times(1)).activateAccount(input);
  }
}