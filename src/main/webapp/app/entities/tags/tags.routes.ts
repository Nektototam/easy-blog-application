import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { TagsComponent } from './list/tags.component';
import { TagsDetailComponent } from './detail/tags-detail.component';
import { TagsUpdateComponent } from './update/tags-update.component';
import TagsResolve from './route/tags-routing-resolve.service';

const tagsRoute: Routes = [
  {
    path: '',
    component: TagsComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: TagsDetailComponent,
    resolve: {
      tags: TagsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: TagsUpdateComponent,
    resolve: {
      tags: TagsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: TagsUpdateComponent,
    resolve: {
      tags: TagsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default tagsRoute;
