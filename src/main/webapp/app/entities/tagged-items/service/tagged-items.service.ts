import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { ITaggedItems, NewTaggedItems } from '../tagged-items.model';

export type PartialUpdateTaggedItems = Partial<ITaggedItems> & Pick<ITaggedItems, 'id'>;

export type EntityResponseType = HttpResponse<ITaggedItems>;
export type EntityArrayResponseType = HttpResponse<ITaggedItems[]>;

@Injectable({ providedIn: 'root' })
export class TaggedItemsService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/tagged-items');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/tagged-items/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(taggedItems: NewTaggedItems): Observable<EntityResponseType> {
    return this.http.post<ITaggedItems>(this.resourceUrl, taggedItems, { observe: 'response' });
  }

  update(taggedItems: ITaggedItems): Observable<EntityResponseType> {
    return this.http.put<ITaggedItems>(`${this.resourceUrl}/${this.getTaggedItemsIdentifier(taggedItems)}`, taggedItems, {
      observe: 'response',
    });
  }

  partialUpdate(taggedItems: PartialUpdateTaggedItems): Observable<EntityResponseType> {
    return this.http.patch<ITaggedItems>(`${this.resourceUrl}/${this.getTaggedItemsIdentifier(taggedItems)}`, taggedItems, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ITaggedItems>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ITaggedItems[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ITaggedItems[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<ITaggedItems[]>()], asapScheduler)));
  }

  getTaggedItemsIdentifier(taggedItems: Pick<ITaggedItems, 'id'>): number {
    return taggedItems.id;
  }

  compareTaggedItems(o1: Pick<ITaggedItems, 'id'> | null, o2: Pick<ITaggedItems, 'id'> | null): boolean {
    return o1 && o2 ? this.getTaggedItemsIdentifier(o1) === this.getTaggedItemsIdentifier(o2) : o1 === o2;
  }

  addTaggedItemsToCollectionIfMissing<Type extends Pick<ITaggedItems, 'id'>>(
    taggedItemsCollection: Type[],
    ...taggedItemsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const taggedItems: Type[] = taggedItemsToCheck.filter(isPresent);
    if (taggedItems.length > 0) {
      const taggedItemsCollectionIdentifiers = taggedItemsCollection.map(
        taggedItemsItem => this.getTaggedItemsIdentifier(taggedItemsItem)!,
      );
      const taggedItemsToAdd = taggedItems.filter(taggedItemsItem => {
        const taggedItemsIdentifier = this.getTaggedItemsIdentifier(taggedItemsItem);
        if (taggedItemsCollectionIdentifiers.includes(taggedItemsIdentifier)) {
          return false;
        }
        taggedItemsCollectionIdentifiers.push(taggedItemsIdentifier);
        return true;
      });
      return [...taggedItemsToAdd, ...taggedItemsCollection];
    }
    return taggedItemsCollection;
  }
}
