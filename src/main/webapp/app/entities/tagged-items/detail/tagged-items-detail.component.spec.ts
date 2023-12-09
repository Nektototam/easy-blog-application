import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { TaggedItemsDetailComponent } from './tagged-items-detail.component';

describe('TaggedItems Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaggedItemsDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: TaggedItemsDetailComponent,
              resolve: { taggedItems: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(TaggedItemsDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load taggedItems on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', TaggedItemsDetailComponent);

      // THEN
      expect(instance.taggedItems).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
