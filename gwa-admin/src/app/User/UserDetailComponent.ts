import {
  Component,
  OnInit
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { User } from './User';

@Component({
  selector: 'app-user-detail',
  template: `
<nav md-tab-nav-bar *ngIf="user">
  <a md-tab-link
     [routerLink]="['.']"
     routerLinkActive
     #rla1="routerLinkActive"
     [active]="rla1.isActive"
     [routerLinkActiveOptions]="{exact:true}"
  >User: {{user.username}}</a>
  <a md-tab-link
     [routerLink]="['groups']"
     routerLinkActive
     #rla2="routerLinkActive"
     [active]="rla2.isActive"
     [routerLinkActiveOptions]="{exact:true}"
  >Groups</a>
  <a md-tab-link
     [routerLink]="['plugins']"
     routerLinkActive
     #rla3="routerLinkActive"
     [active]="rla3.isActive"
     [routerLinkActiveOptions]="{exact:true}"
  >Plugins</a>
</nav>
<router-outlet></router-outlet>
  `
})
export class UserDetailComponent implements OnInit {
  user: User;

  constructor(
    protected route: ActivatedRoute,
  ) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { user: User }) => {
        this.user = data.user;
      }
      );
  }
}
