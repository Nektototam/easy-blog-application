import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { ITags, NewTags } from '../tags.model';

export type PartialUpdateTags = Partial<ITags> & Pick<ITags, 'id'>;

export type EntityResponseType = HttpResponse<ITags>;
export type EntityArrayResponseType = HttpResponse<ITags[]>;

@Injectable({ providedIn: 'root' })
export class TagsService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/tags');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/tags/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(tags: NewTags): Observable<EntityResponseType> {
    return this.http.post<ITags>(this.resourceUrl, tags, { observe: 'response' });
  }

  update(tags: ITags): Observable<EntityResponseType> {
    return this.http.put<ITags>(`${this.resourceUrl}/${this.getTagsIdentifier(tags)}`, tags, { observe: 'response' });
  }

  partialUpdate(tags: PartialUpdateTags): Observable<EntityResponseType> {
    return this.http.patch<ITags>(`${this.resourceUrl}/${this.getTagsIdentifier(tags)}`, tags, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ITags>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ITags[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ITags[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<ITags[]>()], asapScheduler)));
  }

  getTagsIdentifier(tags: Pick<ITags, 'id'>): number {
    return tags.id;
  }

  compareTags(o1: Pick<ITags, 'id'> | null, o2: Pick<ITags, 'id'> | null): boolean {
    return o1 && o2 ? this.getTagsIdentifier(o1) === this.getTagsIdentifier(o2) : o1 === o2;
  }

  addTagsToCollectionIfMissing<Type extends Pick<ITags, 'id'>>(
    tagsCollection: Type[],
    ...tagsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const tags: Type[] = tagsToCheck.filter(isPresent);
    if (tags.length > 0) {
      const tagsCollectionIdentifiers = tagsCollection.map(tagsItem => this.getTagsIdentifier(tagsItem)!);
      const tagsToAdd = tags.filter(tagsItem => {
        const tagsIdentifier = this.getTagsIdentifier(tagsItem);
        if (tagsCollectionIdentifiers.includes(tagsIdentifier)) {
          return false;
        }
        tagsCollectionIdentifiers.push(tagsIdentifier);
        return true;
      });
      return [...tagsToAdd, ...tagsCollection];
    }
    return tagsCollection;
  }
}
