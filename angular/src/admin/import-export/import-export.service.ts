import {
  Injector,
  Injectable
} from '@angular/core';
import {BaseService} from '../../shared/Service/BaseService';

import {ImportExport} from './import-export';
@Injectable()
export class ImportExportService extends BaseService<ImportExport> {

  constructor(injector: Injector) {
    super(injector);
    this.path = `/importExport`;
    this.typeTitle = 'Import/Export';
  }

  newObject(): ImportExport {
    return new ImportExport();
  }

}
