import {Observable} from 'rxjs';
import {
  Injectable,
  Injector
} from '@angular/core';

import {BaseService} from 'revolsys-angular-framework';

@Injectable()
export class NamespaceService extends BaseService<any> {
  constructor(injector: Injector) {
    super(injector);
  }

  getStatus(): Observable<any> {
    return this.getObjectDo('/status');
  }

  newObject(): any {
    return {name: ""};
  }

  addObject(ns: Object): Observable<Object> {
    return this.addObjectDo(
      `/namespaces`,
      ns
    );
  }

}
