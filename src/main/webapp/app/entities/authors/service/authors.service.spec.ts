import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IAuthors } from '../authors.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../authors.test-samples';

import { AuthorsService } from './authors.service';

const requireRestSample: IAuthors = {
  ...sampleWithRequiredData,
};

describe('Authors Service', () => {
  let service: AuthorsService;
  let httpMock: HttpTestingController;
  let expectedResult: IAuthors | IAuthors[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(AuthorsService);
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

    it('should create a Authors', () => {
      const authors = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(authors).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Authors', () => {
      const authors = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(authors).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Authors', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Authors', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Authors', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Authors', () => {
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

    describe('addAuthorsToCollectionIfMissing', () => {
      it('should add a Authors to an empty array', () => {
        const authors: IAuthors = sampleWithRequiredData;
        expectedResult = service.addAuthorsToCollectionIfMissing([], authors);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(authors);
      });

      it('should not add a Authors to an array that contains it', () => {
        const authors: IAuthors = sampleWithRequiredData;
        const authorsCollection: IAuthors[] = [
          {
            ...authors,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAuthorsToCollectionIfMissing(authorsCollection, authors);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Authors to an array that doesn't contain it", () => {
        const authors: IAuthors = sampleWithRequiredData;
        const authorsCollection: IAuthors[] = [sampleWithPartialData];
        expectedResult = service.addAuthorsToCollectionIfMissing(authorsCollection, authors);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(authors);
      });

      it('should add only unique Authors to an array', () => {
        const authorsArray: IAuthors[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const authorsCollection: IAuthors[] = [sampleWithRequiredData];
        expectedResult = service.addAuthorsToCollectionIfMissing(authorsCollection, ...authorsArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const authors: IAuthors = sampleWithRequiredData;
        const authors2: IAuthors = sampleWithPartialData;
        expectedResult = service.addAuthorsToCollectionIfMissing([], authors, authors2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(authors);
        expect(expectedResult).toContain(authors2);
      });

      it('should accept null and undefined values', () => {
        const authors: IAuthors = sampleWithRequiredData;
        expectedResult = service.addAuthorsToCollectionIfMissing([], null, authors, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(authors);
      });

      it('should return initial array if no Authors is added', () => {
        const authorsCollection: IAuthors[] = [sampleWithRequiredData];
        expectedResult = service.addAuthorsToCollectionIfMissing(authorsCollection, undefined, null);
        expect(expectedResult).toEqual(authorsCollection);
      });
    });

    describe('compareAuthors', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAuthors(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareAuthors(entity1, entity2);
        const compareResult2 = service.compareAuthors(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareAuthors(entity1, entity2);
        const compareResult2 = service.compareAuthors(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareAuthors(entity1, entity2);
        const compareResult2 = service.compareAuthors(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
