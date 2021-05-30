import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs/Rx";
import { environment } from "../../../environments/environment";
import { AndroidInterface } from "../../shared/interface/AndroidInterface";
import { HunterPermission } from "../../shared/model/HunterPermission";
import { HunterUser } from "../../shared/model/HunterUser";

declare var forge: any;

@Injectable()
export class AuthService {
  constructor(private http: HttpClient) {}

  getCredential(username: String): Observable<any> {
    return this.http.get(environment.coreserver + "auth/preauth/" + username);
  }

  login(username: string, password: string) {
    let credential = { credential: password };

    return this.http.post(
      environment.coreserver + "auth/validate/" + username,
      credential
    );
  }

  deriveAKey(password, salt, iterations) {
    let byteSalt = forge.util.hexToBytes(salt);
    return forge.util.bytesToHex(
      forge.pkcs5.pbkdf2(password, byteSalt, iterations, 64)
    );
  }

  getUser(): Observable<any> {
    return this.http.get(environment.coreserver + "user/");
  }

  getUserId(): Observable<HunterUser> {
    return this.http.get(environment.coreserver + "user/").map( (user: HunterUser) => {
      return user;
    })
  }

  getPermissions(): Observable<HunterPermission[]> {
    return this.http
      .get(environment.coreserver + "user/permission/")
      .map((perms: HunterPermission[]) => {
        if (perms !== null && perms !== undefined) {
          return perms
            .filter((p) => p.app === "HunterWEB")
            .filter((perm, i, arr) => {
              return arr.indexOf(arr.find((t) => t.id === perm.id)) === i;
            });
        }
      });
  }


  checkMobile(): boolean {
    if (
      window.navigator.userAgent === "hunter WMS Mobile" ||
      window.navigator.userAgent === ""
    ) {
      let android: AndroidInterface = window["AndroidInterface"];
      let token: string = android.getToken();

      if (token !== undefined && token.length > 0) {
        android.showToast(token);
        sessionStorage.setItem("currentUser", token);
        return true;
      }
    }
    return false;
  }
}
