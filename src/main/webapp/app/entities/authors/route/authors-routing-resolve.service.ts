import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IAuthors } from '../authors.model';
import { AuthorsService } from '../service/authors.service';

export const authorsResolve = (route: ActivatedRouteSnapshot): Observable<null | IAuthors> => {
  const id = route.params['id'];
  if (id) {
    return inject(AuthorsService)
      .find(id)
      .pipe(
        mergeMap((authors: HttpResponse<IAuthors>) => {
          if (authors.body) {
            return of(authors.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default authorsResolve;
