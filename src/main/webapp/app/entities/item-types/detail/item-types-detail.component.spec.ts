import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { ItemTypesDetailComponent } from './item-types-detail.component';

describe('ItemTypes Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItemTypesDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: ItemTypesDetailComponent,
              resolve: { itemTypes: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(ItemTypesDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load itemTypes on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ItemTypesDetailComponent);

      // THEN
      expect(instance.itemTypes).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
