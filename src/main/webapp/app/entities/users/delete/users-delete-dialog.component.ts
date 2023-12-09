import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IUsers } from '../users.model';
import { UsersService } from '../service/users.service';

@Component({
  standalone: true,
  templateUrl: './users-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class UsersDeleteDialogComponent {
  users?: IUsers;

  constructor(
    protected usersService: UsersService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.usersService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
