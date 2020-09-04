import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {
  RoleGuard,
  PageNotFoundComponent
} from 'revolsys-angular-framework';

import {ApiAddComponent} from './Api/api-add.component';
import {ApiListComponent} from './Api/api-list.component';
import {ApiViewTabsComponent} from './Api/api-view-tabs.component';
import {ApiViewComponent} from './Api/api-view.component';
import {ApiResolver} from './Api/api.resolver';

import {ApiGroupListComponent} from './Api/Group/api-group-list.component';
import {ApiGroupUserListComponent} from './Api/Group/api-group-user-list.component';

import {ApiPluginListComponent} from './Api/Plugin/api-plugin-list.component';
import {ApiPluginViewComponent} from './Api/Plugin/api-plugin-view.component';
import {ApiPluginResolver} from './Api/Plugin/api-plugin.resolver';

import {ApiPluginUserListComponent} from './Api/Plugin/User/api-plugin-user-list.component';
import {ApiPluginUserViewComponent} from './Api/Plugin/User/api-plugin-user-view.component';
import {ApiPluginUserResolver} from './Api/Plugin/User/api-plugin-user.resolver';

import {UserAddComponent} from './User/user-add.component';
import {UserListComponent} from './User/user-list.component';
import {UserDetailComponent} from './User/user-view-tabs.component';
import {UserViewComponent} from './User/user-view.component';
import {UserResolver} from './User/user-resolver';

import {UserDataNameListComponent} from './User/Data/user-data-name-list.component';
import {UserDataListTabsComponent} from './User/Data/user-data-list-tabs.component';
import {UserDataViewTabsComponent} from './User/Data/user-data-view-tabs.component';

import {UserGroupListComponent} from './User/Group/user-group-list.component';

import {UserPluginListComponent} from './User/Plugin/user-plugin-list.component';

import {EndpointListComponent} from './Endpoint/endpoint-list.component';
import {EndpointViewTabsComponent} from './Endpoint/endpoint-view-tabs.component';
import {EndpointResolver} from './Endpoint/endpoint.resolver';
import {EndpointViewComponent} from './Endpoint/endpoint-view.component';

import {EndpointGroupListComponent} from './Endpoint/Group/endpoint-group-list.component';
import {EndpointGroupUserListComponent} from './Endpoint/Group/endpoint-group-user-list.component';

import {GroupUserListComponent} from './Group/group-user-list.component';

import {GroupListComponent} from './Group/group-list.component';

import {PluginNameListComponent} from './Plugin/plugin-name-list.component';
import {PluginListComponent} from './Plugin/plugin-list.component';

import {StatusViewComponent} from './Status/status-view.component';

import {ImportExportComponent} from './import-export/import-export.component';

const routes: Routes = [
  {path: '', redirectTo: 'ui/endpoints', pathMatch: 'full'},
  {path: 'ui', redirectTo: 'ui/endpoints', pathMatch: 'full'},

  {path: 'ui/apis', component: ApiListComponent, canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},
  {path: 'ui/apis/_add_', component: ApiAddComponent, pathMatch: 'full', canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},
  {
    path: 'ui/apis/:apiName',
    component: ApiViewTabsComponent,
    canActivate: [RoleGuard],
    data: {roles: ['gwa_admin']},
    resolve: {api: ApiResolver},
    children: [
      {path: '', component: ApiViewComponent, pathMatch: 'full'},
      {path: 'plugins', component: ApiPluginListComponent, pathMatch: 'full'},
      {path: 'groups', component: ApiGroupListComponent, pathMatch: 'full'},
    ]
  },
  {path: 'ui/apis/:apiName/groups/:groupName', component: ApiGroupUserListComponent},
  {
    path: 'ui/apis/:apiName/plugins/:pluginName',
    component: ApiPluginViewComponent,
    canActivate: [RoleGuard],
    data: {roles: ['gwa_admin']},
    resolve: {plugin: ApiPluginResolver}
  },

  {
    path: 'ui/apis/:apiName/plugins/:pluginName/users',
    component: ApiPluginUserListComponent,
    canActivate: [RoleGuard],
    data: {roles: ['gwa_admin']},
    resolve: {api: ApiResolver}
  },

  {
    path: 'ui/apis/:apiName/plugins/:pluginName/users/:username',
    component: ApiPluginUserViewComponent,
    canActivate: [RoleGuard],
    data: {roles: ['gwa_admin']},
    resolve: {plugin: ApiPluginUserResolver}
  },

  {path: 'ui/users', component: UserListComponent, canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},
  {path: 'ui/users/_add_', component: UserAddComponent, pathMatch: 'full', canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},
  {
    path: 'ui/users/:username',
    component: UserDetailComponent,
    canActivate: [RoleGuard],
    data: {roles: ['gwa_admin']},
    resolve: {user: UserResolver},
    children: [
      {path: '', component: UserViewComponent, pathMatch: 'full'},
      {path: 'data', component: UserDataNameListComponent, pathMatch: 'full'},
      {path: 'groups', component: UserGroupListComponent, pathMatch: 'full'},
      {path: 'plugins', component: UserPluginListComponent, pathMatch: 'full'},
    ]
  },

  {path: 'ui/users/:username/data/:dataName', component: UserDataListTabsComponent, canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},

  {
    path: 'ui/users/:username/data/:dataName/:id',
    component: UserDataViewTabsComponent,
    canActivate: [RoleGuard],
    data: {roles: ['gwa_admin']}
  },

  {path: 'ui/endpoints', component: EndpointListComponent},
  {
    path: 'ui/endpoints/:apiName',
    component: EndpointViewTabsComponent,
    resolve: {api: EndpointResolver},
    children: [
      {path: '', component: EndpointViewComponent, pathMatch: 'full'},
      {path: 'groups', component: EndpointGroupListComponent, pathMatch: 'full'},
    ]
  },
  {path: 'ui/groups', component: GroupListComponent },
  {path: 'ui/groups/:groupName', component: GroupUserListComponent },

  {path: 'ui/endpoints/:apiName/groups/:groupName', component: EndpointGroupUserListComponent},
  
  {path: 'ui/plugins', component: PluginNameListComponent, canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},
  {path: 'ui/plugins/:pluginName', component: PluginListComponent, canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},

  {path: 'ui/status', component: StatusViewComponent, canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},

  {path: 'ui/importExport', component: ImportExportComponent, canActivate: [RoleGuard], data: {roles: ['gwa_admin']}},

  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule {
}
