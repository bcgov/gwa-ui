import {
  Component,
  Injector,
  Input,
  OnInit
} from '@angular/core';
import {BaseDetailComponent} from 'revolsys-angular-framework';
import {Api} from '../Api/Api';
import {EndpointService} from './endpoint.service';
import {Plugin} from '../Plugin/Plugin';

@Component({
  selector: 'admin-endpoint-view',
  templateUrl: 'endpoint-view.component.html',
  styles: [`
  .rateLimit .mat-form-field {
    max-width: 90px;
  }
`]
})
export class EndpointViewComponent extends BaseDetailComponent<Api> implements OnInit {

  acl: Plugin;

  endpoint: Plugin;

  keyAuth: Plugin;

  rateLimit: Plugin;

  constructor(
    protected injector: Injector,
    protected endpointService: EndpointService
  ) {
    super(injector, endpointService, 'Endpoint - Gateway Admin');
    this.idParamName = 'apiName';
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: {api: Api}) => {
        const api = data.api;
        if (api) {
          this.setTitle(`Endpoint: ${api.name} - Gateway Admin`);
          this.endpoint = api.plugin('bcgov-gwa-endpoint');
          if (!this.endpoint) {
              this.endpoint = new Plugin('bcgov-gwa-endpoint', {})
          }
          this.acl = api.plugin('acl');
          this.keyAuth = api.plugin('key-auth');
          let rateLimit = api.plugin('rate-limiting');
          if (rateLimit == null) {
            rateLimit = new Plugin();
            rateLimit.name = 'rate-limiting';
            rateLimit.config = {
              second: null,
              minute: 60,
              hour: null,
              day: null,
              month: null,
              year: null
            };
            api.pluginAdd(rateLimit);
          }
          this.rateLimit = rateLimit;
        }
        this.setObject(api);
      }
      );
  }
}
