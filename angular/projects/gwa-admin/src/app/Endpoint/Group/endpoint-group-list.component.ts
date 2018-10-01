import {
  Component,
  Injector,
  OnInit
} from '@angular/core';

import {BaseListComponent} from 'revolsys-angular-framework';

import {Api} from '../../Api/Api';
import {Group} from '../../Group/Group';

@Component({
  selector: 'admin-endpoint-group-list',
  template: `
  <table mat-table  #table [dataSource]="dataSource" >

    <ng-container matColumnDef="group">
      <th mat-header-cell *matHeaderCellDef>Group</th>
      <td mat-cell *matCellDef="let record">
        <a [routerLink]="['/ui','endpoints', api.name, 'groups', record.group]">{{record.group}}</a></td>
    </ng-container>

    <tr mat-header-row *matHeaderRowDef="columnNames"></tr>
    <tr mat-row *matRowDef="let row; columns: columnNames"></tr>
  </table> 
  <mat-paginator
    #paginator
    [length]="recordCount | async"
    [pageSize]="pageSize"
    [hidePageSize]="true"
    [showFirstLastButtons]="true">
  </mat-paginator>
`
})
export class EndpointGroupListComponent extends BaseListComponent<Group> implements OnInit {
  api: Api;

  constructor(
    injector: Injector,
  ) {
    super(injector, null, 'Endpoint Groups - Gateway Admin');
    this.columnNames = ['group'];
  }

  ngOnInit(): void {
    this.route.parent.data
      .subscribe((data: {api: Api}) => {
        this.api = data.api;
        const endpoint = this.api.plugin('acl');
        if (endpoint) {
          const groupNames = endpoint.config.whitelist;
          if (groupNames) {
            const records = [];
            for (const groupName of groupNames) {
              records.push({group: groupName});
            }
            this.arrayDataSource.data = records;
          }
        }
      }
      );
  }
}
