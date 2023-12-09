import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IItemTypes, NewItemTypes } from '../item-types.model';

export type PartialUpdateItemTypes = Partial<IItemTypes> & Pick<IItemTypes, 'id'>;

export type EntityResponseType = HttpResponse<IItemTypes>;
export type EntityArrayResponseType = HttpResponse<IItemTypes[]>;

@Injectable({ providedIn: 'root' })
export class ItemTypesService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/item-types');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/item-types/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(itemTypes: NewItemTypes): Observable<EntityResponseType> {
    return this.http.post<IItemTypes>(this.resourceUrl, itemTypes, { observe: 'response' });
  }

  update(itemTypes: IItemTypes): Observable<EntityResponseType> {
    return this.http.put<IItemTypes>(`${this.resourceUrl}/${this.getItemTypesIdentifier(itemTypes)}`, itemTypes, { observe: 'response' });
  }

  partialUpdate(itemTypes: PartialUpdateItemTypes): Observable<EntityResponseType> {
    return this.http.patch<IItemTypes>(`${this.resourceUrl}/${this.getItemTypesIdentifier(itemTypes)}`, itemTypes, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IItemTypes>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IItemTypes[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IItemTypes[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<IItemTypes[]>()], asapScheduler)));
  }

  getItemTypesIdentifier(itemTypes: Pick<IItemTypes, 'id'>): number {
    return itemTypes.id;
  }

  compareItemTypes(o1: Pick<IItemTypes, 'id'> | null, o2: Pick<IItemTypes, 'id'> | null): boolean {
    return o1 && o2 ? this.getItemTypesIdentifier(o1) === this.getItemTypesIdentifier(o2) : o1 === o2;
  }

  addItemTypesToCollectionIfMissing<Type extends Pick<IItemTypes, 'id'>>(
    itemTypesCollection: Type[],
    ...itemTypesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const itemTypes: Type[] = itemTypesToCheck.filter(isPresent);
    if (itemTypes.length > 0) {
      const itemTypesCollectionIdentifiers = itemTypesCollection.map(itemTypesItem => this.getItemTypesIdentifier(itemTypesItem)!);
      const itemTypesToAdd = itemTypes.filter(itemTypesItem => {
        const itemTypesIdentifier = this.getItemTypesIdentifier(itemTypesItem);
        if (itemTypesCollectionIdentifiers.includes(itemTypesIdentifier)) {
          return false;
        }
        itemTypesCollectionIdentifiers.push(itemTypesIdentifier);
        return true;
      });
      return [...itemTypesToAdd, ...itemTypesCollection];
    }
    return itemTypesCollection;
  }
}
