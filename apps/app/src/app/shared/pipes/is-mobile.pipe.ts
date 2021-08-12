import { NgModule, Pipe, PipeTransform } from '@angular/core';
import { Observable } from 'rxjs';
import { IsMobileService } from '../services/is-mobile.service';

@Pipe({
  name: 'isMobile',
})
export class IsMobilePipe implements PipeTransform {
  constructor(private readonly isMobileService: IsMobileService) {}

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  transform(_ = ''): Observable<boolean> {
    return this.isMobileService.isMobile$;
  }
}

@NgModule({
  declarations: [IsMobilePipe],
  exports: [IsMobilePipe],
})
export class IsMobileModule {}
