import {
  Component,
  Injector,
  Input,
  TemplateRef,
  ViewChild
} from '@angular/core';

import { BaseListComponent } from '../../../shared/Component/BaseListComponent';

import { Api } from '../Api';
import { PluginListComponent } from '../../Plugin/PluginListComponent';
import { PluginService } from '../../Plugin/PluginService';

@Component({
  selector: 'app-api-plugin-list',
  templateUrl: 'ApiPluginList.html'
})
export class ApiPluginListComponent extends PluginListComponent {
  api: Api;

  pluginNames: string[];

  pluginName: string;

  constructor(
    injector: Injector,
    service: PluginService
  ) {
    super(injector, service);
    service.getPluginNames().then(pluginNames => {
      this.pluginNames = pluginNames;
      if (pluginNames && pluginNames.length > 0) {
        this.pluginName = this.pluginNames[0];
      }
    });
    this.showApi = false;
    this.showUser = false;
  }

  initParams(): void {
    this.route.parent.data.subscribe((data: { api: Api }) => {
      this.api = data.api;
      this.path = `/apis/${this.api.id}/plugins`;
      this.refresh();
    }
    );
  }
}
