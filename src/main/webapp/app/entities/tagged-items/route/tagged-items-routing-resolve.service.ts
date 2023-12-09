import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ITaggedItems } from '../tagged-items.model';
import { TaggedItemsService } from '../service/tagged-items.service';

export const taggedItemsResolve = (route: ActivatedRouteSnapshot): Observable<null | ITaggedItems> => {
  const id = route.params['id'];
  if (id) {
    return inject(TaggedItemsService)
      .find(id)
      .pipe(
        mergeMap((taggedItems: HttpResponse<ITaggedItems>) => {
          if (taggedItems.body) {
            return of(taggedItems.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default taggedItemsResolve;
