import {
  Component,
  Injector,
  OnInit
} from '@angular/core';
import {Params} from '@angular/router';
import {BaseDetailComponent} from 'revolsys-angular-framework';
import {StatusService} from './status.service';
import {MatTableDataSource} from '@angular/material';

@Component({
  selector: 'admin-status-detail',
  template: `
    <h2>Server Status</h2>

    <table mat-table  #serverTable [dataSource]="serverDataSource" >
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Name</th>
        <td mat-cell *matCellDef="let record">{{record.name}}</td>
      </ng-container>
      <ng-container matColumnDef="text">
        <th mat-header-cell *matHeaderCellDef>Value</th>
        <td mat-cell *matCellDef="let record">{{record.text}}</td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columnNames"></tr>
      <tr mat-row *matRowDef="let row; columns: columnNames"></tr>
    </table> 

    <h2>Database Status</h2>

    <table mat-table  #databaseTable [dataSource]="databaseDataSource" >
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Name</th>
        <td mat-cell *matCellDef="let record">{{record.name}}</td>
      </ng-container>
      <ng-container matColumnDef="text">
        <th mat-header-cell *matHeaderCellDef>Value</th>
        <td mat-cell *matCellDef="let record">{{record.text}}</td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columnNames"></tr>
      <tr mat-row *matRowDef="let row; columns: columnNames"></tr>
    </table> 
  `
})
export class StatusViewComponent extends BaseDetailComponent<any> implements OnInit {

  columnNames = ['name', 'text'];

  databaseDataSource = new MatTableDataSource<any>();

  serverDataSource = new MatTableDataSource<any>();

  constructor(
    injector: Injector,
    protected statusService: StatusService
  ) {
    super(injector, statusService, 'Status - Gateway Admin');
  }

  ngOnInit() {
    this.route.params
      .subscribe(params => {
        this.statusService.getStatus().subscribe(statusRecord => {
          this.object = statusRecord;
          if (statusRecord) {
            const serverRecords = [];
            for (const name of Object.keys(statusRecord.server)) {
              serverRecords.push({
                name: name,
                text: statusRecord.server[name].toString()
              });
            }
            this.serverDataSource.data = serverRecords;
            const databaseRecords = [];
            for (const name of Object.keys(statusRecord.database)) {
              databaseRecords.push({
                name: name,
                text: statusRecord.database[name].toString()
              });
            }
            this.databaseDataSource.data = databaseRecords;
          }
        });
      });
  }
}
