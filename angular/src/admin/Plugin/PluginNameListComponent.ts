import {
  Component,
  Injector,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';

import {DataSource} from '@angular/cdk';

import { BaseListComponent } from '../../shared/Component/BaseListComponent';

import { Plugin } from './Plugin';
import { PluginService } from './PluginService';

@Component({
  selector: 'app-plugin-name-list',
  templateUrl: 'PluginNameList.html'
})
export class PluginNameListComponent extends BaseListComponent<Plugin> implements OnInit {
  constructor(
    injector: Injector,
    service: PluginService
  ) {
    super(injector, service, 'Plugins - Gateway Admin');
  }

  ngOnInit(): void {
    this.columns = [
      { prop: 'name', name: 'Plugin', cellTemplate: this.idTemplate, sortable: true },
    ];
    super.ngOnInit();
  }
}
