import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { TaggedItemsComponent } from './list/tagged-items.component';
import { TaggedItemsDetailComponent } from './detail/tagged-items-detail.component';
import { TaggedItemsUpdateComponent } from './update/tagged-items-update.component';
import TaggedItemsResolve from './route/tagged-items-routing-resolve.service';

const taggedItemsRoute: Routes = [
  {
    path: '',
    component: TaggedItemsComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: TaggedItemsDetailComponent,
    resolve: {
      taggedItems: TaggedItemsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: TaggedItemsUpdateComponent,
    resolve: {
      taggedItems: TaggedItemsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: TaggedItemsUpdateComponent,
    resolve: {
      taggedItems: TaggedItemsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default taggedItemsRoute;
