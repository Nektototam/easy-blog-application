import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { PagesComponent } from './list/pages.component';
import { PagesDetailComponent } from './detail/pages-detail.component';
import { PagesUpdateComponent } from './update/pages-update.component';
import PagesResolve from './route/pages-routing-resolve.service';

const pagesRoute: Routes = [
  {
    path: '',
    component: PagesComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: PagesDetailComponent,
    resolve: {
      pages: PagesResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: PagesUpdateComponent,
    resolve: {
      pages: PagesResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: PagesUpdateComponent,
    resolve: {
      pages: PagesResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default pagesRoute;
