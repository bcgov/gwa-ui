import {Observable} from 'rxjs';
import {
  Injectable,
  Injector
} from '@angular/core';

import {BaseService} from 'revolsys-angular-framework';

import {ServiceAccount} from './ServiceAccount';

@Injectable()
export class ServiceAccountService extends BaseService<ServiceAccount> {

  constructor(injector: Injector) {
    super(injector);
    this.path = '/serviceAccounts';
    this.typeTitle = 'Service Account';
    this.labelFieldName = 'key';
    this.usePostForDelete = false;
  }


  addObject(serviceAccount: ServiceAccount, path?: string): Observable<ServiceAccount> {
    return this.addObjectDo(
      '/serviceAccounts',
      serviceAccount
    );
  }

  deleteObject(serviceAccount: ServiceAccount): Observable<boolean> {
    return this.deleteObjectDo(
      `/serviceAccounts/${serviceAccount.key}`
    );
  }

  updateObject(serviceAccount: ServiceAccount): Observable<ServiceAccount> {
    return this.updateObjectDo(
      `/serviceAccounts/${serviceAccount.key}`,
      serviceAccount
    );
  }

  public getUrl(path: string): string {
    return window.location.protocol + '//' + window.location.host + this.config.basePath + '/rest' + path;
  }

  newObject(): ServiceAccount {
    return new ServiceAccount();
  }
}
