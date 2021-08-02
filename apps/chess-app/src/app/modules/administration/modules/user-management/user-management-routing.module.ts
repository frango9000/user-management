import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserResolver } from './modules/detail/resolvers/user.resolver';
import { RolesResolver } from './resolvers/roles.resolver';

const loadUserManagementHomeModule = () =>
  import('./modules/home/user-management-home.module').then((m) => m.UserManagementHomeModule);

const loadUserManagementDetailModule = () =>
  import('./modules/detail/user-management-detail.module').then((m) => m.UserManagementDetailModule);

const loadUserManagementCreateModule = () =>
  import('./modules/create/user-management-create.module').then((m) => m.UserManagementCreateModule);

const routes: Routes = [
  {
    path: '',
    loadChildren: loadUserManagementHomeModule,
  },
  {
    path: 'create',
    loadChildren: loadUserManagementCreateModule,
    resolve: { roles: RolesResolver },
  },
  {
    path: 'edit/:userId',
    loadChildren: loadUserManagementDetailModule,
    resolve: { user: UserResolver },
  },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UserManagementRoutingModule {}