import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { AuthorsComponent } from './list/authors.component';
import { AuthorsDetailComponent } from './detail/authors-detail.component';
import { AuthorsUpdateComponent } from './update/authors-update.component';
import AuthorsResolve from './route/authors-routing-resolve.service';

const authorsRoute: Routes = [
  {
    path: '',
    component: AuthorsComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: AuthorsDetailComponent,
    resolve: {
      authors: AuthorsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: AuthorsUpdateComponent,
    resolve: {
      authors: AuthorsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: AuthorsUpdateComponent,
    resolve: {
      authors: AuthorsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default authorsRoute;
