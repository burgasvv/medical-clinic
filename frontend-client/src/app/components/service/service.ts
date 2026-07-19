import {Component, inject, OnInit, signal} from '@angular/core';
import {MedicalService} from '../../services/medical-service/medical-service';
import {ServiceResponse} from '../../models/service.model';

@Component({
    selector: 'app-service',
    standalone: false,
    templateUrl: './service.html',
    styleUrl: './service.scss',
})
export class Service implements OnInit {

    private medicalService = inject(MedicalService)
    services = signal<ServiceResponse[]>([])

    ngOnInit(): void {
        this.medicalService.findAll().subscribe({
            next: (data) => this.services.set(data),
            error: (err) => console.error('Ошибка при загрузке:', err)
        })
    }
}
