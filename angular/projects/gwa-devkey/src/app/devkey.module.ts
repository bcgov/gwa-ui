import {NgModule, APP_INITIALIZER} from '@angular/core';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {CdkTableModule} from '@angular/cdk/table';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {
  MatButtonModule,
  MatCheckboxModule,
  MatCardModule,
  MatDialogModule,
  MatIconModule,
  MatProgressSpinnerModule,
  MatSelectModule,
  MatTableModule,
  MatToolbarModule
} from '@angular/material';
import {MatExpansionModule} from '@angular/material/expansion';
import {BrowserModule} from '@angular/platform-browser';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';

import {RevolsysAngularBcgovPageModule} from 'revolsys-angular-bcgov-page';
import {RevolsysAngularFrameworkModule} from 'revolsys-angular-framework';

import {DevKeyRoutingModule} from './devkey-routing.module';

import {ApiService} from './Api/api.service';
import {DevKeyComponent} from './devkey.component';
import {ApiListComponent} from './Api/api-list.component';
import {RateLimitService} from './Api/rateLimit.service';

import {ApiKeyService} from './ApiKey/api-key.service';
import {ApiKeyListComponent} from './ApiKey/api-key-list.component';
import {DevKeySecurityService} from './devkey-security.service';

@NgModule({
  imports: [
    BrowserModule,
    NoopAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,

    CdkTableModule,

    MatAutocompleteModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatExpansionModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    MatToolbarModule,

    RevolsysAngularFrameworkModule.forRoot({
      basePath: '',
      title: 'API Keys'
    }),
    RevolsysAngularBcgovPageModule.forRoot({
      basePath: '/',
      title: 'API Keys',
      fullWidthContent: true
    }),
    DevKeyRoutingModule
  ],
  entryComponents: [
  ],
  declarations: [
    DevKeyComponent,
    ApiListComponent,
    ApiKeyListComponent
  ],
  providers: [
    ApiService,
    ApiKeyService,
    RateLimitService,
    DevKeySecurityService,
    {
      // Initializes security service
      provide: APP_INITIALIZER,
      useFactory: (ds: DevKeySecurityService) => function() {
        return null;
      },
      deps: [DevKeySecurityService],
      multi: true
    },
  ],
  bootstrap: [DevKeyComponent]
})
export class DevKeyModule {
}

