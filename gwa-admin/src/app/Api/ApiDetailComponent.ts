import {
  Component,
  OnInit
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Api } from './Api';

@Component({
  selector: 'app-api-detail',
  template: `
<nav md-tab-nav-bar *ngIf="api">
  <a md-tab-link
     [routerLink]="['.']"
     routerLinkActive
     #rla1="routerLinkActive"
     [active]="rla1.isActive"
     [routerLinkActiveOptions]="{exact:true}"
  >API: {{api.name}}</a>
  <a md-tab-link
     [routerLink]="['plugins']"
     routerLinkActive
     #rla2="routerLinkActive"
     [active]="rla2.isActive"
     [routerLinkActiveOptions]="{exact:true}"
  >Plugins</a>
  <a md-tab-link
     [routerLink]="['groups']"
     routerLinkActive
     #rla3="routerLinkActive"
     [active]="rla3.isActive"
     [routerLinkActiveOptions]="{exact:true}"
  >Groups</a>
</nav>
<router-outlet></router-outlet>
  `
})
export class ApiDetailComponent implements OnInit {
  api: Api;

  constructor(
    protected route: ActivatedRoute,
  ) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { api: Api }) => {
        this.api = data.api;
      }
      );
  }
}
