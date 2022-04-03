import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ToasterService } from '@app/ui/shared/app';
import { fileSizeValidator } from '@app/ui/shared/common';
import { CardViewHeaderService } from '@app/ui/shared/core';
import { UserSettingsService } from '../../../../services/user-settings.service';

@Component({
  selector: 'app-user-upload-avatar',
  templateUrl: './user-upload-avatar.component.html',
  styleUrls: ['./user-upload-avatar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserUploadAvatarComponent implements OnDestroy {
  maxSize = 128;

  public readonly form = new FormGroup({
    avatar: new FormControl(null, [fileSizeValidator(this.maxSize * 1024)]),
  });

  constructor(
    public readonly userService: UserSettingsService,
    private readonly headerService: CardViewHeaderService,
    private readonly toasterService: ToasterService,
  ) {
    this.headerService.setHeader({ title: 'User Avatar' });
  }

  ngOnDestroy(): void {
    this.headerService.resetHeader();
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onSubmit(event: any) {
    if (event?.target?.files && event?.target?.files[0]) {
      this.userService.uploadAvatar(event.target.files[0]).subscribe({
        next: () => this.toasterService.showToast({ message: 'Image Uploaded Successfully' }),
        error: () => this.toasterService.showErrorToast({ message: 'Error uploading profile image, max 30kb' }),
      });
    }
  }
}