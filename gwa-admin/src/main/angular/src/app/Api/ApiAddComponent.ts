import {
  Component, 
  Injector
} from '@angular/core';
import { Api } from './Api';
import { ApiService } from './ApiService';
import { ApiDetailComponent } from './ApiDetailComponent';

@Component({
  selector: 'api-add',
  templateUrl: 'ApiDetail.html'
})
export class ApiAddComponent extends ApiDetailComponent {

  constructor(
    protected injector:Injector,
    protected service: ApiService
  ) {
    super(injector, service);
  }

  postSave(savedApi: Api): void {
    this.router.navigate(['/app/apis', savedApi.name]);
  }

}