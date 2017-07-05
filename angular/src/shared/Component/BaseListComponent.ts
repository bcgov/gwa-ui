import {
  Injector,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {
  MdDialog,
  MdDialogRef
} from '@angular/material';

import { BaseComponent } from './BaseComponent';
import { DeleteDialogComponent } from './DeleteDialogComponent';

import { Service } from '../Service/Service';

export class BaseListComponent<T> extends BaseComponent<T> implements OnInit {

  @ViewChild('idT') idTemplate: TemplateRef<any>;

  @ViewChild('actionsT') actionsTemplate: TemplateRef<any>;

  @ViewChild('dateT') dateTemplate: TemplateRef<any>;

  @ViewChild('flagT') flagTemplate: TemplateRef<any>;

  @ViewChild('arrayT') arrayTemplate: TemplateRef<any>;

  columns: any[];

  dialog: MdDialog = this.injector.get(MdDialog);

  refreshingCount = 0;

  rows: Array<T> = [];

  count = 0;

  offset = 0;

  limit = 100;

  filterFields: any[];

  filterFieldName: string;

  filterValue: string;

  filter: { [fieldName: string]: string } = {};

  paging = false;

  path: string;

  cssClasses = {
    sortAscending: 'fa fa-chevron-down',
    sortDescending: 'fa fa-chevron-up',
    pagerLeftArrow: 'fa fa-chevron-left',
    pagerRightArrow: 'fa fa-chevron-right',
    pagerPrevious: 'fa fa-step-backward',
    pagerNext: 'fa fa-step-forward'
  };

  constructor(injector: Injector, service: Service<T>) {
    super(injector, service);
  }

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    if (this.paging) {
      this.page(this.offset, this.limit);
    } else {
      this.refreshingCount++;
      const filter = this.newFilter();
      this.service.getObjects(this.path, filter).then(objects => {
        this.rows = objects;
        this.refreshingCount--;
      });
    }
  }

  deleteObject(object: T): void {
    const dialogRef = this.dialog.open(DeleteDialogComponent, {
      data: {
        typeTitle: this.service.getTypeTitle(),
        objectLabel: this.service.getLabel(object),
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'Delete') {
        this.deleteObjectDo(object);
      }
    });
  }

  protected deleteObjectDo(object: T): void {
    this.service.deleteObject(object, this.path)
      .then((deleted) => {
        if (deleted) {
          this.onDeleted(object);
        }
      })
      ;
  }

  onDeleted(object: T): void {
    if (this.paging) {
      this.refresh();
    } else {
      this.rows = this.rows.filter(row => row !== object);
    }
  }

  getRows(): Array<T> {
    return this.rows;
  }

  page(offset: number, limit: number) {
    this.refreshingCount++;
    this.fetch(offset, limit, (results: any) => {
      this.refreshingCount--;
      this.count = results.count;
      this.rows = results.rows;
    });
  }

  newFilter(): { [fieldName: string]: string } {
    const filter: { [fieldName: string]: string } = {};
    if (this.filter) {
      for (const fieldName of Object.keys(this.filter)) {
        filter[fieldName] = this.filter[fieldName];
      }
    }
    if (this.filterFieldName) {
      filter[this.filterFieldName] = this.filterValue;
    }
    return filter;
  }

  fetch(offset: number, limit: number, callback: any) {
    const filter = this.newFilter();
    this.service.getRowsPage(
      offset,
      limit,
      this.path,
      this.filter
    ).then(callback);
  }

  onPage(event: any) {
    this.page(event.offset, event.limit);
  }
}