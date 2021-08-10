import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CurrentUserRelations, User } from '@chess-lite/domain';
import { UntilDestroy } from '@ngneat/until-destroy';
import { EMPTY, iif, Observable } from 'rxjs';
import { first, switchMap } from 'rxjs/operators';
import { HeaderService } from '../../../../../../core/services/header.service';
import { CurrentUserService } from '../../../../services/current-user.service';
import { UserRemoveAccountConfirmComponent } from '../user-remove-account-confirm/user-remove-account-confirm.component';

@UntilDestroy()
@Component({
  selector: 'chess-lite-user-remove-account',
  templateUrl: './user-remove-account.component.html',
  styleUrls: ['./user-remove-account.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserRemoveAccountComponent implements OnDestroy {
  private readonly _user$: Observable<User> = this.userService.getCurrentUser() as Observable<User>;

  DELETE_ACCOUNT_REL = CurrentUserRelations.DELETE_ACCOUNT_REL;

  constructor(
    private readonly dialogService: MatDialog,
    public readonly userService: CurrentUserService,
    private readonly headerService: HeaderService,
  ) {
    this.headerService.setHeader({ title: 'Remove Account' });
  }

  ngOnDestroy(): void {
    this.headerService.resetHeader();
  }

  get user$(): Observable<User> {
    return this._user$;
  }

  openDialog(): void {
    const matDialogRef = this.dialogService.open(UserRemoveAccountConfirmComponent, {
      width: '350px',
    });
    matDialogRef
      .afterClosed()
      .pipe(
        first(),
        switchMap((username) => iif(() => !!username?.length, this.userService.deleteAccount(this.user$), EMPTY)),
      )
      .subscribe();
  }
}
