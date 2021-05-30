import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class AddressService {

  constructor(
    private http: HttpClient
  ) { }

  loadAllAddressModels() {
    return this.http.get(environment.processserver + 'addressmodel/all')
  }

  getAllAddress(type) {
    return this.http.get(environment.processserver + 'address/bytype/' + type)
  }

  setAddress(body) {
    return this.http.post(environment.processserver + 'address', body);
  }

  deleteAddress(id) {
    return this.http.delete(environment.processserver + 'address/' + id);
  }

  updateAddress(id, body) {
    return this.http.put(environment.processserver + 'address', body)
  }

  listByTypeAndLocation(type: string, location: string): Observable<any> {
    return this.http.get(environment.processserver + 'address/bytypeandlocation/' + type + '/' + location);
  }
}
