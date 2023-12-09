import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IPages } from '../pages.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../pages.test-samples';

import { PagesService, RestPages } from './pages.service';

const requireRestSample: RestPages = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
  updatedAt: sampleWithRequiredData.updatedAt?.toJSON(),
  publishedAt: sampleWithRequiredData.publishedAt?.toJSON(),
};

describe('Pages Service', () => {
  let service: PagesService;
  let httpMock: HttpTestingController;
  let expectedResult: IPages | IPages[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(PagesService);
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

    it('should create a Pages', () => {
      const pages = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(pages).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Pages', () => {
      const pages = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(pages).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Pages', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Pages', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Pages', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Pages', () => {
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

    describe('addPagesToCollectionIfMissing', () => {
      it('should add a Pages to an empty array', () => {
        const pages: IPages = sampleWithRequiredData;
        expectedResult = service.addPagesToCollectionIfMissing([], pages);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(pages);
      });

      it('should not add a Pages to an array that contains it', () => {
        const pages: IPages = sampleWithRequiredData;
        const pagesCollection: IPages[] = [
          {
            ...pages,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addPagesToCollectionIfMissing(pagesCollection, pages);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Pages to an array that doesn't contain it", () => {
        const pages: IPages = sampleWithRequiredData;
        const pagesCollection: IPages[] = [sampleWithPartialData];
        expectedResult = service.addPagesToCollectionIfMissing(pagesCollection, pages);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(pages);
      });

      it('should add only unique Pages to an array', () => {
        const pagesArray: IPages[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const pagesCollection: IPages[] = [sampleWithRequiredData];
        expectedResult = service.addPagesToCollectionIfMissing(pagesCollection, ...pagesArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const pages: IPages = sampleWithRequiredData;
        const pages2: IPages = sampleWithPartialData;
        expectedResult = service.addPagesToCollectionIfMissing([], pages, pages2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(pages);
        expect(expectedResult).toContain(pages2);
      });

      it('should accept null and undefined values', () => {
        const pages: IPages = sampleWithRequiredData;
        expectedResult = service.addPagesToCollectionIfMissing([], null, pages, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(pages);
      });

      it('should return initial array if no Pages is added', () => {
        const pagesCollection: IPages[] = [sampleWithRequiredData];
        expectedResult = service.addPagesToCollectionIfMissing(pagesCollection, undefined, null);
        expect(expectedResult).toEqual(pagesCollection);
      });
    });

    describe('comparePages', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.comparePages(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.comparePages(entity1, entity2);
        const compareResult2 = service.comparePages(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.comparePages(entity1, entity2);
        const compareResult2 = service.comparePages(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.comparePages(entity1, entity2);
        const compareResult2 = service.comparePages(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
