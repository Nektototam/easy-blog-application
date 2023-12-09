import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { PostsComponent } from './list/posts.component';
import { PostsDetailComponent } from './detail/posts-detail.component';
import { PostsUpdateComponent } from './update/posts-update.component';
import PostsResolve from './route/posts-routing-resolve.service';

const postsRoute: Routes = [
  {
    path: '',
    component: PostsComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: PostsDetailComponent,
    resolve: {
      posts: PostsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: PostsUpdateComponent,
    resolve: {
      posts: PostsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: PostsUpdateComponent,
    resolve: {
      posts: PostsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default postsRoute;
