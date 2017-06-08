import {
  Component,
  Injector,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';

import { BaseListComponent } from '../Component/BaseListComponent';

import { Api } from './Api';
import { ApiService } from './ApiService';

@Component({
  selector: 'api-list',
  templateUrl: 'ApiList.html'
})
export class ApiListComponent extends BaseListComponent<Api> implements OnInit {

  @ViewChild('upstreamT') upstreamTemplate: TemplateRef<any>;

  constructor(
    injector: Injector,
    protected apiService: ApiService
  ) {
    super(injector, apiService);
    this.paging = true;
    this.filterFields = [
      { prop: 'name', name: 'Name' },
      { prop: 'hosts', name: 'Host' },
      { prop: 'uris', name: 'Path' },
    ];
    this.filterFieldName = 'name';
  }

  ngOnInit(): void {
    this.columns = [
      { name: 'Name', cellTemplate: this.idTemplate, sortable: false },
      { prop: 'hosts', name: 'Hosts', cellTemplate: this.arrayTemplate, sortable: false },
      { prop: 'uris', name: 'Paths', cellTemplate: this.arrayTemplate, sortable: false },
      { prop: 'created_at', name: 'Created At', cellTemplate: this.dateTemplate, sortable: false },
      { name: 'Actions', cellTemplate: this.actionsTemplate, sortable: false }
    ];
    super.ngOnInit();
  }
}
