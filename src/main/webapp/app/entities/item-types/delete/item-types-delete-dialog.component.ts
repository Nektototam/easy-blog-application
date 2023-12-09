import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IItemTypes } from '../item-types.model';
import { ItemTypesService } from '../service/item-types.service';

@Component({
  standalone: true,
  templateUrl: './item-types-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ItemTypesDeleteDialogComponent {
  itemTypes?: IItemTypes;

  constructor(
    protected itemTypesService: ItemTypesService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.itemTypesService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
