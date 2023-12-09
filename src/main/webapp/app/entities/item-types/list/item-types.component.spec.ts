import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { ItemTypesService } from '../service/item-types.service';

import { ItemTypesComponent } from './item-types.component';

describe('ItemTypes Management Component', () => {
  let comp: ItemTypesComponent;
  let fixture: ComponentFixture<ItemTypesComponent>;
  let service: ItemTypesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{ path: 'item-types', component: ItemTypesComponent }]),
        HttpClientTestingModule,
        ItemTypesComponent,
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
      .overrideTemplate(ItemTypesComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ItemTypesComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(ItemTypesService);

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
    expect(comp.itemTypes?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to itemTypesService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getItemTypesIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getItemTypesIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
