import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPages } from '../pages.model';
import { PagesService } from '../service/pages.service';

export const pagesResolve = (route: ActivatedRouteSnapshot): Observable<null | IPages> => {
  const id = route.params['id'];
  if (id) {
    return inject(PagesService)
      .find(id)
      .pipe(
        mergeMap((pages: HttpResponse<IPages>) => {
          if (pages.body) {
            return of(pages.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default pagesResolve;
