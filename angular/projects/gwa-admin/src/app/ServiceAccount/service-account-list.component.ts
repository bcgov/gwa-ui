import {
  Component,
  Injector,
  Input,
  OnInit
} from '@angular/core';

import {RegDialogComponent} from './regen-dialog';

import {BaseListComponent} from 'revolsys-angular-framework';

import {ServiceAccount} from './ServiceAccount';
import {ServiceAccountService} from './service-account.service';


@Component({
  selector: 'devkey-service-account-list',
  templateUrl: 'service-account-list.component.html',
  styleUrls: ['service-account-list.component.css']
})
export class ServiceAccountListComponent extends BaseListComponent<ServiceAccount> implements OnInit {
  acceptTerms = false;

  serviceAccount: ServiceAccount;

  appName: string;

  appRedirectUrl: string;

  appSendMessage = false;

  hasServiceAccount = false;

  constructor(
    injector: Injector,
    service: ServiceAccountService,
  ) {
    super(injector, service, 'Service Accounts');
    this.service = service;
    this.columnNames = ['key', 'scope', 'actions'];
  }

  ngOnInit(): void {
    this.route.queryParams
      .subscribe(params => {
        this.appName = params['appName'];
        this.appRedirectUrl = params['appRedirectUrl'];
        this.appSendMessage = params['appSendMessage'] === 'true';
        this.refresh();
      });
    super.ngOnInit();
  }

  addServiceAccount(): void {
    const serviceAccount: ServiceAccount = new ServiceAccount();
    this.service.addObject(
      serviceAccount
    ).subscribe(
      serviceAccount2 => {
        this.hasServiceAccount = true;
        this.refresh();
        this.serviceAccount = serviceAccount2;
      }
      );
  }

  deleteServiceAccount(serviceAccount: ServiceAccount): void {
    let s = this.service;
    this.deleteObject(serviceAccount);
  }

  regenerateCredentials(serviceAccount: ServiceAccount): void {
    let s = this.service;
    let dialogRef = this.dialog.open(RegDialogComponent, {
        height: '300px',
        width: '400px',
        data: {
            serviceAccount: serviceAccount
        }
    });
    dialogRef.afterClosed().subscribe(result => {
        if (result) {
            s.updateObject(
                serviceAccount
            ).subscribe(
                serviceAccount2 => {
                  this.hasServiceAccount = true;
                  this.refresh();
                  this.serviceAccount = serviceAccount2;
                }
            );
        }
    });      
  }

  authorizeAccess(): void {
    const key = this.serviceAccount.key;
    if (this.appSendMessage) {
      let messageWindow = window.opener;
      if (!messageWindow) {
        messageWindow = window.parent;
      }
      messageWindow.postMessage(key, '*');
    } else {
      let url = this.appRedirectUrl;
      if (url.indexOf('?') === -1) {
        url += '?';
      } else {
        url += '&';
      }
      url += 'serviceAccount=' + key;
      this.document.location.href = url;
    }
  }

  onDeleted(serviceAccount: ServiceAccount): void {
    super.onDeleted(serviceAccount);
    const records = this.arrayDataSource.data;
    if (records.length === 0) {
      this.hasServiceAccount = false;
      this.serviceAccount = null;
    } else {
      let setServiceAccount = true;
      if (this.serviceAccount) {
        for (const row of records) {
          if (this.serviceAccount === row) {
            setServiceAccount = false;
          }
        }
      }
      if (setServiceAccount) {
        this.serviceAccount = records[0];
      }
    }
  }

  protected setRows(records: ServiceAccount[]) {
    this.arrayDataSource.data = records;
    if (records.length > 0) {
      this.hasServiceAccount = true;
      let setServiceAccount = true;
      if (this.serviceAccount) {
        for (const record of records) {
          if (this.serviceAccount.key === record.key) {
            this.serviceAccount = record;
            setServiceAccount = false;
          }
        }
      }
      if (setServiceAccount) {
        this.serviceAccount = records[0];
      }
      this.acceptTerms = true;
    } else {
      this.hasServiceAccount = false;
    }
  }

  
}
