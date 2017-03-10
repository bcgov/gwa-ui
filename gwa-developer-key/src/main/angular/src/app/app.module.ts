import { NgModule }       from '@angular/core';
import { BrowserModule }  from '@angular/platform-browser';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import { HttpModule }    from '@angular/http';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { ModalModule} from 'ng2-bootstrap/modal';
import { BootstrapModalModule } from 'ng2-bootstrap-modal';
import {ConfirmationPopoverModule} from 'angular-confirmation-popover';
import { AppRoutingModule } from './app-routing.module';
import { AuthService } from './Authentication/AuthService';
import {
  BcGovTemplate,
  BcGovTemplateModule
} from './bcgov-template/index';
import { BsFormModule } from './bs-form/index';
import { MessageDialog } from './Component/MessageDialog';
import { PageNotFoundComponent } from './Component/PageNotFoundComponent';
import { ApiKeyService } from './ApiKey/ApiKeyService';
import { ApiKeyListComponent } from './ApiKey/ApiKeyListComponent';
import { ApiService } from './Api/ApiService';
import { ApiListComponent } from './Api/ApiListComponent';
import { ApiAuthorizeComponent } from './Api/ApiAuthorizeComponent';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    NgxDatatableModule,
    ModalModule.forRoot(),
    BootstrapModalModule,
    BcGovTemplateModule.forRoot({
        title: 'GW Dev Keys',
        headerMenuItems: [
          {
            title: 'API Keys',
            routerLink: 'app/apiKeys'
          },
          {
            title: 'APIs',
            routerLink: 'app/apis'
          },
        ]
      }
    ),
    AppRoutingModule,
    ConfirmationPopoverModule.forRoot({
      confirmButtonType: 'danger'
    }),
    BsFormModule.forRoot()
  ],
  entryComponents: [
    MessageDialog
  ],
  declarations: [
    ApiKeyListComponent,
    ApiListComponent,
    ApiAuthorizeComponent,
    PageNotFoundComponent,
    MessageDialog
  ],
  providers: [
    AuthService,
    ApiKeyService,
    ApiService,
  ],
  bootstrap: [ BcGovTemplate ]
})
export class AppModule { }

