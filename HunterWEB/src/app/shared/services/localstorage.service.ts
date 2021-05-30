import { Inject, Injectable } from '@angular/core';
import { SESSION_STORAGE, StorageService } from 'angular-webstorage-service';

@Injectable()
export class LocalStorageService {

  constructor(@Inject(SESSION_STORAGE) private storage: StorageService) { }

  public storeOnLocalStorage(storageKey: string, element: any): void {
    //get array of tasks from local storage
    let array = this.storage.get(storageKey) || [];
    // push new task to array
    array.push(element);
    // insert updated array to local storage
    this.storage.set(storageKey, array);
    console.log(this.storage.get(storageKey) || 'LocaL storage is empty');
  }

  public listFromLocalStorage(storageKey: string): any {
    console.log(this.storage.get(storageKey) || 'LocaL storage does not contain' + storageKey);
    return this.storage.get(storageKey);
  }

  public removeAllFromLocalStorage(storageKey: string): void {
    this.storage.remove(storageKey);
  }

  public getFromLocalStorage(storageKey: string, key: string, value: any): any {
    return this.storage.get(storageKey).find(r => r[key] === value);
  }

  public removeFromLocalStorage(storageKey: string, key: string, value: any): void {
    this.storage.remove(this.getFromLocalStorage(storageKey, key, value));
  }
}
