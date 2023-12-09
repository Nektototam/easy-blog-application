import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IAuthors } from '../authors.model';
import { AuthorsService } from '../service/authors.service';
import { AuthorsFormService, AuthorsFormGroup } from './authors-form.service';

@Component({
  standalone: true,
  selector: 'jhi-authors-update',
  templateUrl: './authors-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class AuthorsUpdateComponent implements OnInit {
  isSaving = false;
  authors: IAuthors | null = null;

  editForm: AuthorsFormGroup = this.authorsFormService.createAuthorsFormGroup();

  constructor(
    protected authorsService: AuthorsService,
    protected authorsFormService: AuthorsFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ authors }) => {
      this.authors = authors;
      if (authors) {
        this.updateForm(authors);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const authors = this.authorsFormService.getAuthors(this.editForm);
    if (authors.id !== null) {
      this.subscribeToSaveResponse(this.authorsService.update(authors));
    } else {
      this.subscribeToSaveResponse(this.authorsService.create(authors));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAuthors>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(authors: IAuthors): void {
    this.authors = authors;
    this.authorsFormService.resetForm(this.editForm, authors);
  }
}
