import {inject, Service, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {IdentityResponse} from '../../models/identity.model';

@Service()
export class IdentityService {

    private url: string = "http://localhost:9000/api/v1/identities"
    private http = inject(HttpClient)

    public authIdentity = signal<IdentityResponse | null>(null)

    findAuthenticated() {
        this.http.get<IdentityResponse>(`${this.url}/authenticated`).subscribe({
            next: value => this.authIdentity.set(value),
            error: err => console.log(err)
        })
    }
}
