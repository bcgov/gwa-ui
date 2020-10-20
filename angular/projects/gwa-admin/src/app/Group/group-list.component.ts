import {
  Component,
  Injector
} from '@angular/core';

import {BaseListComponent} from 'revolsys-angular-framework';

import {Group} from '../Group/Group';
import {GroupService} from './group.service';

@Component({
  selector: 'admin-group-list',
  templateUrl: 'group-list.component.html'
})
export class GroupListComponent extends BaseListComponent<Group> {

  addGroupName: string;

  constructor(
    injector: Injector,
    protected groupService: GroupService
  ) {
    super(injector, groupService, 'Groups - Gateway Admin');
    this.columnNames = ['group', 'actions'];
  }

  addGroup() {
    let hasGroup = false;
    const groups = this.arrayDataSource.data;
    for (const group of groups) {
      if (group.group === this.addGroupName) {
        hasGroup = true;
      }
    }
    if (hasGroup) {
      this.router.navigate([this.addGroupName], {relativeTo: this.route});
    } else {
      this.groupService.addObject({
        group: this.addGroupName
      }).subscribe(group => {
        if (group) {
          this.router.navigate([group.group], {relativeTo: this.route});
        }
      });
    }
  }
}
