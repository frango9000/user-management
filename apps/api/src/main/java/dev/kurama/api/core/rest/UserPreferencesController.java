package dev.kurama.api.core.rest;

import dev.kurama.api.core.facade.UserPreferencesFacade;
import dev.kurama.api.core.hateoas.input.UserPreferencesInput;
import dev.kurama.api.core.hateoas.model.UserPreferencesModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user/preferences")
@PreAuthorize("isAuthenticated()")
public class UserPreferencesController {

  @NonNull
  private final UserPreferencesFacade userPreferencesFacade;


  @GetMapping("/{userPreferencesId}")
  @PreAuthorize("hasAuthority('user:preferences:read')")
  public ResponseEntity<UserPreferencesModel> get(@PathVariable("userPreferencesId") String userPreferencesId) {
    return ResponseEntity.ok().body(userPreferencesFacade.findById(userPreferencesId));
  }

  @PatchMapping("/{userPreferencesId}")
  @PreAuthorize("hasAuthority('user:preferences:update')")
  public ResponseEntity<UserPreferencesModel> update(
    @PathVariable("userPreferencesId") String userPreferencesId,
    @RequestBody UserPreferencesInput userPreferencesInput) {
    return ResponseEntity.ok().body(userPreferencesFacade.update(userPreferencesId, userPreferencesInput));
  }
}