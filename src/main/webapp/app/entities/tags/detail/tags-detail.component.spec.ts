import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { TagsDetailComponent } from './tags-detail.component';

describe('Tags Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TagsDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: TagsDetailComponent,
              resolve: { tags: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(TagsDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load tags on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', TagsDetailComponent);

      // THEN
      expect(instance.tags).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
