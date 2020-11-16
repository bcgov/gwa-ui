import {
  Component,
  Injector,
  OnInit
} from '@angular/core';
import {Params} from '@angular/router';
import {BaseDetailComponent} from 'revolsys-angular-framework';
import {NamespaceService} from './namespace.service';
import {MatTableDataSource} from '@angular/material';
import {NewNamespaceDialogComponent} from './new-dialog';

@Component({
  styleUrls: ['namespace-view.component.css'],
  selector: 'namespace-detail',
  template: `
    <div class="rowToColumn">

        <ng-container>
            <mat-card>
                <mat-card-title>Namespaces</mat-card-title>
                <mat-card-content>
                    <mat-card *ngIf="success" style="margin-bottom:10px">
                        Namespace created.  <a href="logout">Logout</a> to select your new namespace.
                    </mat-card>
                    <mat-toolbar>
                        <button (click)="newNamespace()" mat-raised-button color="primary">Create Namespace</button>
                    </mat-toolbar>
                </mat-card-content>
            </mat-card>
        </ng-container>
    </div>
  `
})
export class NamespaceViewComponent extends BaseDetailComponent<any> implements OnInit {

  columnNames = ['name', 'text'];

  databaseDataSource = new MatTableDataSource<any>();

  serverDataSource = new MatTableDataSource<any>();

  response: string;

  success: boolean;

  constructor(
    injector: Injector,
    protected namespaceService: NamespaceService
  ) {
    super(injector, namespaceService, 'Namespaces');
  }

  ngOnInit() {
    this.route.params
      .subscribe(params => {

      });
  }

  newNamespace(): void {
    let s = this.service;
    let dialogRef = this.dialog.open(NewNamespaceDialogComponent, {
        height: '300px',
        width: '400px',
        data: {
        }
    });

    dialogRef.afterClosed().subscribe(result => {
        if (result) {
            s.addObject(
                {name : result}
            ).subscribe(
                response => {
                  this.response = response;
                  this.success = response ? true:false;
                  console.log(response)
                }
            );
        }
    });
  }
}
