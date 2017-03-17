import 'rxjs/add/operator/toPromise';
import {
  Injectable,
  Injector
} from '@angular/core';

import {
  Location
} from '@angular/common';

import {
  Headers, 
  Http,
  URLSearchParams
} from '@angular/http';

import {
  MdDialog,
  MdDialogRef
} from '@angular/material';

import { Service } from './Service';

import { MessageDialog } from '../Component/MessageDialog';

@Injectable()
export abstract class BaseService<T> implements Service<T> {
  protected http: Http = this.injector.get(Http);

  protected location: Location = this.injector.get(Location);

  protected path : string;

  protected typeTitle : string;

  protected labelFieldName : string;

  protected idFieldName : string = "id";

  dialog: MdDialog = this.injector.get(MdDialog);

  private jsonHeaders = {
    headers: new Headers({ 'Content-Type': 'application/json' })
  };

  constructor(
    protected injector : Injector,
   ) {
  }

  addObject(object: T): Promise<T> {
    return this.addObjectDo(
      this.path,
      object
    );
  }
  
  protected addObjectDo(path: string, object: T, callback?: () => void): Promise<T> {
    const url = this.getUrl(path);
    const jsonText = JSON.stringify(object);
    return this.http.post(
      url,
      jsonText,
      this.jsonHeaders
    ).toPromise()
      .then(response => {
        const json = response.json();
        if (json.error) {
          this.showError(json.error);
          return null;
        } else {
          Object.assign(object, json);
          if (callback) {
            callback();
          }
          return object;
        }
      })
      .catch(this.handleError.bind(this));
  }

  addOrUpdateObject(object: T): Promise<T> {
    if (object == null) {
      return Promise.resolve(object);
    } else if (object[this.idFieldName]) {
      return this.updateObject(object);
    } else {  
      return this.addObject(object);
    }
  }

  protected handleError(error: any): Promise<any> {
    this.showError(error.message || error);
    return Promise.reject(error.message || error);
  }

  protected showError(message: string) {
    let dialogRef = this.dialog.open(MessageDialog, {
      data: {
        title: 'Error',
        message: message,
      }
    });
  }

  protected getUrl(path: string): string {
    return this.location.prepareExternalUrl('/rest' + path);
  }

  deleteObject(object: T): Promise<boolean> {
    return null;
  }

  protected deleteObjectDo(path: string, callback?: (deleted: boolean) => void, parameters?: any): Promise<boolean> {
    let params = new URLSearchParams();
    if (parameters) {
      for (let name in parameters) {
        params.set(name, parameters[name]);
      }
    }

    const url = this.getUrl(path);
    return this.http.delete(
      url,
      {
        headers: new Headers({ 'Content-Type': 'application/json' }),
        search: params
      }
    ).toPromise()
      .then(response => {
        const json = response.json();
        if (json.error) {
          this.showError(json.error);
          return false;
        } else {
          var deleted = json.deleted == true;
          if (callback) {
            callback(deleted);
          }
          return deleted;
        }
      })
      .catch(this.handleError.bind(this));
  }

  getLabel(object: T): string {
    let fieldNames : string[];
    if (this.labelFieldName) {
      fieldNames = this.labelFieldName.split('.');
    } else {
      fieldNames = [ this.idFieldName ];
    }
    let value : any = object;
    for (const fieldName of fieldNames) {
      if (value == null) {
        return null;
      } else {
        value = value[fieldName]
      }
    }
    return value;
  }
  
  getObject(id: string): Promise<T> {
     return this.getObjectDo(this.path +'/' + id);
  }

  getObjectDo(path: string): Promise<T> {
    const url = this.getUrl(path);
    return this.http.get(url)
      .toPromise()
      .then(response => {
        const json = response.json();
        if (json.error) {
          this.showError(json.error);
          return null;
        } else {
          return this.toObject(json);
        }
      })
      .catch(this.handleError.bind(this));
  }

  getObjects(): Promise<T[]> {
    return this.getObjectsDo(this.path);
  }
  
  getObjectsDo(path: string): Promise<T[]> {
    let params = new URLSearchParams();

    const url = this.getUrl(path);
    return this.http.get(url)
      .toPromise()
      .then(response => {
        let objects: T[] = [];
        const json = response.json();
        if (json.error) {
          this.showError(json.error);
        } else {
          const data = json.data;
          if (data) {
            data.forEach((json: any) => {
              let object = this.toObject(json);
              objects.push(object);
            });
          }
        }
        return objects;
      })
      .catch(this.handleError.bind(this));
  }
 
  getPath() : string {
    return this.path;
  }

  getRowsPage(
    offset: number,
    limit: number,
    filterFieldName : string,
    filterValue : string
  ): Promise<any> {
    let params = new URLSearchParams();
    params.set('offset', offset.toString()); 
    params.set('limit', limit.toString());
    if (filterFieldName && filterValue) {
      params.set("filterFieldName", filterFieldName);
      params.set("filterValue", filterValue);
    }
    const url = this.getUrl(this.path);
    return this.http.get(
      url,
      {
        search: params
      }
    ).toPromise()
      .then(response => {
        let rows: T[] = [];
        let total = 0;
        const json = response.json();
        if (json.error) {
          this.showError(json.error);
          return null;
        } else {
          const data = json.data;
          if (data) {
            data.forEach((json: any) => {
              let object = this.toObject(json);
              rows.push(object);
            });
            total = json.total;
          }
        }
        return {
          rows: rows,
          count: total
        };
      })
      .catch(this.handleError.bind(this));
  }
 
  getTypeTitle() : string {
    return this.typeTitle;
  }
  
  newObject(): T {
    return null;
  }
  
  toObject(json: any): T {
    let object = this.newObject();
    Object.assign(object, json);
    return object;
  }

  updateObject(object: T): Promise<T> {
    return null;
  }

  protected updateObjectDo(path: string, object: T, callback?: () => void): Promise<T> {
    const url = this.getUrl(path);
    const jsonText = JSON.stringify(object);
    return this.http.put(
      url,
      jsonText,
      this.jsonHeaders
    ).toPromise()
      .then(response => {
        const json = response.json();
        if (json.error) {
          this.showError(json.error);
          return null;
        } else {
          Object.assign(object, json);
          if (callback) {
            callback();
          }
          return object;
        }
      })
      .catch(this.handleError.bind(this));
  }
}
