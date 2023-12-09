import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IUsers } from '../users.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../users.test-samples';

import { UsersService } from './users.service';

const requireRestSample: IUsers = {
  ...sampleWithRequiredData,
};

describe('Users Service', () => {
  let service: UsersService;
  let httpMock: HttpTestingController;
  let expectedResult: IUsers | IUsers[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(UsersService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Users', () => {
      const users = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(users).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Users', () => {
      const users = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(users).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Users', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Users', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Users', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Users', () => {
      const queryObject: any = {
        page: 0,
        size: 20,
        query: '',
        sort: [],
      };
      service.search(queryObject).subscribe(() => expectedResult);

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
      expect(expectedResult).toBe(null);
    });

    describe('addUsersToCollectionIfMissing', () => {
      it('should add a Users to an empty array', () => {
        const users: IUsers = sampleWithRequiredData;
        expectedResult = service.addUsersToCollectionIfMissing([], users);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(users);
      });

      it('should not add a Users to an array that contains it', () => {
        const users: IUsers = sampleWithRequiredData;
        const usersCollection: IUsers[] = [
          {
            ...users,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addUsersToCollectionIfMissing(usersCollection, users);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Users to an array that doesn't contain it", () => {
        const users: IUsers = sampleWithRequiredData;
        const usersCollection: IUsers[] = [sampleWithPartialData];
        expectedResult = service.addUsersToCollectionIfMissing(usersCollection, users);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(users);
      });

      it('should add only unique Users to an array', () => {
        const usersArray: IUsers[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const usersCollection: IUsers[] = [sampleWithRequiredData];
        expectedResult = service.addUsersToCollectionIfMissing(usersCollection, ...usersArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const users: IUsers = sampleWithRequiredData;
        const users2: IUsers = sampleWithPartialData;
        expectedResult = service.addUsersToCollectionIfMissing([], users, users2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(users);
        expect(expectedResult).toContain(users2);
      });

      it('should accept null and undefined values', () => {
        const users: IUsers = sampleWithRequiredData;
        expectedResult = service.addUsersToCollectionIfMissing([], null, users, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(users);
      });

      it('should return initial array if no Users is added', () => {
        const usersCollection: IUsers[] = [sampleWithRequiredData];
        expectedResult = service.addUsersToCollectionIfMissing(usersCollection, undefined, null);
        expect(expectedResult).toEqual(usersCollection);
      });
    });

    describe('compareUsers', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareUsers(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareUsers(entity1, entity2);
        const compareResult2 = service.compareUsers(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareUsers(entity1, entity2);
        const compareResult2 = service.compareUsers(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareUsers(entity1, entity2);
        const compareResult2 = service.compareUsers(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
