import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ITags } from '../tags.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../tags.test-samples';

import { TagsService } from './tags.service';

const requireRestSample: ITags = {
  ...sampleWithRequiredData,
};

describe('Tags Service', () => {
  let service: TagsService;
  let httpMock: HttpTestingController;
  let expectedResult: ITags | ITags[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(TagsService);
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

    it('should create a Tags', () => {
      const tags = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(tags).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Tags', () => {
      const tags = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(tags).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Tags', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Tags', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Tags', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Tags', () => {
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

    describe('addTagsToCollectionIfMissing', () => {
      it('should add a Tags to an empty array', () => {
        const tags: ITags = sampleWithRequiredData;
        expectedResult = service.addTagsToCollectionIfMissing([], tags);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(tags);
      });

      it('should not add a Tags to an array that contains it', () => {
        const tags: ITags = sampleWithRequiredData;
        const tagsCollection: ITags[] = [
          {
            ...tags,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addTagsToCollectionIfMissing(tagsCollection, tags);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Tags to an array that doesn't contain it", () => {
        const tags: ITags = sampleWithRequiredData;
        const tagsCollection: ITags[] = [sampleWithPartialData];
        expectedResult = service.addTagsToCollectionIfMissing(tagsCollection, tags);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(tags);
      });

      it('should add only unique Tags to an array', () => {
        const tagsArray: ITags[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const tagsCollection: ITags[] = [sampleWithRequiredData];
        expectedResult = service.addTagsToCollectionIfMissing(tagsCollection, ...tagsArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const tags: ITags = sampleWithRequiredData;
        const tags2: ITags = sampleWithPartialData;
        expectedResult = service.addTagsToCollectionIfMissing([], tags, tags2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(tags);
        expect(expectedResult).toContain(tags2);
      });

      it('should accept null and undefined values', () => {
        const tags: ITags = sampleWithRequiredData;
        expectedResult = service.addTagsToCollectionIfMissing([], null, tags, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(tags);
      });

      it('should return initial array if no Tags is added', () => {
        const tagsCollection: ITags[] = [sampleWithRequiredData];
        expectedResult = service.addTagsToCollectionIfMissing(tagsCollection, undefined, null);
        expect(expectedResult).toEqual(tagsCollection);
      });
    });

    describe('compareTags', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareTags(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareTags(entity1, entity2);
        const compareResult2 = service.compareTags(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareTags(entity1, entity2);
        const compareResult2 = service.compareTags(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareTags(entity1, entity2);
        const compareResult2 = service.compareTags(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
