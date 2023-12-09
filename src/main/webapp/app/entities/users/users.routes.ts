import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { UsersComponent } from './list/users.component';
import { UsersDetailComponent } from './detail/users-detail.component';
import { UsersUpdateComponent } from './update/users-update.component';
import UsersResolve from './route/users-routing-resolve.service';

const usersRoute: Routes = [
  {
    path: '',
    component: UsersComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: UsersDetailComponent,
    resolve: {
      users: UsersResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: UsersUpdateComponent,
    resolve: {
      users: UsersResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: UsersUpdateComponent,
    resolve: {
      users: UsersResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default usersRoute;
