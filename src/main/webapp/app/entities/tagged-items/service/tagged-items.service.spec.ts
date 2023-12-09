import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ITaggedItems } from '../tagged-items.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../tagged-items.test-samples';

import { TaggedItemsService } from './tagged-items.service';

const requireRestSample: ITaggedItems = {
  ...sampleWithRequiredData,
};

describe('TaggedItems Service', () => {
  let service: TaggedItemsService;
  let httpMock: HttpTestingController;
  let expectedResult: ITaggedItems | ITaggedItems[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(TaggedItemsService);
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

    it('should create a TaggedItems', () => {
      const taggedItems = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(taggedItems).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a TaggedItems', () => {
      const taggedItems = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(taggedItems).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a TaggedItems', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of TaggedItems', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a TaggedItems', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a TaggedItems', () => {
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

    describe('addTaggedItemsToCollectionIfMissing', () => {
      it('should add a TaggedItems to an empty array', () => {
        const taggedItems: ITaggedItems = sampleWithRequiredData;
        expectedResult = service.addTaggedItemsToCollectionIfMissing([], taggedItems);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(taggedItems);
      });

      it('should not add a TaggedItems to an array that contains it', () => {
        const taggedItems: ITaggedItems = sampleWithRequiredData;
        const taggedItemsCollection: ITaggedItems[] = [
          {
            ...taggedItems,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addTaggedItemsToCollectionIfMissing(taggedItemsCollection, taggedItems);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a TaggedItems to an array that doesn't contain it", () => {
        const taggedItems: ITaggedItems = sampleWithRequiredData;
        const taggedItemsCollection: ITaggedItems[] = [sampleWithPartialData];
        expectedResult = service.addTaggedItemsToCollectionIfMissing(taggedItemsCollection, taggedItems);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(taggedItems);
      });

      it('should add only unique TaggedItems to an array', () => {
        const taggedItemsArray: ITaggedItems[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const taggedItemsCollection: ITaggedItems[] = [sampleWithRequiredData];
        expectedResult = service.addTaggedItemsToCollectionIfMissing(taggedItemsCollection, ...taggedItemsArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const taggedItems: ITaggedItems = sampleWithRequiredData;
        const taggedItems2: ITaggedItems = sampleWithPartialData;
        expectedResult = service.addTaggedItemsToCollectionIfMissing([], taggedItems, taggedItems2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(taggedItems);
        expect(expectedResult).toContain(taggedItems2);
      });

      it('should accept null and undefined values', () => {
        const taggedItems: ITaggedItems = sampleWithRequiredData;
        expectedResult = service.addTaggedItemsToCollectionIfMissing([], null, taggedItems, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(taggedItems);
      });

      it('should return initial array if no TaggedItems is added', () => {
        const taggedItemsCollection: ITaggedItems[] = [sampleWithRequiredData];
        expectedResult = service.addTaggedItemsToCollectionIfMissing(taggedItemsCollection, undefined, null);
        expect(expectedResult).toEqual(taggedItemsCollection);
      });
    });

    describe('compareTaggedItems', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareTaggedItems(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareTaggedItems(entity1, entity2);
        const compareResult2 = service.compareTaggedItems(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareTaggedItems(entity1, entity2);
        const compareResult2 = service.compareTaggedItems(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareTaggedItems(entity1, entity2);
        const compareResult2 = service.compareTaggedItems(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
