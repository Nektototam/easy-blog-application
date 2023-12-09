import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { AuthorsDetailComponent } from './authors-detail.component';

describe('Authors Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthorsDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: AuthorsDetailComponent,
              resolve: { authors: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(AuthorsDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load authors on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', AuthorsDetailComponent);

      // THEN
      expect(instance.authors).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
