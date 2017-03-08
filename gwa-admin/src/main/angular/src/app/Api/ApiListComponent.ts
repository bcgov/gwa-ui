import { 
  Component, 
  Injector
} from '@angular/core';

import { BaseListComponent } from '../Component/BaseListComponent';

import { Api } from './Api';
import { ApiService } from './ApiService';

@Component({
  selector: 'api-list',
  templateUrl: 'ApiList.html'
})
export class ApiListComponent extends BaseListComponent<Api> {

  constructor(
    injector: Injector,
    protected apiService: ApiService
  ) {
    super(injector, apiService);
  }

  ngOnInit(): void {
    this.columns = [
      { name: 'Name', cellTemplate: this.idTemplate },
      { name: 'Title' },
      { prop: 'created_by', name: 'Created By' },
      { name: 'Enabled', cellTemplate: this.flagTemplate },
      { name: 'Actions', cellTemplate: this.actionsTemplate, sortable: false }
    ];
    super.ngOnInit();
  }

}