import {
    Component,
    Injector,
    Inject,
    Input,
    OnInit
  } from '@angular/core';

import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';

@Component({
    templateUrl: 'new-dialog.html',
})
export class NewNamespaceDialogComponent {
  name = null

  constructor(@Inject(MAT_DIALOG_DATA) public data: any, public dialogRef: MatDialogRef<NewNamespaceDialogComponent>) { }

  ngOnInit() {
      this.name = ""
  }

  closeDialog() {
    this.dialogRef.close(false);
  }

  create() {
    this.dialogRef.close(this.name);
  }

}