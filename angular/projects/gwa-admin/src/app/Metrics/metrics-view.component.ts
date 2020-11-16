import {
  Component,
  Injector,
  OnInit
} from '@angular/core';
import {Params} from '@angular/router';
import {BaseDetailComponent} from 'revolsys-angular-framework';
import {MetricsService} from './metrics.service';
import {MatTableDataSource} from '@angular/material';

@Component({
  styleUrls: ['metrics-view.component.css'],
  selector: 'metrics-detail',
  template: `
    <div class="rowToColumn">

        <ng-container>
            <mat-card>
                <mat-card-title>Metrics</mat-card-title>
                <mat-card-content>
                    <table mat-table [dataSource]="links">
                        <ng-container matColumnDef="label">
                            <th mat-header-cell *matHeaderCellDef> Metrics </th>
                            <td mat-cell *matCellDef="let link"> {{link.label}} </td>
                        </ng-container>
                        <ng-container matColumnDef="link">
                            <th mat-header-cell *matHeaderCellDef> Link </th>
                            <td mat-cell *matCellDef="let link"> <a target="_blank" href="{{service.grafanaBaseUrl}}{{link.link}}">{{link.link}}</a> </td>
                        </ng-container>
                        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
                    </table>
                </mat-card-content>
            </mat-card>
        </ng-container>
    </div>
  `
})
export class MetricsViewComponent extends BaseDetailComponent<any> implements OnInit {

  columnNames = ['name', 'text'];

  databaseDataSource = new MatTableDataSource<any>();

  serverDataSource = new MatTableDataSource<any>();

  links = [
      { label: 'Performance metrics (Request Rate, Latency Rate, Bandwidth), filterable by Service and Route.', link: '/d/providerperf/gateway-performance'},
      { label: 'Usage metrics (Request Counts by Consumer, HTTP Status, and User Agent), filterable by Service.', link: '/d/providerusage/gateway-usage'}
  ]
  displayedColumns: string[] = ['label', 'link'];

  grafanaBaseUrl: string;

  constructor(
    injector: Injector,
    protected metricsService: MetricsService
  ) {
    super(injector, metricsService, 'Metrics');
  }

  ngOnInit() {
    this.route.params
      .subscribe(params => {
      });  
  }
}
