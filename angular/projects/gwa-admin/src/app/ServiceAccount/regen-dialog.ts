import {
    Component,
    Injector,
    Inject,
    Input,
    OnInit
  } from '@angular/core';

import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';

@Component({
    templateUrl: 'regen-dialog.html',
})
export class RegDialogComponent {
  serviceAccountKey = null

  constructor(@Inject(MAT_DIALOG_DATA) public data: any, public dialogRef: MatDialogRef<RegDialogComponent>) { }

  ngOnInit() {
    this.serviceAccountKey = this.data.serviceAccount.key;
  }

  closeDialog() {
    this.dialogRef.close(false);
  }

  regenerate() {
    this.dialogRef.close(true);
  }

}