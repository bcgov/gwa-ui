import { 
  Component, 
  Injector
} from '@angular/core';
import { Params } from '@angular/router';

import { BaseListComponent } from '../../Component/BaseListComponent';

import { Group } from '../../Group/Group';
import { ApiGroupUserService } from './ApiGroupUserService';

@Component({
  selector: 'api-group-user-list',
  templateUrl: 'ApiGroupUserList.html'
})
export class ApiGroupUserListComponent extends BaseListComponent<Group> {
  apiName : string;

  groupName : string;

  addUsername : string;

  constructor(
    injector: Injector,
    service: ApiGroupUserService
  ) {
    super(injector, service);
    this.paging = true;
  }
  

  ngOnInit() {
    this.columns = [
      { prop: 'username', name: 'User',  sortable: false },
      { prop: 'created_at', name: 'Created At', cellTemplate: this.dateTemplate, sortable: false },
      { prop: 'actions', name: 'Actions', cellTemplate: this.actionsTemplate, sortable: false }
    ];
    this.route.params
      .map(params => {
        return params;
      })
      .subscribe(params => {
        this.apiName = params['apiName'];
        this.groupName = params['groupName'];
        this.path = `/apis/${this.apiName}/groups/${this.groupName}/users`;
        this.refresh();
      });
  }
  
  addUser() {
    const group = this.service.newObject();
    group.group = this.groupName;
    this.service.addObject(group, `/apis/${this.apiName}/groups/${this.groupName}/users/${this.addUsername}`)
      .then((savedGroup) => {
        this.refresh();
      });
  }

  get groupEditable() {
    if (this.groupName.indexOf('idir') == 0) {
      return false;
    } else if (this.groupName.indexOf('github') == 0) {
      return false;
    } else {
      return true;
    }
  }
}