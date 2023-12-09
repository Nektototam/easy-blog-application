import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IPosts } from '../posts.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../posts.test-samples';

import { PostsService, RestPosts } from './posts.service';

const requireRestSample: RestPosts = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
  updatedAt: sampleWithRequiredData.updatedAt?.toJSON(),
  publishedAt: sampleWithRequiredData.publishedAt?.toJSON(),
};

describe('Posts Service', () => {
  let service: PostsService;
  let httpMock: HttpTestingController;
  let expectedResult: IPosts | IPosts[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(PostsService);
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

    it('should create a Posts', () => {
      const posts = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(posts).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Posts', () => {
      const posts = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(posts).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Posts', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Posts', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Posts', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Posts', () => {
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

    describe('addPostsToCollectionIfMissing', () => {
      it('should add a Posts to an empty array', () => {
        const posts: IPosts = sampleWithRequiredData;
        expectedResult = service.addPostsToCollectionIfMissing([], posts);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(posts);
      });

      it('should not add a Posts to an array that contains it', () => {
        const posts: IPosts = sampleWithRequiredData;
        const postsCollection: IPosts[] = [
          {
            ...posts,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addPostsToCollectionIfMissing(postsCollection, posts);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Posts to an array that doesn't contain it", () => {
        const posts: IPosts = sampleWithRequiredData;
        const postsCollection: IPosts[] = [sampleWithPartialData];
        expectedResult = service.addPostsToCollectionIfMissing(postsCollection, posts);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(posts);
      });

      it('should add only unique Posts to an array', () => {
        const postsArray: IPosts[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const postsCollection: IPosts[] = [sampleWithRequiredData];
        expectedResult = service.addPostsToCollectionIfMissing(postsCollection, ...postsArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const posts: IPosts = sampleWithRequiredData;
        const posts2: IPosts = sampleWithPartialData;
        expectedResult = service.addPostsToCollectionIfMissing([], posts, posts2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(posts);
        expect(expectedResult).toContain(posts2);
      });

      it('should accept null and undefined values', () => {
        const posts: IPosts = sampleWithRequiredData;
        expectedResult = service.addPostsToCollectionIfMissing([], null, posts, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(posts);
      });

      it('should return initial array if no Posts is added', () => {
        const postsCollection: IPosts[] = [sampleWithRequiredData];
        expectedResult = service.addPostsToCollectionIfMissing(postsCollection, undefined, null);
        expect(expectedResult).toEqual(postsCollection);
      });
    });

    describe('comparePosts', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.comparePosts(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.comparePosts(entity1, entity2);
        const compareResult2 = service.comparePosts(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.comparePosts(entity1, entity2);
        const compareResult2 = service.comparePosts(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.comparePosts(entity1, entity2);
        const compareResult2 = service.comparePosts(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
