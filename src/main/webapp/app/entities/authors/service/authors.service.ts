import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IAuthors, NewAuthors } from '../authors.model';

export type PartialUpdateAuthors = Partial<IAuthors> & Pick<IAuthors, 'id'>;

export type EntityResponseType = HttpResponse<IAuthors>;
export type EntityArrayResponseType = HttpResponse<IAuthors[]>;

@Injectable({ providedIn: 'root' })
export class AuthorsService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/authors');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/authors/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(authors: NewAuthors): Observable<EntityResponseType> {
    return this.http.post<IAuthors>(this.resourceUrl, authors, { observe: 'response' });
  }

  update(authors: IAuthors): Observable<EntityResponseType> {
    return this.http.put<IAuthors>(`${this.resourceUrl}/${this.getAuthorsIdentifier(authors)}`, authors, { observe: 'response' });
  }

  partialUpdate(authors: PartialUpdateAuthors): Observable<EntityResponseType> {
    return this.http.patch<IAuthors>(`${this.resourceUrl}/${this.getAuthorsIdentifier(authors)}`, authors, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IAuthors>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IAuthors[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IAuthors[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<IAuthors[]>()], asapScheduler)));
  }

  getAuthorsIdentifier(authors: Pick<IAuthors, 'id'>): number {
    return authors.id;
  }

  compareAuthors(o1: Pick<IAuthors, 'id'> | null, o2: Pick<IAuthors, 'id'> | null): boolean {
    return o1 && o2 ? this.getAuthorsIdentifier(o1) === this.getAuthorsIdentifier(o2) : o1 === o2;
  }

  addAuthorsToCollectionIfMissing<Type extends Pick<IAuthors, 'id'>>(
    authorsCollection: Type[],
    ...authorsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const authors: Type[] = authorsToCheck.filter(isPresent);
    if (authors.length > 0) {
      const authorsCollectionIdentifiers = authorsCollection.map(authorsItem => this.getAuthorsIdentifier(authorsItem)!);
      const authorsToAdd = authors.filter(authorsItem => {
        const authorsIdentifier = this.getAuthorsIdentifier(authorsItem);
        if (authorsCollectionIdentifiers.includes(authorsIdentifier)) {
          return false;
        }
        authorsCollectionIdentifiers.push(authorsIdentifier);
        return true;
      });
      return [...authorsToAdd, ...authorsCollection];
    }
    return authorsCollection;
  }
}
