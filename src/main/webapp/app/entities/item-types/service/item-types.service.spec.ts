import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IItemTypes } from '../item-types.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../item-types.test-samples';

import { ItemTypesService } from './item-types.service';

const requireRestSample: IItemTypes = {
  ...sampleWithRequiredData,
};

describe('ItemTypes Service', () => {
  let service: ItemTypesService;
  let httpMock: HttpTestingController;
  let expectedResult: IItemTypes | IItemTypes[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(ItemTypesService);
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

    it('should create a ItemTypes', () => {
      const itemTypes = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(itemTypes).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a ItemTypes', () => {
      const itemTypes = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(itemTypes).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a ItemTypes', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of ItemTypes', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a ItemTypes', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a ItemTypes', () => {
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

    describe('addItemTypesToCollectionIfMissing', () => {
      it('should add a ItemTypes to an empty array', () => {
        const itemTypes: IItemTypes = sampleWithRequiredData;
        expectedResult = service.addItemTypesToCollectionIfMissing([], itemTypes);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(itemTypes);
      });

      it('should not add a ItemTypes to an array that contains it', () => {
        const itemTypes: IItemTypes = sampleWithRequiredData;
        const itemTypesCollection: IItemTypes[] = [
          {
            ...itemTypes,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addItemTypesToCollectionIfMissing(itemTypesCollection, itemTypes);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a ItemTypes to an array that doesn't contain it", () => {
        const itemTypes: IItemTypes = sampleWithRequiredData;
        const itemTypesCollection: IItemTypes[] = [sampleWithPartialData];
        expectedResult = service.addItemTypesToCollectionIfMissing(itemTypesCollection, itemTypes);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(itemTypes);
      });

      it('should add only unique ItemTypes to an array', () => {
        const itemTypesArray: IItemTypes[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const itemTypesCollection: IItemTypes[] = [sampleWithRequiredData];
        expectedResult = service.addItemTypesToCollectionIfMissing(itemTypesCollection, ...itemTypesArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const itemTypes: IItemTypes = sampleWithRequiredData;
        const itemTypes2: IItemTypes = sampleWithPartialData;
        expectedResult = service.addItemTypesToCollectionIfMissing([], itemTypes, itemTypes2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(itemTypes);
        expect(expectedResult).toContain(itemTypes2);
      });

      it('should accept null and undefined values', () => {
        const itemTypes: IItemTypes = sampleWithRequiredData;
        expectedResult = service.addItemTypesToCollectionIfMissing([], null, itemTypes, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(itemTypes);
      });

      it('should return initial array if no ItemTypes is added', () => {
        const itemTypesCollection: IItemTypes[] = [sampleWithRequiredData];
        expectedResult = service.addItemTypesToCollectionIfMissing(itemTypesCollection, undefined, null);
        expect(expectedResult).toEqual(itemTypesCollection);
      });
    });

    describe('compareItemTypes', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareItemTypes(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareItemTypes(entity1, entity2);
        const compareResult2 = service.compareItemTypes(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareItemTypes(entity1, entity2);
        const compareResult2 = service.compareItemTypes(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareItemTypes(entity1, entity2);
        const compareResult2 = service.compareItemTypes(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
