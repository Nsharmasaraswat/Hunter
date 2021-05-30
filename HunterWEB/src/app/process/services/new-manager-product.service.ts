import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable()
export class NewManagerProductService {

  constructor(
    private http: HttpClient
  ) { }

    loadAllProdutsModels() {
      return this.http.get(environment.processserver + 'productmodel/all')
    }

    loadProductModel(id) {
      return this.http.get(environment.processserver + 'productmodel/')
    }

    getAllProduct(type) {

      return this.http.get(environment.processserver + 'product/bytype/' + type)

    }

    setProduct(body) {
      return this.http.post(environment.processserver + 'product', body);
    }

    deleteProduct(id) {
      return this.http.delete(environment.processserver + 'product/' + id);
    }

    updateProduct(id, body) {
      return this.http.put(environment.processserver + 'product/' + id, body)
    }

}
