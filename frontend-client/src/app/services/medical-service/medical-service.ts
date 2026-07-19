import {Service, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ServiceResponse} from '../../models/service.model';

@Service()
export class MedicalService {

    private http = inject(HttpClient)
    private url: string = "http://localhost:9000/api/v1/services"

    findAll(): Observable<ServiceResponse[]> {
        return this.http.get<ServiceResponse[]>(this.url)
    }

    findById(id: string) {
        return this.http.get<ServiceResponse>(`${this.url}/by-id?serviceId=${id}`)
    }
}
