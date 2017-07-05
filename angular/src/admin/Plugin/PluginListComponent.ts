import {
  Component,
  Injector,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';

import { BaseListComponent } from '../../shared/Component/BaseListComponent';

import { Plugin } from './Plugin';
import { PluginService } from './PluginService';

@Component({
  selector: 'app-plugin-list',
  templateUrl: 'PluginList.html'
})
export class PluginListComponent extends BaseListComponent<Plugin> implements OnInit {

  pluginName: string;

  @ViewChild('apiT') apiTemplate: TemplateRef<any>;

  @ViewChild('userT') userTemplate: TemplateRef<any>;

  showUser = true;

  showApi = true;

  showPlugin = true;

  constructor(
    injector: Injector,
    service: PluginService
  ) {
    super(injector, service);
  }

  initParams(): void {
    this.route.params.subscribe(params => {
      this.pluginName = params['pluginName'];
      this.path = `/plugins/${this.pluginName}`;
      this.refresh();
    });
  }

  ngOnInit(): void {
    this.columns = [];
    if (this.showApi) {
      this.columns.push({ prop: 'api_name', name: 'API', cellTemplate: this.apiTemplate, sortable: true });
    }
    if (this.showPlugin) {
      this.columns.push({ prop: 'name', name: 'Plugin', cellTemplate: this.idTemplate, sortable: true });
    }
    if (this.showUser) {
      this.columns.push({ prop: 'user_username', name: 'User', cellTemplate: this.userTemplate, sortable: true });
    }
    this.columns.push({ prop: 'enabled', name: 'Enabled', cellTemplate: this.flagTemplate, sortable: true });
    this.columns.push({ prop: 'created_at', name: 'Created At', cellTemplate: this.dateTemplate, sortable: true });
    this.columns.push({ prop: 'actions', name: 'Actions', cellTemplate: this.actionsTemplate, sortable: false });
    this.initParams();
  }
}