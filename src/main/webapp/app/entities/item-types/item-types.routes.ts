import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { ItemTypesComponent } from './list/item-types.component';
import { ItemTypesDetailComponent } from './detail/item-types-detail.component';
import { ItemTypesUpdateComponent } from './update/item-types-update.component';
import ItemTypesResolve from './route/item-types-routing-resolve.service';

const itemTypesRoute: Routes = [
  {
    path: '',
    component: ItemTypesComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ItemTypesDetailComponent,
    resolve: {
      itemTypes: ItemTypesResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ItemTypesUpdateComponent,
    resolve: {
      itemTypes: ItemTypesResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ItemTypesUpdateComponent,
    resolve: {
      itemTypes: ItemTypesResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default itemTypesRoute;
