import {
  Component,
  Injector
} from '@angular/core';

import {ApiListComponent} from '../Api/api-list.component';
import {ApiService} from '../Api/api.service';

@Component({
  selector: 'admin-endpoint-list',
  templateUrl: 'endpoint-list.component.html'
})
export class EndpointListComponent extends ApiListComponent {

  constructor(
    injector: Injector,
    protected apiService: ApiService
  ) {
    super(injector, apiService);
    this.columnNames = ['name', 'tags', 'hosts', 'paths'];
    this.setTitle('Endpoints - Gateway Admin');
    this.path = '/endpoints';
  }
}
