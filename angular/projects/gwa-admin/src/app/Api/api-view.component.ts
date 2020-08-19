import {GroupService} from '../Group/group.service';
import {
  Component,
  Injector,
  Input,
  OnInit,
  ElementRef,
  ViewChild
} from '@angular/core';
import {
  FormControl
} from '@angular/forms';
import {MatChipInputEvent} from '@angular/material/chips';
import {ENTER, COMMA} from '@angular/cdk/keycodes';
import {BaseDetailComponent} from 'revolsys-angular-framework';

import {Api} from './Api';
import {Plugin} from '../Plugin/Plugin';
import {ApiService} from './api.service';
import {MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';

@Component({
  selector: 'admin-api-view',
  templateUrl: 'api-view.component.html'
})
export class ApiViewComponent extends BaseDetailComponent<Api> implements OnInit {

  acl: Plugin = new Plugin(
    'acl',
    {
      allow: [],
      blacklist: []
    },
    false
  );

  endpoint: Plugin = new Plugin(
    'bcgov-gwa-endpoint',
    {
      api_owners: [],
    }
  );

  groupNameControl = new FormControl();

  @ViewChild('groupInput') groupNameInput: ElementRef;

  groupNamesAuto = [];

  keyAuth: Plugin = new Plugin(
    'key-auth',
    {
      key_names: 'apikey',
      hide_credentials: false,
      anonymous: null
    },
    false
  );

  separatorKeysCodes = [ENTER, COMMA];

  constructor(
    protected injector: Injector,
    protected service: ApiService,
    private groupService: GroupService
  ) {
    super(injector, service, 'API - Gateway Admin');
    this.idParamName = 'apiName';
  }

  groupNameSelected(event: MatAutocompleteSelectedEvent): void {
    this.acl.config.allow.push(event.option.viewValue);
    this.groupNameInput.nativeElement.value = '';
    this.groupNameControl.setValue(null);
  }

  ngOnInit() {
    if (this.addPage) {
      super.ngOnInit();
    } else {
      this.route.parent.data
        .subscribe((data: {api: Api}) => {
          this.setObject(data.api);
        }
        );
    }
    this.groupNameControl.valueChanges.subscribe((searchName: string) => {
      if (searchName) {
        this.groupService.getObjects('/groups/?groupName=' + searchName.replace(/[^a-zA-Z0-9_]/, '_'), {})
          .subscribe(groups => {
            const groupNames = [];
            for (const group of groups) {
              groupNames.push(group['group']);
            }
            this.groupNamesAuto = groupNames;
          });
      } else {
        this.groupNamesAuto = [];
      }
    });
  }

  protected setObject(api: Api) {
    if (api) {
      if (api.id) {
        this.setTitle(`API: ${api.name} - Gateway Admin`);
      } else {
        this.setTitle(`Add API - Gateway Admin`);
      }
      const endpoint = api.plugin('bcgov-gwa-endpoint');
      if (endpoint == null) {
        api.pluginAdd(this.endpoint);
      } else {
        this.endpoint = endpoint;
      }
      const keyAuth = api.plugin('key-auth');
      if (keyAuth == null) {
        api.pluginAdd(this.keyAuth);
      } else {
        this.keyAuth = keyAuth;
      }
      const acl = api.plugin('acl');
      if (acl == null) {
        api.pluginAdd(this.acl);
      } else {
        this.acl = acl;
      }
    }
    super.setObject(api);
  }

  addApiOwner(event: MatChipInputEvent): void {
    this.addValue(this.endpoint.config.api_owners, event);
  }

  addGroupName(event: MatChipInputEvent): void {
    this.addValue(this.acl.config.allow, event);
  }

  addHost(event: MatChipInputEvent): void {
    this.addValue(this.object.hosts, event);
  }

  addUri(event: MatChipInputEvent): void {
    this.addValue(this.object.uris, event);
  }

  private addValue(values: string[], event: MatChipInputEvent): void {
    const value = event.value;
    if (value) {
      const uri = value.trim();
      if (uri && values.indexOf(uri) === -1) {
        values.push(uri);
      }
    }
    event.input.value = '';
  }


  deleteApiOwner(apiOwner: string): void {
    this.deleteValue(this.endpoint.config.api_owners, apiOwner);
  }

  deleteGroupName(apiOwner: string): void {
    this.deleteValue(this.acl.config.allow, apiOwner);
  }

  deleteHost(host: string): void {
    this.deleteValue(this.object.hosts, host);
  }

  deleteUri(uri: string): void {
    this.deleteValue(this.object.uris, uri);
  }

  private deleteValue(values: string[], value: string): void {
    const index = values.indexOf(value);
    if (index >= 0) {
      values.splice(index, 1);
    }
  }

  get developerKeyGroupNames(): string[] {
    const groupNames: string[] = [];
    if (this.acl && this.acl.config.allow) {
      for (let groupName of this.acl.config.allow) {
        groupName = groupName.trim().toLowerCase();
        if (groupName.length === 0) {
        } else if (groupName.indexOf('idir') === 0) {
        } else if (groupName.indexOf('github') === 0) {
        } else {
          groupNames.push(groupName);
        }
      }
    }
    return groupNames;
  }

  routeDetail(): void {
    this.router.navigate(['..', this.object.name], {relativeTo: this.route});
  }

  formValid(): boolean {
    return (this.object.hosts && this.object.hosts.length > 0) || (this.object.uris && this.object.uris.length > 0);
  }
}
