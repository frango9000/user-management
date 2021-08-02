import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Authority, User } from '@chess-lite/domain';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { Observable, startWith } from 'rxjs';
import { first, map, switchMap } from 'rxjs/operators';

@UntilDestroy()
@Component({
  selector: 'chess-lite-user-management-authorities',
  templateUrl: './user-management-authorities.component.html',
  styleUrls: ['./user-management-authorities.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserManagementAuthoritiesComponent implements OnInit {
  @Input() user$: Observable<User> | undefined;

  @Output() userChange = new EventEmitter<User>();

  public formArray = new FormArray([]);
  public form = new FormGroup({ authorities: this.formArray });

  submitSuccessMessage = false;
  submitErrorMessage = false;

  constructor(private readonly route: ActivatedRoute, private readonly cdr: ChangeDetectorRef) {
    this.route.data
      .pipe(
        startWith({ authorities: [] }),
        map((data) => data.authorities),
      )
      .subscribe((authorities: Authority[]) => {
        this.formArray.clear();
        authorities.forEach((authority) => {
          this.formArray.push(
            new FormGroup({
              id: new FormControl(authority.id),
              name: new FormControl(authority.name),
              active: new FormControl(false),
            }),
          );
        });
      });
  }

  ngOnInit(): void {
    this.user$?.pipe(untilDestroyed(this)).subscribe((user) => {
      this.formArray.controls.forEach((control) => {
        const hasAuthority = user?.authorities?.some((authority) => authority === control.value.name);
        if (control.value.active != hasAuthority) {
          control.patchValue({ active: hasAuthority });
        }
      });
    });
  }

  onSubmit() {
    this.user$
      ?.pipe(
        first(),
        switchMap((user) =>
          user.submitToTemplateOrThrow('update', {
            authorityIds: this.formArray.value
              .filter((authority: { active: boolean }) => authority.active)
              .map((authority: { id: string }) => authority.id),
          }),
        ),
      )
      .subscribe({
        next: (user) => {
          this.userChange.emit(user);
          this.setSubmitStatus(true);
        },
        error: () => this.setSubmitStatus(false),
      });
  }

  setSubmitStatus(success: boolean) {
    this.submitSuccessMessage = success;
    this.submitErrorMessage = !success;
    this.cdr.markForCheck();
  }
}