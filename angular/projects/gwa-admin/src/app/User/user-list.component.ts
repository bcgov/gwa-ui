import {
  Component,
  Injector
} from '@angular/core';

import {BaseListComponent} from 'revolsys-angular-framework';

import {User} from './User';
import {UserService} from './user.service';

@Component({
  selector: 'admin-user-list',
  templateUrl: 'user-list.component.html',
  styles: [`
.mat-column-custom_id,
.mat-column-id {
  max-width: 280px;
  min-width: 280px;
}
  `]
})
export class UserListComponent extends BaseListComponent<User> {

  constructor(
    injector: Injector,
    service: UserService
  ) {
    super(injector, service, 'Users - Gateway Admin');
    this.columnNames = ['id', 'username', 'custom_id', 'created_at', 'actions'];
    this.filterFields = [
      // {prop: 'username', name: 'Username'},
      {prop: 'custom_id', name: 'Custom ID'},
    ];
    this.filterFieldName = 'username';
  }

  newFilter(): {[fieldName: string]: string} {
    const filter: {[fieldName: string]: string} = {};
    if (this.filter) {
      for (const fieldName of Object.keys(this.filter)) {
        filter[fieldName] = this.filter[fieldName];
      }
    }
    if (this.filterFieldName && !(this.filterValue == null)) {
      filter[this.filterFieldName] = this.filterValue;
    }
    return filter;
  }

}
