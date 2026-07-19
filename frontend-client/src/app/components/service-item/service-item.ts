import {Component, inject, OnInit, signal} from '@angular/core';
import {MedicalService} from '../../services/medical-service/medical-service';
import {ServiceResponse} from '../../models/service.model';
import {ActivatedRoute} from '@angular/router';

@Component({
    selector: 'app-service-item',
    standalone: false,
    templateUrl: './service-item.html',
    styleUrl: './service-item.scss',
})
export class ServiceItem implements OnInit {

    private medicalService = inject(MedicalService)
    private route = inject(ActivatedRoute)
    private service = signal<ServiceResponse | undefined>(undefined)

    ngOnInit(): void {
        let id = this.route.snapshot.paramMap.get("id")
        this.medicalService.findById(id!).subscribe({
            next: value => this.service.set(value),
            error: err => console.error('Ошибка при загрузке:', err)
        })
    }
}
