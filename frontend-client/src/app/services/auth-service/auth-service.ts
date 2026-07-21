import {inject, Service} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {NgForm} from '@angular/forms';

@Service()
export class AuthService {

    private http = inject(HttpClient)
    private url: string = "http://localhost:9000/api/v1/security"

    login(form: NgForm): Observable<any> {
        const {email, password} = form.value
        const credentials = btoa(`${email}:${password}`);
        const headers = new HttpHeaders({
            'Authorization': `Basic ${credentials}`
        });
        return this.http.post(`${this.url}/login`, {}, { headers })
    }

    logout(): Observable<any> {
        return this.http.post(`${this.url}/logout`, {})
    }
}
