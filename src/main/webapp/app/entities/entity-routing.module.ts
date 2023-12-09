import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'authors',
        data: { pageTitle: 'easyBlogApp.authors.home.title' },
        loadChildren: () => import('./authors/authors.routes'),
      },
      {
        path: 'item-types',
        data: { pageTitle: 'easyBlogApp.itemTypes.home.title' },
        loadChildren: () => import('./item-types/item-types.routes'),
      },
      {
        path: 'tags',
        data: { pageTitle: 'easyBlogApp.tags.home.title' },
        loadChildren: () => import('./tags/tags.routes'),
      },
      {
        path: 'users',
        data: { pageTitle: 'easyBlogApp.users.home.title' },
        loadChildren: () => import('./users/users.routes'),
      },
      {
        path: 'pages',
        data: { pageTitle: 'easyBlogApp.pages.home.title' },
        loadChildren: () => import('./pages/pages.routes'),
      },
      {
        path: 'posts',
        data: { pageTitle: 'easyBlogApp.posts.home.title' },
        loadChildren: () => import('./posts/posts.routes'),
      },
      {
        path: 'tagged-items',
        data: { pageTitle: 'easyBlogApp.taggedItems.home.title' },
        loadChildren: () => import('./tagged-items/tagged-items.routes'),
      },
      {
        path: 'comments',
        data: { pageTitle: 'easyBlogApp.comments.home.title' },
        loadChildren: () => import('./comments/comments.routes'),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
