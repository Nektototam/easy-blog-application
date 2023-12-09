import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { UsersDetailComponent } from './users-detail.component';

describe('Users Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsersDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: UsersDetailComponent,
              resolve: { users: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(UsersDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load users on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', UsersDetailComponent);

      // THEN
      expect(instance.users).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
