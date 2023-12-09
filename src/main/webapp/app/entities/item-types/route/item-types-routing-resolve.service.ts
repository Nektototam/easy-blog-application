import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IItemTypes } from '../item-types.model';
import { ItemTypesService } from '../service/item-types.service';

export const itemTypesResolve = (route: ActivatedRouteSnapshot): Observable<null | IItemTypes> => {
  const id = route.params['id'];
  if (id) {
    return inject(ItemTypesService)
      .find(id)
      .pipe(
        mergeMap((itemTypes: HttpResponse<IItemTypes>) => {
          if (itemTypes.body) {
            return of(itemTypes.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default itemTypesResolve;
