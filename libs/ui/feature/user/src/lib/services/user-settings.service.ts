import { Injectable } from '@angular/core';
import { clearSession, filterNulls, SessionRepository, updateSession } from '@app/ui/shared/app';
import {
  CurrentUserRelations,
  IUserPreferences,
  User,
  UserChangePasswordInput,
  UserPreferences,
  UserUpdateProfileInput,
} from '@app/ui/shared/domain';
import { HalFormService, Resource, submitToTemplateOrThrowPipe } from '@hal-form-client';
import { Actions } from '@ngneat/effects-ng';
import { Observable } from 'rxjs';
import { first, map, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class UserSettingsService {
  constructor(
    private readonly halFormsService: HalFormService,
    private readonly sessionRepository: SessionRepository,
    private readonly actions: Actions,
  ) {}

  public getCurrentUser(): Observable<User | undefined> {
    return this.sessionRepository.user$;
  }

  public getCurrentUserPreferences(): Observable<UserPreferences | undefined> {
    return this.sessionRepository.userPreferences$;
  }

  public isAllowedToUpdateProfile(): Observable<boolean> {
    return this.getCurrentUser().pipe(
      map((user: User | undefined) => !!user?.isAllowedTo(CurrentUserRelations.UPDATE_PROFILE_REL)),
    );
  }

  public updateProfile(body: UserUpdateProfileInput): Observable<User> {
    return this.getCurrentUser().pipe(
      first(),
      filterNulls(),
      submitToTemplateOrThrowPipe(CurrentUserRelations.UPDATE_PROFILE_REL, { body }),
      tap((user) => this.actions.dispatch(updateSession({ user }))),
    );
  }

  public isAllowedToDeleteAccount(): Observable<boolean> {
    return this.getCurrentUser().pipe(
      map((user: User | undefined) => !!user?.isAllowedTo(CurrentUserRelations.DELETE_ACCOUNT_REL)),
    );
  }

  public deleteAccount(): Observable<Resource> {
    return this.getCurrentUser().pipe(
      first(),
      filterNulls(),
      submitToTemplateOrThrowPipe(CurrentUserRelations.DELETE_ACCOUNT_REL),
      tap(() => this.actions.dispatch(clearSession())),
    );
  }

  public isAllowedToChangePassword(): Observable<boolean> {
    return this.getCurrentUser().pipe(
      map((user: User | undefined) => !!user?.isAllowedTo(CurrentUserRelations.CHANGE_PASSWORD_REL)),
    );
  }

  public changePassword(body: UserChangePasswordInput): Observable<User> {
    return this.getCurrentUser().pipe(
      first(),
      filterNulls(),
      submitToTemplateOrThrowPipe(CurrentUserRelations.CHANGE_PASSWORD_REL, { body }),
    );
  }

  public isAllowedToUploadAvatar(): Observable<boolean> {
    return this.getCurrentUser().pipe(
      map((user: User | undefined) => !!user?.isAllowedTo(CurrentUserRelations.UPLOAD_AVATAR_REL)),
    );
  }

  public uploadAvatar(file: File): Observable<User> {
    const body = new FormData();
    body.append('avatar', file);

    return this.getCurrentUser().pipe(
      first(),
      filterNulls(),
      submitToTemplateOrThrowPipe(CurrentUserRelations.UPLOAD_AVATAR_REL, { body }),
      tap((user) => this.actions.dispatch(updateSession({ user }))),
    );
  }

  public hasLinkToUserPreferences(): Observable<boolean> {
    return this.getCurrentUser().pipe(
      map((user: User | undefined) => !!user?.hasLink(CurrentUserRelations.USER_PREFERENCES_REL)),
    );
  }

  public updateUserPreferences(body: IUserPreferences): Observable<UserPreferences> {
    return this.getCurrentUserPreferences().pipe(
      first(),
      filterNulls(),
      submitToTemplateOrThrowPipe(CurrentUserRelations.UPDATE_PREFERENCES_REL, { body }),
      tap((userPreferences: UserPreferences) => {
        this.actions.dispatch(updateSession({ userPreferences }));
      }),
    );
  }

  public isAllowedToUpdateUserPreferences(): Observable<boolean> {
    return this.getCurrentUserPreferences().pipe(
      map(
        (userPreferences: UserPreferences | undefined) =>
          !!userPreferences?.isAllowedTo(CurrentUserRelations.UPDATE_PREFERENCES_REL),
      ),
    );
  }
}