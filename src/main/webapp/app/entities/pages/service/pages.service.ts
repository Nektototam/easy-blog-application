import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError, map } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IPages, NewPages } from '../pages.model';

export type PartialUpdatePages = Partial<IPages> & Pick<IPages, 'id'>;

type RestOf<T extends IPages | NewPages> = Omit<T, 'createdAt' | 'updatedAt' | 'publishedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
  publishedAt?: string | null;
};

export type RestPages = RestOf<IPages>;

export type NewRestPages = RestOf<NewPages>;

export type PartialUpdateRestPages = RestOf<PartialUpdatePages>;

export type EntityResponseType = HttpResponse<IPages>;
export type EntityArrayResponseType = HttpResponse<IPages[]>;

@Injectable({ providedIn: 'root' })
export class PagesService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/pages');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/pages/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(pages: NewPages): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(pages);
    return this.http.post<RestPages>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(pages: IPages): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(pages);
    return this.http
      .put<RestPages>(`${this.resourceUrl}/${this.getPagesIdentifier(pages)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(pages: PartialUpdatePages): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(pages);
    return this.http
      .patch<RestPages>(`${this.resourceUrl}/${this.getPagesIdentifier(pages)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestPages>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPages[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestPages[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),
      catchError(() => scheduled([new HttpResponse<IPages[]>()], asapScheduler)),
    );
  }

  getPagesIdentifier(pages: Pick<IPages, 'id'>): number {
    return pages.id;
  }

  comparePages(o1: Pick<IPages, 'id'> | null, o2: Pick<IPages, 'id'> | null): boolean {
    return o1 && o2 ? this.getPagesIdentifier(o1) === this.getPagesIdentifier(o2) : o1 === o2;
  }

  addPagesToCollectionIfMissing<Type extends Pick<IPages, 'id'>>(
    pagesCollection: Type[],
    ...pagesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const pages: Type[] = pagesToCheck.filter(isPresent);
    if (pages.length > 0) {
      const pagesCollectionIdentifiers = pagesCollection.map(pagesItem => this.getPagesIdentifier(pagesItem)!);
      const pagesToAdd = pages.filter(pagesItem => {
        const pagesIdentifier = this.getPagesIdentifier(pagesItem);
        if (pagesCollectionIdentifiers.includes(pagesIdentifier)) {
          return false;
        }
        pagesCollectionIdentifiers.push(pagesIdentifier);
        return true;
      });
      return [...pagesToAdd, ...pagesCollection];
    }
    return pagesCollection;
  }

  protected convertDateFromClient<T extends IPages | NewPages | PartialUpdatePages>(pages: T): RestOf<T> {
    return {
      ...pages,
      createdAt: pages.createdAt?.toJSON() ?? null,
      updatedAt: pages.updatedAt?.toJSON() ?? null,
      publishedAt: pages.publishedAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restPages: RestPages): IPages {
    return {
      ...restPages,
      createdAt: restPages.createdAt ? dayjs(restPages.createdAt) : undefined,
      updatedAt: restPages.updatedAt ? dayjs(restPages.updatedAt) : undefined,
      publishedAt: restPages.publishedAt ? dayjs(restPages.publishedAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestPages>): HttpResponse<IPages> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestPages[]>): HttpResponse<IPages[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
