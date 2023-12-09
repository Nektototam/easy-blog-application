import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IUsers } from '../users.model';
import { UsersService } from '../service/users.service';

export const usersResolve = (route: ActivatedRouteSnapshot): Observable<null | IUsers> => {
  const id = route.params['id'];
  if (id) {
    return inject(UsersService)
      .find(id)
      .pipe(
        mergeMap((users: HttpResponse<IUsers>) => {
          if (users.body) {
            return of(users.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default usersResolve;
