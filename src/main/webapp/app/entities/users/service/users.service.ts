import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IUsers, NewUsers } from '../users.model';

export type PartialUpdateUsers = Partial<IUsers> & Pick<IUsers, 'id'>;

export type EntityResponseType = HttpResponse<IUsers>;
export type EntityArrayResponseType = HttpResponse<IUsers[]>;

@Injectable({ providedIn: 'root' })
export class UsersService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/users');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/users/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(users: NewUsers): Observable<EntityResponseType> {
    return this.http.post<IUsers>(this.resourceUrl, users, { observe: 'response' });
  }

  update(users: IUsers): Observable<EntityResponseType> {
    return this.http.put<IUsers>(`${this.resourceUrl}/${this.getUsersIdentifier(users)}`, users, { observe: 'response' });
  }

  partialUpdate(users: PartialUpdateUsers): Observable<EntityResponseType> {
    return this.http.patch<IUsers>(`${this.resourceUrl}/${this.getUsersIdentifier(users)}`, users, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IUsers>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IUsers[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IUsers[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<IUsers[]>()], asapScheduler)));
  }

  getUsersIdentifier(users: Pick<IUsers, 'id'>): number {
    return users.id;
  }

  compareUsers(o1: Pick<IUsers, 'id'> | null, o2: Pick<IUsers, 'id'> | null): boolean {
    return o1 && o2 ? this.getUsersIdentifier(o1) === this.getUsersIdentifier(o2) : o1 === o2;
  }

  addUsersToCollectionIfMissing<Type extends Pick<IUsers, 'id'>>(
    usersCollection: Type[],
    ...usersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const users: Type[] = usersToCheck.filter(isPresent);
    if (users.length > 0) {
      const usersCollectionIdentifiers = usersCollection.map(usersItem => this.getUsersIdentifier(usersItem)!);
      const usersToAdd = users.filter(usersItem => {
        const usersIdentifier = this.getUsersIdentifier(usersItem);
        if (usersCollectionIdentifiers.includes(usersIdentifier)) {
          return false;
        }
        usersCollectionIdentifiers.push(usersIdentifier);
        return true;
      });
      return [...usersToAdd, ...usersCollection];
    }
    return usersCollection;
  }
}
