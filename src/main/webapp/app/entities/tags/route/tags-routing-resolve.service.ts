import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ITags } from '../tags.model';
import { TagsService } from '../service/tags.service';

export const tagsResolve = (route: ActivatedRouteSnapshot): Observable<null | ITags> => {
  const id = route.params['id'];
  if (id) {
    return inject(TagsService)
      .find(id)
      .pipe(
        mergeMap((tags: HttpResponse<ITags>) => {
          if (tags.body) {
            return of(tags.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default tagsResolve;
