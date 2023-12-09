import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IAuthors } from '../authors.model';
import { AuthorsService } from '../service/authors.service';

@Component({
  standalone: true,
  templateUrl: './authors-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class AuthorsDeleteDialogComponent {
  authors?: IAuthors;

  constructor(
    protected authorsService: AuthorsService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.authorsService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
