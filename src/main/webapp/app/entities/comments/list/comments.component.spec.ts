import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { CommentsService } from '../service/comments.service';

import { CommentsComponent } from './comments.component';

describe('Comments Management Component', () => {
  let comp: CommentsComponent;
  let fixture: ComponentFixture<CommentsComponent>;
  let service: CommentsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{ path: 'comments', component: CommentsComponent }]),
        HttpClientTestingModule,
        CommentsComponent,
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({
              defaultSort: 'id,asc',
            }),
            queryParamMap: of(
              jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
              }),
            ),
            snapshot: { queryParams: {} },
          },
        },
      ],
    })
      .overrideTemplate(CommentsComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(CommentsComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(CommentsService);

    const headers = new HttpHeaders();
    jest.spyOn(service, 'query').mockReturnValue(
      of(
        new HttpResponse({
          body: [{ id: 123 }],
          headers,
        }),
      ),
    );
  });

  it('Should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.comments?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to commentsService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getCommentsIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getCommentsIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
