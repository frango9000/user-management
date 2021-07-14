import { ChangeDetectionStrategy, Component } from '@angular/core';
import { UserService } from '../../../../services/user.service';

@Component({
  selector: 'chess-lite-user-remove-account-confirm',
  templateUrl: './user-remove-account-confirm.component.html',
  styleUrls: ['./user-remove-account-confirm.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserRemoveAccountConfirmComponent {
  constructor(public readonly userService: UserService) {}
}
