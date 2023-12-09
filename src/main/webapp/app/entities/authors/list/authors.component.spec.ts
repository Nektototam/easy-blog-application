import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { AuthorsService } from '../service/authors.service';

import { AuthorsComponent } from './authors.component';

describe('Authors Management Component', () => {
  let comp: AuthorsComponent;
  let fixture: ComponentFixture<AuthorsComponent>;
  let service: AuthorsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{ path: 'authors', component: AuthorsComponent }]),
        HttpClientTestingModule,
        AuthorsComponent,
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
      .overrideTemplate(AuthorsComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(AuthorsComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(AuthorsService);

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
    expect(comp.authors?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to authorsService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getAuthorsIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getAuthorsIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
