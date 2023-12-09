import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPages } from '../pages.model';
import { PagesService } from '../service/pages.service';

@Component({
  standalone: true,
  templateUrl: './pages-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PagesDeleteDialogComponent {
  pages?: IPages;

  constructor(
    protected pagesService: PagesService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.pagesService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
