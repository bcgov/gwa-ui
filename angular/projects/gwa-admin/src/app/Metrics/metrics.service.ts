import {Observable} from 'rxjs';
import {
  Injectable,
  Injector
} from '@angular/core';

import {BaseService} from 'revolsys-angular-framework';

@Injectable()
export class MetricsService extends BaseService<any> {
  grafanaBaseUrl: string;

  constructor(injector: Injector) {
    super(injector);
    const url = this.getUrl('/authentication');
    this.httpRequest(
      http => {
        return http.get(url);
      },
      json => json
    ).subscribe(result => {
      if (result) {
          this.grafanaBaseUrl = result.config['grafanaBaseUrl'];
      }
    });
  }
}
