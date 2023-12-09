import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { TaggedItemsService } from '../service/tagged-items.service';

import { TaggedItemsComponent } from './tagged-items.component';

describe('TaggedItems Management Component', () => {
  let comp: TaggedItemsComponent;
  let fixture: ComponentFixture<TaggedItemsComponent>;
  let service: TaggedItemsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{ path: 'tagged-items', component: TaggedItemsComponent }]),
        HttpClientTestingModule,
        TaggedItemsComponent,
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
      .overrideTemplate(TaggedItemsComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(TaggedItemsComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(TaggedItemsService);

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
    expect(comp.taggedItems?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to taggedItemsService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getTaggedItemsIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getTaggedItemsIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
