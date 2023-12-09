import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IItemTypes } from '../item-types.model';
import { ItemTypesService } from '../service/item-types.service';
import { ItemTypesFormService, ItemTypesFormGroup } from './item-types-form.service';

@Component({
  standalone: true,
  selector: 'jhi-item-types-update',
  templateUrl: './item-types-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ItemTypesUpdateComponent implements OnInit {
  isSaving = false;
  itemTypes: IItemTypes | null = null;

  editForm: ItemTypesFormGroup = this.itemTypesFormService.createItemTypesFormGroup();

  constructor(
    protected itemTypesService: ItemTypesService,
    protected itemTypesFormService: ItemTypesFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ itemTypes }) => {
      this.itemTypes = itemTypes;
      if (itemTypes) {
        this.updateForm(itemTypes);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const itemTypes = this.itemTypesFormService.getItemTypes(this.editForm);
    if (itemTypes.id !== null) {
      this.subscribeToSaveResponse(this.itemTypesService.update(itemTypes));
    } else {
      this.subscribeToSaveResponse(this.itemTypesService.create(itemTypes));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IItemTypes>>): void {
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

  protected updateForm(itemTypes: IItemTypes): void {
    this.itemTypes = itemTypes;
    this.itemTypesFormService.resetForm(this.editForm, itemTypes);
  }
}
